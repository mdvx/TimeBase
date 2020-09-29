package deltix.qsrv.util.cmd;

import deltix.qsrv.QSHome;
import deltix.util.Version;
import deltix.util.cmdline.AbstractShell;
import deltix.util.io.Home;

import java.util.regex.Matcher;

/**
 * Extends AbstractShell with built-in support for -home parameter that specifies QSHome
 */
public abstract class AbstractShellEx extends AbstractShell {

    protected AbstractShellEx(String[] args) {
        super(args);
    }

    @Override
    public String expandPath(String path) {
        return (path.replaceAll ("\\$\\{home\\}", Matcher.quoteReplacement (Home.get ())));
    }

    @Override
    protected boolean doSet(String option, String value) throws Exception {
        if (option.equalsIgnoreCase ("home")) {
            System.out.println("Setting " + QSHome.QSRV_HOME_SYS_PROP + "=" + value);
            QSHome.set(value);
            return (true);
        }
        return super.doSet(option, value);
    }

    @Override
    protected boolean doCommand(String key, String args) throws Exception {

        if (key.equalsIgnoreCase ("version")) {
            System.out.println ("Version " + Version.VERSION_STRING);
            return (true);
        }

        return super.doCommand(key, args);
    }
}
