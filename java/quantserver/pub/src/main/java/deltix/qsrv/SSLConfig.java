package deltix.qsrv;

import deltix.util.io.Home;

public class SSLConfig {
    public String                   keystoreFile    = Home.getPath("cert/localhost.jks");
    public String                   keystorePass    = "deltix";
    public int                      sslPort         = 0;
}
