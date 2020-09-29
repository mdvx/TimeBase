package deltix.qsrv.dtb.store.pub;

import deltix.qsrv.dtb.store.dataacc.TimeSlice;

public interface TimeSliceIterator {

    public DataAccessor     getAccessor();

    public void             process(TimeSlice slice);
}
