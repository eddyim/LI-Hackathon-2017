package demo.views;
import java.util.*;
import demo.model.*;
public class Messages extends bb.sparkjava.BBSparkTemplate {

private static Messages INSTANCE = new Messages();
    public static String render(List<Message> messages, String user) {
        StringBuilder sb = new StringBuilder();
        renderInto(sb,messages,user);
        return sb.toString();
    }

     public static void renderInto(Appendable buffer,List<Message> messages, String user) {INSTANCE.renderImpl(buffer,messages,user);}    public void renderImpl(Appendable buffer,List<Message> messages, String user) {
        try {
            buffer.append("\n");
            buffer.append("\n");
            buffer.append("\n");
            buffer.append("\n\n");
            for (Message m: messages) {
            buffer.append("\n<div class=\"message\">\n    <span style=\"font-weight: bold\">");
            buffer.append(toS(m.getSender()));
            buffer.append(": </span>");
            buffer.append(toS(m.getMessage()));
            buffer.append("\n</div>\n");
            }
} catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}