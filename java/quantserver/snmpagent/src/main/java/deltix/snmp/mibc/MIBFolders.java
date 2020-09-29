package deltix.snmp.mibc;

import deltix.snmp.parser.*;
import deltix.util.text.ShellPatternCSMatcher;
import java.util.*;
import java.io.*;

/**
 *
 */
public class MIBFolders {
    private final Map <String, File>    moduleIdToFile = new HashMap <> ();
    
    public void                     addFolderToIndex (
        File                            folder, 
        boolean                         recursive,
        String                          mibShellPattern
    )
        throws IOException
    {
        File []     files = folder.listFiles ();
        
        if (files == null)
            return;
        
        for (File f : files) {
            if (f.isDirectory ()) {
                if (recursive)
                    addFolderToIndex (f, true, mibShellPattern);
            }
            else if (ShellPatternCSMatcher.INSTANCE.matches (f.getName (), mibShellPattern))
                addMibFileToIndex (f);
        }
    }
    
    public void                     addMibFileToIndex (File f) 
        throws IOException 
    {
        String                      id = MIBParser.getModuleName (f);
        
        if (id != null) {
            synchronized (moduleIdToFile) {
                moduleIdToFile.put (id, f);
            }
        }
    }
    
    public File                     findModule (String moduleId) {
        synchronized (moduleIdToFile) {
            return (moduleIdToFile.get (moduleId));
        }
    }
}
