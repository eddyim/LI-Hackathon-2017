package bb.mgen.demo;

import java.io.IOException;


public class TestConditionalExpression extends bb.runtime.BaseBBTemplate {
    private static TestConditionalExpression INSTANCE = new TestConditionalExpression();


    public static String render() {
        StringBuilder sb = new StringBuilder();
        renderInto(sb);
        return sb.toString();
    }

    public static void renderInto(Appendable buffer) {
        INSTANCE.renderImpl(buffer);
    }

    public void renderImpl(Appendable buffer) {
        try {
            boolean x = true;
            buffer.append("\n<p>");
            buffer.append(toS("hello" if x));
            buffer.append("</p>\n<h1>The word hello should have rendered above</h1>");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
