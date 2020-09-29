package deltix.qsrv.hf.tickdb.topicdemo.testmode;

import deltix.qsrv.hf.tickdb.topicdemo.ReadAndReplyBase;
import deltix.qsrv.hf.tickdb.topicdemo.ReadEchoBase;
import deltix.qsrv.hf.tickdb.topicdemo.WriteBase;
import deltix.qsrv.hf.tickdb.topicdemo.util.ExperimentFormat;

/**
 * @author Alexei Osipov
 */
public interface TestComponentProvider {
    ReadAndReplyBase getReader();
    WriteBase getWriter();
    ReadEchoBase getEchoReader(ExperimentFormat experimentFormat);
}
