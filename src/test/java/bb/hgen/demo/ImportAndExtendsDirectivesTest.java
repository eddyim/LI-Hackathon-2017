package bb.hgen.demo;

import java.io.IOException;

import java.util.*;

public class ImportAndExtendsDirectivesTest extends bb.runtime.BaseBBTemplate {

private static ImportAndExtendsDirectivesTest INSTANCE = new ImportAndExtendsDirectivesTest();


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
            buffer.append("\n<HTML>\n<BODY>\n");
            System.out.println( "Evaluating date now" );
            buffer.append("\n");
            Date date = new Date();
            buffer.append("\nHello!  The time is now ");
            buffer.append(toS(date));
            buffer.append("\n</BODY>\n</HTML>");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
