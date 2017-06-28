package bb.hgen.demo;

import java.io.IOException;

import java.util.*;

public class NestedImportTest {

private static NestedImportTest INSTANCE = new NestedImportTest();


public static class mySection {

    public static String render() {
        StringBuilder sb = new StringBuilder();
        renderInto(sb);
        return sb.toString();
    }

    public static void renderInto(Appendable buffer) {
        try {            buffer.append("\n        ");
            buffer.append("\n        ");
            HashSet<Integer> myHashSet = new HashSet<>();
        myHashSet.add(1);
        myHashSet.add(2);
        myHashSet.add(3);
        for(Integer a: myHashSet) {
            buffer.append("\n        <h2 style=\"font-size: ");
            buffer.append(toS(a));
            buffer.append("\">Font size: ");
            buffer.append(toS(a));
            buffer.append("</h2>\n        ");
            }
            buffer.append("\n    ");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String toS(Object o) {
        return o == null ? "" : o.toString();
    }
}
    public static String render() {
        StringBuilder sb = new StringBuilder();
        renderInto(sb);
        return sb.toString();
    }

    public static void renderInto(Appendable buffer) {
        INSTANCE.renderImpl(buffer);
    }
    public static void renderImpl(Appendable buffer) {
        try {            buffer.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n    <meta charset=\"UTF-8\">\n    <title>Nested Import Tests</title>\n</head>\n<body>\n    <h1>This will make sure that nested imports are handled correctly.</h1>\n    ");

mySection.renderInto(buffer);
            buffer.append("\n        <p> The above section should work </p>\n</body>\n</html>");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String toS(Object o) {
        return o == null ? "" : o.toString();
    }
}