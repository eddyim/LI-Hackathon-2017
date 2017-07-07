package .demo.views;

import java.io.IOException;


public class Layout extends bb.sparkjava.BBSparkTemplate implements bb.runtime.ILayout {

private static Layout INSTANCE = new Layout();


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
            buffer.append("\n\n<html>\n<head>\n    <script src=\"/js/site.js\"></script>\n    <link rel=\"stylesheet\" type=\"text/css\"  href=\"/css/site.css\"/>\n    <title>Hello Spark BB Templates!</title>\n</head>\n\n");
    }
    @Override
    public void footer(Appendable buffer) throws IOException {
            buffer.append("\n\n</html>");
    }
}
