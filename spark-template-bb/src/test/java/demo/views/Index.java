package demo.views;
import java.util.*;
import demo.model.*;
public class Index extends bb.sparkjava.BBSparkTemplate {

private static Index INSTANCE = new Index();
    public static String render(List<Message> messages, String user) {
        StringBuilder sb = new StringBuilder();
        renderInto(sb,messages,user);
        return sb.toString();
    }

     public static void renderInto(Appendable buffer,List<Message> messages, String user) {INSTANCE.renderImpl(buffer,messages,user);}    public void renderImpl(Appendable buffer,List<Message> messages, String user) {
        try {
Layout.asLayout().header(buffer);            buffer.append("\n");
            buffer.append("\n");
            buffer.append("\n");
            buffer.append("\n");
            buffer.append("\n\n<div id=\"outer-frame\">\n    <div id=\"who\">\n        <ul>\n            <li>Harika</li>\n            <li>Ed</li>\n            <li>Carson</li>\n        </ul>\n    </div>\n\n    <div id=\"top-bar\">\n        <div id=\"title\">\n        Welcome to internchan, ");
            buffer.append(toS(user));
            buffer.append("\n        </div>\n    </div>\n\n    <div id=\"messages\" ic-src=\"/messages\" ic-poll=\"1s\">\n        ");
            for (Message m: messages) {
            buffer.append("\n        <div class=\"message\">\n            <span style=\"font-weight: bold\">");
            buffer.append(toS(m.getSender()));
            buffer.append(": </span>");
            buffer.append(toS(m.getMessage()));
            buffer.append("\n        </div>\n        ");
            }
            buffer.append("\n    </div>\n\n    <div id=\"chat-box\">\n        <form action=\"/\" method=\"post\" autocomplete=\"off\">\n        <textarea id=\"input-box\" type=\"text\" name=\"message\" wrap=\"hard\" rows =\"3\" cols=\"90\" autofocus=\"autofocus\"></textarea>\n            <button ic-post-to=\"/\">Submit</button>\n        </form>\n    </div>\n</div>");
Layout.asLayout().footer(buffer);} catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}