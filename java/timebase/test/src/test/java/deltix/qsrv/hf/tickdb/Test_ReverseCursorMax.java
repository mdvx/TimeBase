package deltix.qsrv.hf.tickdb;

import org.junit.experimental.categories.Category;
import deltix.util.JUnitCategories.TickDBFast;

@Category(TickDBFast.class)
public class Test_ReverseCursorMax extends Test_ReverseCursor {
    static {
        distribution_factor = 0;
    }
}
