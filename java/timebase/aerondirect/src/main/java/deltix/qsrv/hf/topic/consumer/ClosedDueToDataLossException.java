package deltix.qsrv.hf.topic.consumer;

import deltix.util.concurrent.CursorIsClosedException;

/**
 * This exception indicates that cursor or other data source was closed because of detected data loss.
 *
 * @author Alexei Osipov
 */
public class ClosedDueToDataLossException extends CursorIsClosedException {
}
