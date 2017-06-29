package bb.egen.demo;
public class IncludeNestedSectionTest extends bb.runtime.BaseBBTemplate {

private static IncludeNestedSectionTest INSTANCE = new IncludeNestedSectionTest();
    public static String render() {
        StringBuilder sb = new StringBuilder();
        renderInto(sb);
        return sb.toString();
    }

    public String toS(Object o) {
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