package bb.hgen.demo;

import java.io.IOException;


public class TestEmpty extends bb.runtime.BaseBBTemplate {

private static TestEmpty INSTANCE = new TestEmpty();


    public static String render() {
        StringBuilder sb = new StringBuilder();
        renderInto(sb);
        return sb.toString();
    }

    public static void renderInto(Appendable buffer) {
        INSTANCE.renderImpl(buffer);
    }

    public void renderImpl(Appendable buffer) {
    }

}
