package bb.mgen.demo;

import java.io.IOException;


public class Sample1 extends bb.runtime.BaseBBTemplate {
    private static Sample1 INSTANCE = new Sample1();


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
            buffer.append("<html>\n<body>\n<h1>Hello BB Templates!</h1>\n</body>\n</html>");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
