package bb.egen.demo;

import bb.runtime.ILayout;

import java.io.IOException;

public class LayoutExample extends bb.runtime.BaseBBTemplate implements ILayout {

private static LayoutExample INSTANCE = new LayoutExample();
static class header extends bb.runtime.BaseBBTemplate {

private static header INSTANCE = new header();
    public static String render() {
        StringBuilder sb = new StringBuilder();
        renderInto(sb);
        return sb.toString();
    }

     public static void renderInto(Appendable buffer) {INSTANCE.renderImpl(buffer);}    public void renderImpl(Appendable buffer) {
        try {
            buffer.append("<html>\n<body>\n");
} catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
static class footer extends bb.runtime.BaseBBTemplate {

private static footer INSTANCE = new footer();
    public static String render() {
        StringBuilder sb = new StringBuilder();
        renderInto(sb);
        return sb.toString();
    }

     public static void renderInto(Appendable buffer) {INSTANCE.renderImpl(buffer);}    public void renderImpl(Appendable buffer) {
        try {
            buffer.append("\n</body>\n</html>");
} catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
    @Override
    public void header(Appendable buffer) throws IOException {
        header.renderInto(buffer);
    }


    @Override
    public void footer(Appendable buffer) throws IOException {
        footer.renderInto(buffer);
    }
    public static String render() {
        StringBuilder sb = new StringBuilder();
        renderInto(sb);
        return sb.toString();
    }

     public static void renderInto(Appendable buffer) {INSTANCE.renderImpl(buffer);}    public void renderImpl(Appendable buffer) {
        try {
            header.renderInto(buffer);
            footer.renderInto(buffer);
} catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}