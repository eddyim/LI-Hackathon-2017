package bb.expressions;

import org.junit.Test;
import expressions.*;

import static org.junit.Assert.assertEquals;

public class ExpressionsTest {

    @Test
    public void basicExpressionsWork() {
        assertEquals("2", SimpleExpression1.render());
        assertEquals("2", SimpleExpression2.render());
    }

}
