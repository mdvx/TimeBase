package deltix.qsrv.hf.security;

import deltix.qsrv.hf.security.rules.AccessControlEntry;

import java.util.List;

public interface SecurityController {
    void reloadPermissions();

    List<AccessControlEntry> getEffectivePermissions();
}
