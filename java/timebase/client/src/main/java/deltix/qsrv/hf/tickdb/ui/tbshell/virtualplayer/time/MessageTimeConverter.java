package deltix.qsrv.hf.tickdb.ui.tbshell.virtualplayer.time;

import deltix.timebase.messages.InstrumentMessage;

/**
 * @author Alexei Osipov
 */
public interface MessageTimeConverter {
    void convertTime(InstrumentMessage message);
}
