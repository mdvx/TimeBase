package deltix.qsrv.hf.security;

public interface UserContext {
    String getCurrentUser();
    void setCurrentUser(String currentUser);
}
