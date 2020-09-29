package deltix.qsrv.hf.tickdb.comm.server.aeron;

import deltix.util.io.Home;
import deltix.util.io.aeron.DXAeron;
import deltix.util.vsocket.TransportProperties;

/**
 * @author Alexei Osipov
 */
public class DXAeronHelper {
    public static void start(boolean startMediaDriver) {
        DXAeron.start(Home.getPath("temp/dxipc"), startMediaDriver);
    }
}
