package bb.hgen.demo;

import java.io.IOException;


public class TestEmpty {

private static TestEmpty INSTANCE = new TestEmpty();


    public static String render() {
        StringBuilder sb = new StringBuilder();
        renderInto(sb);
        return sb.toString();
    }

    public static void renderInto(Appendable buffer) {
  INSTANCE.renderImpl(buffer);
}    public static void renderImpl(Appendable buffer) {
        try {
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String toS(Object o) {
        return o == null ? "" : o.toString();
    }
}