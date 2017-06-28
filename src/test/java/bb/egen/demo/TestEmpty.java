package bb.egen.demo;
import java.io.IOException;
public class TestEmpty  {

private static TestEmpty INSTANCE = new TestEmpty();
    public static String render() {
        StringBuilder sb = new StringBuilder();
        renderInto(sb);
        return sb.toString();
    }

    private static String toS(Object o) {
        return o == null ? "" : o.toString();
    }

     public static void renderInto(Appendable buffer) {INSTANCE.renderImpl(buffer);}    public void renderImpl(Appendable buffer) {
    }
}