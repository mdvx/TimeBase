package deltix.gradle.tasks

import deltix.buildtools.jarsearch.JarFinder
import groovy.util.slurpersupport.GPathResult
import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

/**
 * Properties:
 *      destinationDir (default : rootDir/bin)
 *      templates
 *      useRunJar
 *      libDir
 * Methods:
 *      xml(path)
 *      xml(file)
 *      commandFile(Closure)
 *      commandFiles(Closures)
 *          closure used for initializing CommandFileParams
 *          example in TestCommandFilesGeneratorTask.testCommandFileInit
 */

class CommandFilesGeneratorTask extends DefaultTask {

    def setJavaLinux = """
if [ -x "\$DELTIX_HOME/jre/bin/java" ];  then
    start="\$DELTIX_HOME/jre/bin/java"
elif [ -n "\$JAVA_HOME" ] && [ -x "\$JAVA_HOME/bin/java" ];  then
    start="\$JAVA_HOME/bin/java"
elif type -p java; then
    start=java
else
    echo "no java"
fi
"""

    def checkVersionLinux = """
if [[ "\$_java" ]]; then
    version=\$("\$_java" -version 2>&1 | awk -F '"' '/version/ {print \$2}')
    echo java version "\$version"
    if [[ "\$version" > "1.8" ]]; then
        echo version is more than 1.5
    else         
        echo version is less than 1.5
    fi
fi
"""

    @Input // URLs are not cacheable
    List<URL> xmls

    List<CommandFileParams> commandFiles

    @Input
    boolean useRunJar = false // RunJar if true. RunJava if false.

    @OutputDirectory
    File destinationDir = project.file("$project.rootDir/bin")

    @InputDirectory
    File libDir = null

    @Input
    boolean templates = false

    CommandFilesGeneratorTask() {
        xmls = new ArrayList<>()
        commandFiles = new ArrayList<>()
    }

    void commandFile(Closure commandFileParamsClosure) {
        commandFiles.add(new CommandFileParams(commandFileParamsClosure))
    }

    void commandFiles(Closure... commandFileParamsClosures) {
        commandFileParamsClosures.each { commandFiles.add(new CommandFileParams(it)) }
    }

    void xml(URL url) {
        xmls.add(url)
    }

    @Deprecated
    void xml(String path) {
        xml project.file(path)
    }

    @Deprecated
    void xml(File file) {
        if (!file.exists())
            throw new InvalidUserDataException("Xml file $file does not exist.")

        xmls.add(file.toURI().toURL())
    }


    @TaskAction
    void generate() {
        if (!destinationDir.exists())
            destinationDir.mkdirs()
        else if (!destinationDir.isDirectory())
            throw new InvalidUserDataException("DestinationDir should be a directory. ($destinationDir)")

        for (URL xmlLocation : xmls) {
            def schema = new XmlSlurper().parse(xmlLocation.openStream())
            schema.command.each { commandFiles.add(new CommandFileParams(it)) }
        }

        if (useRunJar && (libDir == null || !libDir.isDirectory())) {
            throw new InvalidUserDataException("libDir should be a directory. ($libDir)")
        }

        JarFinder jarFinder = useRunJar ? JarFinder.createFromJarDirectory(libDir) : null;

        def linux = !System.properties['os.name'].toLowerCase().contains('windows')

        for (CommandFileParams params : commandFiles) {
            if (params.type == CommandFileParams.Type.SERVICE)
                continue;

            if (params.rootJar == null && useRunJar) {
                params.rootJar = jarFinder.getJarNameByClassName(params.className)
                if (params.rootJar == null) {
                    throw new RuntimeException("Failed to find root jar for class: " + params.className);
                }
            }

            params.destination = destinationDir
            params.template = templates
            params.createFiles()

            if (linux)
                project.exec {
                    workingDir destinationDir
                    commandLine 'chmod'
                    args 'a+x', new File(destinationDir, params.name + CommandFileParams.LINUX_FILE_EXTENSION)
                }
        }
    }

    class CommandFileParams {
        static final String CLIENT_ENV = 'clntenv'

        enum Type {
            CMD, APP, SERVICE
        }
        Type type
        String name
        String title
        String jvmArgs
        String args
        String splash
        String className
        String config = CLIENT_ENV
        String rootJar = null

        boolean template = false

        File destination

        private static final WINDOWS_FILE_EXTENSION = '.cmd'
        private static final LINUX_FILE_EXTENSION = '.sh'

        CommandFileParams(Closure init) {
            init.setDelegate(this)
            init()
        }

        CommandFileParams(GPathResult node) {
            this.type = Type.valueOf(node.@type.toString().toUpperCase())
            this.name = node.name
            this.title = node.title
            this.jvmArgs = node.jvmArgs
            this.args = node.args
            this.splash = node.splash
            this.className = node.className
            if (!node.config.toString().isEmpty())
                this.config = node.config
        }

        void createFiles() {
            if (!name)
                throw new InvalidUserDataException("Name isn't set.")
            if(!className)
                throw new InvalidUserDataException("ClassName isn't set.")
            if(!destination)
                throw new InvalidUserDataException("Destination isn't set.")

            createWindowsFile()
            createLinuxFile()
            createConfig()
        }

        void createWindowsFile() {
            new File(destination, name + WINDOWS_FILE_EXTENSION).withPrintWriter { writer ->
                writer.println '@echo off'
                writer.println '@rem This file is automatically generated'
                writer.println 'setlocal'

                if (template)
                    writer.println 'set DELTIX_HOME=${INSTALL_PATH}\\QuantServer'

                if (type == Type.APP) {
                    writer.println 'set START=start \"\" \"%DELTIX_HOME%\\jre\\bin\\javaw\"'
                    writer.println 'if not \"%NOJAVAW%\" == \"\" ('
                    writer.println 'set START=\"%DELTIX_HOME%\\jre\\bin\\java\"'

                    if (title)
                        writer.println 'title ' + title

                    writer.println ')'
                } else {
                    writer.println 'set START=\"%DELTIX_HOME%\\jre\\bin\\java\"'

                    if (title)
                        writer.println 'title ' + title
                }

                writer.println "call \"%DELTIX_HOME%\\bin\\${config}.cfg$WINDOWS_FILE_EXTENSION\""

                writer.print '%START%'

                if (type == Type.APP)
                    if (splash)
                        writer.print ' -splash:\"%DELTIX_HOME%/' + splash + '\"'

                if (jvmArgs) {
                    writer.print ' '
                    writer.print jvmArgs
                }

                if (useRunJar) {
                    writer.print ' %JAVA_OPTS% -jar \"%DELTIX_HOME%\\bin\\runjar.jar\" '
                    writer.print rootJar
                    writer.print ' '
                } else {
                    writer.print ' %JAVA_OPTS% -jar \"%DELTIX_HOME%\\bin\\runjava.jar\" '
                }
                writer.print className
                if (args) {
                    writer.print ' '
                    writer.print args
                }
                writer.println ' %*'

                writer.println 'endlocal'
            }
        }

        void createLinuxFile() {

            File linuxExecutable = new File(destination, name + LINUX_FILE_EXTENSION)
            linuxExecutable.withPrintWriter { writer ->
                writer.print '#!/bin/sh\n'
                writer.print '# This file is automatically generated\n'
                if (template)
                    writer.print('export DELTIX_HOME="@INSTALL_PATH/QuantServer"\n')
                writer.print ". \"\$DELTIX_HOME/bin/${config}.cfg$LINUX_FILE_EXTENSION\"\n"

                writer.print("")
                writer.print(setJavaLinux)

                if (useRunJar) {
                    writer.print '"$start" $java_opts -jar \"$DELTIX_HOME/bin/runjar.jar\" '
                    writer.print rootJar
                    writer.print ' '
                } else {
                    writer.print '"$start" $java_opts -jar \"$DELTIX_HOME/bin/runjava.jar\" '
                }

                writer.print className
                if (args) {
                    writer.print ' '
                    writer.print args
                }
                writer.print ' $*\n'
            }
        }

        void createConfig() {
            File win = new File(destination, config + '.cfg' + WINDOWS_FILE_EXTENSION)

            def options = '-server -Xmx1000m'
            if (template)
                options = CLIENT_ENV.equals(config) ? '${CLIENT_JAVA_OPTIONS}' : '${SERVER_JAVA_OPTIONS}'

            if (!win.exists())
                win.withPrintWriter { writer -> writer.println "if \"%JAVA_OPTS%\" == \"\" set JAVA_OPTS=%JAVA_OPTS% $options\n" }

            File unix = new File(destination, config + '.cfg' + LINUX_FILE_EXTENSION)

            if (!unix.exists())
                unix.withPrintWriter { writer -> writer.print "if [ -z \"\${java_opts}\" ]; then java_opts=\"$options\"; fi\n" }
        }
    }
}