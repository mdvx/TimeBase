package deltix.qsrv.hf.tickdb.lang.pub;

import deltix.util.parsers.Element;
import deltix.util.parsers.Location;

/**
 *
 */
public abstract class DataTypeSpec extends Element {    
    public final boolean            nullable;    

    protected DataTypeSpec (
        long                        location,
        boolean                     nullable
    )
    {
        super (location);
        
        this.nullable = nullable;
    }

    public DataTypeSpec (
        boolean                     nullable
    )
    {
        this (Location.NONE, nullable);
    }            
}
