package deltix.qsrv.hf.tickdb.pub.query;

import deltix.qsrv.hf.pub.md.RecordClassDescriptor;

/**
 * Message source with indication of current type.
 */

public interface TypedMessageSource {
    /**
     *  Returns the type index of the current message.
     *  @return The current message type index.
     */
    int                             getCurrentTypeIndex ();

    /**
     *  Returns the type of the current message.
     *  @return The current message type.
     */
    RecordClassDescriptor           getCurrentType ();
}
