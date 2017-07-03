package bb.hgen.demo;

import java.io.IOException;


public class LayoutExample extends bb.runtime.BaseBBTemplate {

private static LayoutExample INSTANCE = new LayoutExample();


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
            buffer.append("<html>\n<body>\n");
            buffer.append("\n</body>\n</html>");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
