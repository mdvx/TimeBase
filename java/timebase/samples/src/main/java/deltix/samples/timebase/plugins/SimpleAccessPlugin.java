package deltix.samples.timebase.plugins;

import deltix.timebase.api.messages.BarMessage;

import deltix.qsrv.hf.pub.RawMessage;
import deltix.qsrv.hf.pub.md.RecordClassDescriptor;
import deltix.util.collections.SmallArrays;
import deltix.util.security.DataFilter;
import deltix.util.security.TimebaseAccessController;

import java.io.File;
import java.security.Principal;

public class SimpleAccessPlugin implements TimebaseAccessController {

    private File folder;


//    @Override
//    public boolean connected(Principal user, String address) {
//        return true;
//    }

    @Override
    public DataFilter<RawMessage> createFilter(Principal user, String address) {

        return new DataFilter<RawMessage>() {
            private final RecordClassDescriptor[] types = new RecordClassDescriptor[10];
            private int length = -1;

            @Override
            public boolean accept(RawMessage msg) {
                RecordClassDescriptor type = msg.type;

                int index = SmallArrays.indexOf(type, types, length);
                if (index == -1) {
                    if (BarMessage.class.getName().equals(type.getName()))
                        types[index = ++length] = type;
                }

                if (index != -1)
                    return msg.getInstrumentType() == InstrumentType.EQUITY;

                return true;
            }

            @Override
            public void close() {

            }
        };
    }

//    @Override
//    public void disconnected(Principal user, String address) {
//
//    }

    @Override
    public void close() {

    }
}
