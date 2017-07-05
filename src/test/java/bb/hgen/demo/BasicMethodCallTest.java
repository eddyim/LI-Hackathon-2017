package bb.hgen.demo;

import java.io.IOException;


public class BasicMethodCallTest extends bb.runtime.BaseBBTemplate {

private static BasicMethodCallTest INSTANCE = new BasicMethodCallTest();


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
            buffer.append("<!DOCTYPE html>\n<html>\n    <head><title>First JSP</title></head>\n    <body>\n        ");
            double num = Math.random();
        if (num > 0.95) {
            buffer.append("\n            <h2>You'll have a luck day!</h2><p>( ");
            buffer.append(toS(num));
            buffer.append(" )</p>\n        ");
            } else {
            buffer.append("\n            <h2>Well, life goes on ... </h2><p>( ");
            buffer.append(toS(num));
            buffer.append(" )</p>\n        ");
            }
            buffer.append("\n        <a href=\"www.facebook.com\"><h3>Try Again</h3></a>\n    </body>\n</html>");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
