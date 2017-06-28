package bb.egen.demo;
import java.io.IOException;
public class IncludeNestedSectionTest  {

private static IncludeNestedSectionTest INSTANCE = new IncludeNestedSectionTest();
    public static String render() {
        StringBuilder sb = new StringBuilder();
        renderInto(sb);
        return sb.toString();
    }

    private static String toS(Object o) {
        return o == null ? "" : o.toString();
    }

     public static void renderInto(Appendable buffer) {INSTANCE.renderImpl(buffer);}    public void renderImpl(Appendable buffer) {
        try {
            NestedImportTest.mySection.renderInto(buffer);
} catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}