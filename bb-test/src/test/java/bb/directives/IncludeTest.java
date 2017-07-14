package bb.directives;

import org.junit.Test;
import directives.include.*;

import static org.junit.Assert.assertEquals;


/**
 * Created by eim on 7/14/2017.
 */
public class IncludeTest {
    @Test
    public void basicIncludeWorks() {
        assertEquals("15", SimpleInclude.render());
    }

    @Test
    public void includeWithParamsWorks() {
        assertEquals("Carson", IncludeWithParams.render());
    }

    @Test
    public void includeWithMultipleParamsWorks() {
        assertEquals("Name:CarsonAge:2000", IncludeWithMultipleParams.render());
    }

}
