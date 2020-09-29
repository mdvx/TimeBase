package deltix.util.security;

public interface AccessControlRulesFactory {

    AccessControlRule[] create(UserDirectory userDirectory);
}
