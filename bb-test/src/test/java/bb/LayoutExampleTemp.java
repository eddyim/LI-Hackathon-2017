package bb;

import bb.runtime.BaseBBTemplate;
import bb.runtime.ILayout;

import java.io.IOException;

public class LayoutExampleTemp extends BaseBBTemplate implements ILayout{

    // static stuff
    private static LayoutExampleTemp INSTANCE = new LayoutExampleTemp();

    static String render() {
        StringBuilder stringBuilder = new StringBuilder();
        renderInto(stringBuilder);
        return stringBuilder.toString();
    }

    private static void renderInto(StringBuilder stringBuilder) {
        INSTANCE.renderImpl(stringBuilder);
    }

    // new method to get instance as a layout
    // should be used to insert header and footer in templates that use this layout:
    //
    //  LayoutExampleTemp.asLayout().header(buffer);
    //  ...
    //  LayoutExampleTemp.asLayout().footer(buffer);
    static ILayout asLayout() {
        return INSTANCE;
    }

    // non static stuff

    public void renderImpl(Appendable buffer) {
        try {
            header(buffer);
            footer(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void header(Appendable buffer) throws IOException {
        buffer.append(toS("<html>\n<body>\n"));
    }

    @Override
    public void footer(Appendable buffer) throws IOException {
        buffer.append(toS("</body>\n</html>"));
    }
}
