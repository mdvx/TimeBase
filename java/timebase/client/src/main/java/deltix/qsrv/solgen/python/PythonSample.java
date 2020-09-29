package deltix.qsrv.solgen.python;

import deltix.qsrv.solgen.SolgenUtils;
import deltix.qsrv.solgen.base.Project;
import deltix.qsrv.solgen.base.Sample;
import deltix.qsrv.solgen.base.Source;
import deltix.qsrv.solgen.base.StringSource;

import java.util.Map;

public abstract class PythonSample implements Sample {

    protected static final String SAMPLE_NAME_PROP = "python.sample.name";
    protected static final String SCRIPT_NAME_PROP = "python.script.name";

    private static final String CMD_LAUNCHER = "launcher.cmd-template";
    private static final String SH_LAUNCHER = "launcher.sh-template";

    private Source windowsLauncherScript;
    private Source linuxLauncherScript;

    protected void generateLaunchers(Map<String, String> params) {
        String sampleName = params.get(SAMPLE_NAME_PROP);
        if (sampleName == null) {
            throw new RuntimeException("python.sample.name is empty");
        }

        String scriptName = params.get(SCRIPT_NAME_PROP);
        if (scriptName == null) {
            throw new RuntimeException("python.script.name is empty");
        }

        windowsLauncherScript = new StringSource(
            sampleName + ".cmd",
            SolgenUtils.readTemplateFromClassPath(this.getClass().getPackage(), CMD_LAUNCHER, params)
        );
        linuxLauncherScript = new StringSource(
            sampleName + ".sh",
            SolgenUtils.convertLineSeparators(
                SolgenUtils.readTemplateFromClassPath(this.getClass().getPackage(), SH_LAUNCHER, params),
                "\n"
            )
        );
    }

    @Override
    public void addToProject(Project project) {
        if (linuxLauncherScript != null) {
            project.addScript(linuxLauncherScript);
        }

        if (windowsLauncherScript != null) {
            project.addScript(windowsLauncherScript);
        }
    }
}
