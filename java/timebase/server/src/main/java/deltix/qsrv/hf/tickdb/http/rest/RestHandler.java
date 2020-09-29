package deltix.qsrv.hf.tickdb.http.rest;

import deltix.qsrv.hf.tickdb.http.HTTPProtocol;
import deltix.util.concurrent.QuickExecutor;

import java.io.DataOutput;
import java.io.IOException;

/**
 *
 */
public abstract class RestHandler extends QuickExecutor.QuickTask {

    public RestHandler(QuickExecutor quickExecutor) {
        super(quickExecutor);
    }

    @Override
    public abstract void        run() throws InterruptedException;

    public abstract void        sendKeepAlive() throws IOException;

    public void                 sendResponse(DataOutput dout, Throwable t) throws IOException {
        dout.write(HTTPProtocol.RESPONSE_BLOCK_ID);
        if (t == null) {
            dout.writeInt(HTTPProtocol.RESP_OK);
        } else {
            dout.writeInt(HTTPProtocol.RESP_ERROR);
            dout.writeUTF(t.getClass().getName());

            String msg = t.getMessage();
            dout.writeUTF(msg != null ? msg : "");
        }
    }
}
