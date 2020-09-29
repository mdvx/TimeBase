package deltix.qsrv.hf.tickdb.testframework;

import deltix.timebase.messages.Bitmask;
import deltix.timebase.messages.SchemaElement;

/**
 *
 */
@Bitmask
@SchemaElement(
        title = "Bitmask Test Type"
)
public enum TestBitmask {
    BIT0,
    BIT1,
    BIT2,
    BIT3
}
