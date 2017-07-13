package bb.mgen.demo;

import java.io.IOException;


public class NestedClassExtendsTest extends bb.runtime.TestExtendsTemplate {
    private static NestedClassExtendsTest INSTANCE = new NestedClassExtendsTest();


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
            buffer.append("\n<h1>Hello</h1>\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    
    public static class mySection extends bb.runtime.TestExtendsTemplate {
        private static mySection INSTANCE = new mySection();

    
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
                buffer.append("\n<h2>This section should extend the same thing</h2>\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
    }
