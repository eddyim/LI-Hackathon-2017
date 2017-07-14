package bb.mgen.demo;

import java.io.IOException;


public class TestForLoop extends bb.runtime.BaseBBTemplate {
    private static TestForLoop INSTANCE = new TestForLoop();


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
            buffer.append("<!DOCTYPE html>\n");
            int fontSize;
            buffer.append("\n<html>\n    <head><title>FOR LOOP Example</title></head>\n    <body>\n        ");
            for ( fontSize = 1; fontSize <= 3; fontSize++){
            buffer.append("\n            <font color = \"green\" size = \"");
            buffer.append(toS(fontSize));
            buffer.append("\">\n                JSP Tutorial\n        </font><br />\n    ");
            }
            buffer.append("\n    </body>\n</html>");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        System.out.println(render());
    }

}
