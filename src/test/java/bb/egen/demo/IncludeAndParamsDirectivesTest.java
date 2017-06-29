package bb.egen.demo;
public class IncludeAndParamsDirectivesTest extends bb.runtime.BaseBBTemplate {

private static IncludeAndParamsDirectivesTest INSTANCE = new IncludeAndParamsDirectivesTest();
    public static String render(String string, String[] strings) {
        StringBuilder sb = new StringBuilder();
        renderInto(sb,string,strings);
        return sb.toString();
    }

    public String toS(Object o) {
        return o == null ? "" : o.toString();
    }

     public static void renderInto(Appendable buffer,String string, String[] strings) {INSTANCE.renderImpl(buffer,string,strings);}    public void renderImpl(Appendable buffer,String string, String[] strings) {
        try {
            buffer.append("<!DOCTYPE html>\n<html>\n\n    <body>\n    ");
            IncludeAndParamsDirectivesTest.renderInto(buffer, "", null);
            buffer.append("\n    ");
            buffer.append("\n    ");
            ImportAndExtendsDirectivesTest.renderInto(buffer);
            buffer.append("\n    ");
            ImportAndExtendsDirectivesTest.renderInto(buffer);
            buffer.append("\n    </body>\n</html>");
} catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}