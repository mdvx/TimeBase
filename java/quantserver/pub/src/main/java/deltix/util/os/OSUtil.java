package deltix.util.os;

import deltix.util.lang.*;
import java.io.*;

/**
 *
 */
public class OSUtil {
    public static File  getUserHome () {
        return (new File (System.getProperty ("user.home")));
    }

    public static boolean isX64() {
        if (Util.IS_WINDOWS_OS) {
            return !WindowsOS.IS_X86;
        } else {
            return LinuxOS.isX64();
        }
    }
    
}
