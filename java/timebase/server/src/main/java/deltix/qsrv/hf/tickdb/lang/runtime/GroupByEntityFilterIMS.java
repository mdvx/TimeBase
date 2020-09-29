package deltix.qsrv.hf.tickdb.lang.runtime;

import deltix.qsrv.hf.pub.*;
import deltix.qsrv.hf.pub.md.*;
import deltix.qsrv.hf.tickdb.pub.query.*;
import deltix.util.collections.ArrayEnumeration;
import deltix.util.lang.Util;
import java.util.Enumeration;

/**
 *  Groups states by entity, using the very efficient entity index.
 */
public abstract class GroupByEntityFilterIMS extends FilterIMSImpl {
    private FilterState []              states = new FilterState [256];
    private int                         numStates = 0;
    protected int                       currentEntityIdx;
    protected FilterState               currentState;
    
    public GroupByEntityFilterIMS (
        InstrumentMessageSource         source,
        RecordClassDescriptor []        inputTypes,
        RecordClassDescriptor []        outputTypes,
        ReadableValue []                params
    )
    {
        super (source, inputTypes, outputTypes, params);
    }

    protected final FilterState         getState (RawMessage msg) {
        currentEntityIdx = source.getCurrentEntityIndex ();

        final int           curLength = states.length;

        if (currentEntityIdx >= curLength) {
            int             newLength =
                Util.doubleUntilAtLeast (curLength, currentEntityIdx + 1);

            Object []       old = states;
            states = new FilterState [newLength];
            System.arraycopy (old, 0, states, 0, curLength);

            currentState = states [currentEntityIdx] = newState ();
        }
        else {
            currentState = states [currentEntityIdx];

            if (currentState == null) {
                assert currentEntityIdx == numStates;
                
                currentState = states [currentEntityIdx] = newState ();
                numStates = currentEntityIdx + 1;
            }
        }

        return (currentState);
    }
    
    protected Enumeration <FilterState>  getStates () { 
        return (new ArrayEnumeration <FilterState> (states, 0, numStates));
    }
}
