package bb.statements;

import org.junit.Test;
import statements.*;

import static org.junit.Assert.assertEquals;

public class StatementsTest {

    @Test
    public void basicStatementsWork() {
        assertEquals("5", SimpleStatement.render());
    }

    @Test
    public void ifStatementsWork() {
        assertEquals("hello", IfStatement1.render());
        assertEquals("goodbye", IfStatement2.render());
        assertEquals("hello", IfStatementWithDefinedBoolean.render());
    }

    @Test
    public void loopsWork() {
        assertEquals("aaaaaaaaaa", WhileStatement.render());
        assertEquals("bbbbbbbbbb", ForStatement.render());
    }

}