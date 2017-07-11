package demo.views;
import java.io.IOException;
import bb.runtime.ILayout;
public class Layout extends bb.sparkjava.BBSparkTemplate implements ILayout {

private static Layout INSTANCE = new Layout();
public static class header1 extends bb.sparkjava.BBSparkTemplate {

private static header1 INSTANCE = new header1();
    public static String render() {
        StringBuilder sb = new StringBuilder();
        renderInto(sb);
        return sb.toString();
    }

     public static void renderInto(Appendable buffer) {INSTANCE.renderImpl(buffer);}    public void renderImpl(Appendable buffer) {
        try {
            buffer.append("\n\n<html>\n<head>\n    <link rel=\"stylesheet\" type=\"text/css\"  href=\"/css/site.css\"/>\n    <link href=\"https://fonts.googleapis.com/css?family=PT+Sans\" rel=\"stylesheet\">\n    <script src=\"https://code.jquery.com/jquery-3.2.1.min.js\"></script>\n    <script src=\"https://intercoolerreleases-leaddynocom.netdna-ssl.com/intercooler-1.1.2.min.js\"></script>\n    <script src=\"/js/site.js\"></script>\n    <title>Hello Spark BB Templates!</title>\n</head>\n\n");
} catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
public static class footer1 extends bb.runtime.BaseBBTemplate {

private static footer1 INSTANCE = new footer1();
    public static String render() {
        StringBuilder sb = new StringBuilder();
        renderInto(sb);
        return sb.toString();
    }

     public static void renderInto(Appendable buffer) {INSTANCE.renderImpl(buffer);}    public void renderImpl(Appendable buffer) {
        try {
            buffer.append("\n\n</html>");
} catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
@Override
    public void header(Appendable buffer) throws IOException {
        header1.renderInto(buffer);
    }

    @Override
    public void footer(Appendable buffer) throws IOException {
        footer1.renderInto(buffer);
    }
    static ILayout asLayout() {
        return INSTANCE;
    }
    public static String render() {
        StringBuilder sb = new StringBuilder();
        renderInto(sb);
        return sb.toString();
    }

     public static void renderInto(Appendable buffer) {INSTANCE.renderImpl(buffer);}    public void renderImpl(Appendable buffer) {
        try {
            header1.renderInto(buffer);
            footer1.renderInto(buffer);
} catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}