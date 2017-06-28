package bb.hgen.demo;

import java.io.IOException;


public class IncludeNestedSectionTest {

private static IncludeNestedSectionTest INSTANCE = new IncludeNestedSectionTest();


    public static String render() {
        StringBuilder sb = new StringBuilder();
        renderInto(sb);
        return sb.toString();
    }

    public static void renderInto(Appendable buffer) {
  INSTANCE.renderImpl(buffer);
}    public static void renderImpl(Appendable buffer) {
        try {
            buffer.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n    <meta charset=\"UTF-8\">\n    <title>Including Nested Sections Test</title>\n</head>\n<body>\n    ");
            NestedImportTest.mySection.renderInto(buffer);
            buffer.append("\n</body>\n</html>");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String toS(Object o) {
        return o == null ? "" : o.toString();
    }
}