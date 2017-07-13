package bb.mgen.demo;

import java.io.IOException;


public class LayoutExample extends bb.runtime.BaseBBTemplate implements bb.runtime.ILayout {
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
            INSTANCE.header(buffer);
            INSTANCE.footer(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static bb.runtime.ILayout asLayout() {
        return INSTANCE;
    }

    @Override
    public void header(Appendable buffer) throws IOException {
            buffer.append("</html>\n</body>\n");
    }
    @Override
    public void footer(Appendable buffer) throws IOException {
            buffer.append("\n</body>\n</html>");
    }
}
