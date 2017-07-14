package bb.mgen.demo;

import java.io.IOException;

import java.util.*;

public class aSectionTest extends bb.runtime.BaseBBTemplate {
    private static aSectionTest INSTANCE = new aSectionTest();


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
            buffer.append("<html>\n<head><title>First JSP</title></head>\n<body>\n");
            double num = Math.random();
if (num > 0.95) {
            buffer.append("\n<h2>You'll have a luck day!</h2><p>( ");
            buffer.append(toS(num));
            buffer.append(" )</p>\n");
            } else {
            buffer.append("\n<h2>Well, life goes on ... </h2><p>( ");
            buffer.append(toS(num));
            buffer.append(" )</p>\n");
            }
            buffer.append("\n<a href=\"www.facebook.com\"><h3>Try Again</h3></a>\n\n    ");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    
    public static class MySection extends bb.runtime.BaseBBTemplate {
        private static MySection INSTANCE = new MySection();

    
        public static String render(double num) {
            StringBuilder sb = new StringBuilder();
            renderInto(sb, num);
            return sb.toString();
        }

        public static void renderInto(Appendable buffer, double num) {
            INSTANCE.renderImpl(buffer, num);
        }

        public void renderImpl(Appendable buffer, double num) {
            try {
                buffer.append("\n    ");
                buffer.append("\n        <body>\n            <h1>This is a demo template</h1>\n            <p>1 + 1 = ");
                buffer.append(toS(1 + 1));
                buffer.append("</p>\n        </body>\n    ");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        
        public static class yourSection extends bb.runtime.BaseBBTemplate {
            private static yourSection INSTANCE = new yourSection();

        
            public static String render(double num) {
                StringBuilder sb = new StringBuilder();
                renderInto(sb, num);
                return sb.toString();
            }

            public static void renderInto(Appendable buffer, double num) {
                INSTANCE.renderImpl(buffer, num);
            }

            public void renderImpl(Appendable buffer, double num) {
                try {
                    buffer.append("\n        <body>\n        ");
                    int fontSize;
                    buffer.append("\n        ");
                    for ( fontSize = 1; fontSize <= 3; fontSize++){
                    buffer.append("\n        <font color = \"green\" size = \"");
                    buffer.append(toS(fontSize));
                    buffer.append("\">\n            JSP Tutorial\n        </font><br />\n        ");
                    }
                    buffer.append("\n        </body>\n    ");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        }
    }
}
