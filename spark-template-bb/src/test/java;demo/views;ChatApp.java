package views;ChatApp;

import java.io.IOException;


public class views;ChatApp extends bb.runtime.BaseBBTemplate {

private static views;ChatApp INSTANCE = new views;ChatApp();


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
            Layout.asLayout().header(buffer);
            buffer.append("\n<h1 style=\"text-align:center;color:DarkSlateGrey;\">Chat App</h1>");
            Layout.asLayout().footer(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
