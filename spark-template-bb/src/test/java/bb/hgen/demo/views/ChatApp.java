package bb.hgen.demo.views;

import java.io.IOException;


public class ChatApp extends bb.runtime.BaseBBTemplate {

private static ChatApp INSTANCE = new ChatApp();


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
            buffer.append("\n<h1 style=\"text-align:center;color:DarkSlateGrey;\">Chat App</h1>");
            Layout.asLayout().footer(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
