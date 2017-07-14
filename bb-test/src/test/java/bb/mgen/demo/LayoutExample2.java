package bb.mgen.demo;

import java.io.IOException;

import java.util.*;

public class LayoutExample2 extends bb.runtime.BaseBBTemplate {
    private static LayoutExample2 INSTANCE = new LayoutExample2();


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
            LayoutExample.asLayout().header(buffer);
            buffer.append("<html>\n<head><title>First JSP</title></head>\n\n");
            buffer.append("\n\n");
            LayoutExample.asLayout().footer(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    
    public static class MySection extends bb.runtime.BaseBBTemplate {
        private static MySection INSTANCE = new MySection();

    
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
                buffer.append("\n");
                buffer.append("\n<body>\n<h1>This is a demo template</h1>\n<p>1 + 1 = ");
                buffer.append(toS(1 + 1));
                buffer.append("</p>\n</body>\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        
        public static class yourSection extends bb.runtime.BaseBBTemplate {
            private static yourSection INSTANCE = new yourSection();

        
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
                    buffer.append("\n<body>\n");
                    int fontSize;
                    buffer.append("\n");
                    for ( fontSize = 1; fontSize <= 3; fontSize++){
                    buffer.append("\n<font color = \"green\" size = \"");
                    buffer.append(toS(fontSize));
                    buffer.append("\">\n    JSP Tutorial\n</font><br />\n");
                    }
                    buffer.append("\n</body>\n");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        }
    }
}
