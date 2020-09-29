package deltix.gradle.tasks

import groovy.util.slurpersupport.GPathResult
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class TestCommandFilesGeneratorTask {
    @Test
    void testCommandFileInit() {
        CommandFilesGeneratorTask task = getGeneratorTask()
        task.commandFile {
            type = CommandFilesGeneratorTask.CommandFileParams.Type.APP
            name = 'testName'
            title = 'testTitle'
            jvmArgs = 'testJvmArgs'
            args = 'testArgs'
            splash = 'testSplash'
            className = 'testClassName'
        }

        assert task.commandFiles.get(0).type == CommandFilesGeneratorTask.CommandFileParams.Type.APP
        assert task.commandFiles.get(0).name.equals('testName')
        assert task.commandFiles.get(0).title.equals('testTitle')
        assert task.commandFiles.get(0).jvmArgs.equals('testJvmArgs')
        assert task.commandFiles.get(0).args.equals('testArgs')
        assert task.commandFiles.get(0).splash.equals('testSplash')
        assert task.commandFiles.get(0).className.equals('testClassName')
        assert task.commandFiles.get(0).config.equals('clntenv')
    }


    @Test
    void testXmlInit() {
        def schema = new XmlSlurper().parseText(
'''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<schema>
    <command type="app">
        <name>qsadmin</name>
        <title>QuantServer Architect</title>
        <splash>bin/splash.jpg</splash>
        <jvmArgs>-Dsun.java2d.d3d=false</jvmArgs>
        <className>deltix.installer.admin.top.QSAdminMain</className>
    </command>

    <command type="cmd">
        <name>tblatency</name>
        <config>srvenv</config>
        <className>deltix.tools.LatencyTest</className>
    </command>
</schema>''')
        List<CommandFilesGeneratorTask.CommandFileParams> cfps = new ArrayList<>()
        schema.command.each { GPathResult node -> cfps.add(new CommandFilesGeneratorTask.CommandFileParams(node)) }

        assert cfps.get(0).type == CommandFilesGeneratorTask.CommandFileParams.Type.APP
        assert cfps.get(0).name.equals('qsadmin')
        assert cfps.get(0).title.equals('QuantServer Architect')
        assert cfps.get(0).jvmArgs.equals('-Dsun.java2d.d3d=false')
        assert cfps.get(0).splash.equals('bin/splash.jpg')
        assert cfps.get(0).className.equals('deltix.installer.admin.top.QSAdminMain')
        assert cfps.get(0).config.equals('clntenv')

        assert cfps.get(1).type == CommandFilesGeneratorTask.CommandFileParams.Type.CMD
        assert cfps.get(1).name.equals('tblatency')
        assert cfps.get(1).className.equals('deltix.tools.LatencyTest')
        assert cfps.get(1).config.equals('srvenv')

    }

    CommandFilesGeneratorTask getGeneratorTask() {
        def project = ProjectBuilder.builder().build()
        return project.task(type: CommandFilesGeneratorTask, 'testGenerator')
    }
}
