package deltix.qsrv.hf.security.rules;

import deltix.util.security.AccessControlRule;
import deltix.util.security.AuthorizationController;

import java.util.List;

public interface ManagedAuthorizationController extends AuthorizationController {

    interface PermissionVisitor {
        void visit (AccessControlRule.RuleEffect ruleEffect, String user, String permission, String resource, AccessControlRule.ResourceType type);
    }

    void visit (PermissionVisitor visitor);

//    void reload ();

    List<AccessControlEntry> getEffectivePermissions();
}
