package bb.mgen.demo.views;

import java.io.IOException;

import demo.model.*;

public class Login extends bb.sparkjava.BBSparkTemplate {
    private static Login INSTANCE = new Login();


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
            buffer.append("\n");
            buffer.append("\n");
            buffer.append("\n\n<div id=\"login-background\">\n    <div id=\"login-wrapper\">\n        <form action=\"/login\" method=\"post\" autocomplete=\"off\">\n            <input type=\"text\" name=\"userName\">\n            <br>\n            <button>Submit</button>\n        </form>\n    </div>\n</div>");
            Layout.asLayout().footer(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
