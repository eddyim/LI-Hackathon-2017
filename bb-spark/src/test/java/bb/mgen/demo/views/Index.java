package bb.mgen.demo.views;

import java.io.IOException;

import java.util.*;
import demo.model.*;

public class Index extends demo.view.DemoTemplateBase {
    private static Index INSTANCE = new Index();


    public static String render(List<Message> messages, String user) {
        StringBuilder sb = new StringBuilder();
        renderInto(sb, messages, user);
        return sb.toString();
    }

    public static void renderInto(Appendable buffer, List<Message> messages, String user) {
        INSTANCE.renderImpl(buffer, messages, user);
    }

    public void renderImpl(Appendable buffer, List<Message> messages, String user) {
        try {
            Layout.asLayout().header(buffer);
            buffer.append("\n");
            buffer.append("\n");
            buffer.append("\n");
            buffer.append("\n");
            buffer.append("\n\n<div id=\"outer-frame\">\n    <div id=\"who\" ic-poll=\"1s\" ic-src=\"/who\">\n        ");
            buffer.append(toS(user));
            buffer.append("\n        </div>\n    </div>\n\n    <div id=\"messages\"\n         class=\"sticky-to-bottom stuck\"\n         ic-src=\"/messages\"\n         ic-poll=\"1s\"\n         ic-on-success=\"maybeStickToBottom()\">\n        ");
            Layout.asLayout().footer(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    
    public static class inputForm extends demo.view.DemoTemplateBase {
        private static inputForm INSTANCE = new inputForm();

    
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
                buffer.append("\n            <textarea id=\"input-box\" type=\"text\" name=\"message\" wrap=\"hard\" rows =\"3\" cols=\"90\" autofocus=\"autofocus\"></textarea>\n            <button>Submit</button>\n          ");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
    
    public static class messageBox  extends demo.view.DemoTemplateBase {
        private static messageBox  INSTANCE = new messageBox ();

    
        public static String render(List<Message> messages) {
            StringBuilder sb = new StringBuilder();
            renderInto(sb, messages);
            return sb.toString();
        }

        public static void renderInto(Appendable buffer, List<Message> messages) {
            INSTANCE.renderImpl(buffer, messages);
        }

        public void renderImpl(Appendable buffer, List<Message> messages) {
            try {
                buffer.append("\n            ");
                for (Message m: messages) {
                buffer.append("\n            <div class=\"message\">\n                <span style=\"font-weight: bold\">");
                buffer.append(toS(m.getSender()));
                buffer.append(": </span>");
                buffer.append(toS(m.getMessage()));
                buffer.append("\n            </div>\n            ");
                }
                buffer.append("\n        ");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
    
    public static class who extends demo.view.DemoTemplateBase {
        private static who INSTANCE = new who();

    
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
                buffer.append("\n        <ul>\n            ");
                for (String s: getUsers()) {
                buffer.append("\n            <li>");
                buffer.append(toS(s));
                buffer.append("</li>\n            ");
                }
                buffer.append("\n        </ul>\n        ");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
