package bb.egen.demo;
public class TestInferences extends bb.runtime.BaseBBTemplate {

private static TestInferences INSTANCE = new TestInferences();
static class shouldBeInt extends bb.runtime.BaseBBTemplate {

private static shouldBeInt INSTANCE = new shouldBeInt();
    public static String render(int str) {
        StringBuilder sb = new StringBuilder();
        renderInto(sb,str);
        return sb.toString();
    }

     public static void renderInto(Appendable buffer,int str) {INSTANCE.renderImpl(buffer,str);}    public void renderImpl(Appendable buffer,int str) {
        try {
            buffer.append("\n        ");
            buffer.append(toS(str + 1));
            buffer.append("\n        ");
} catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
static class mySection extends bb.runtime.BaseBBTemplate {

private static mySection INSTANCE = new mySection();
    public static String render(String str) {
        StringBuilder sb = new StringBuilder();
        renderInto(sb,str);
        return sb.toString();
    }

     public static void renderInto(Appendable buffer,String str) {INSTANCE.renderImpl(buffer,str);}    public void renderImpl(Appendable buffer,String str) {
        try {
            buffer.append("\n            <h1> urmom</h1>\n        ");
} catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
static class shouldBeABoolean extends bb.runtime.BaseBBTemplate {

private static shouldBeABoolean INSTANCE = new shouldBeABoolean();
    public static String render(boolean blah) {
        StringBuilder sb = new StringBuilder();
        renderInto(sb,blah);
        return sb.toString();
    }

     public static void renderInto(Appendable buffer,boolean blah) {INSTANCE.renderImpl(buffer,blah);}    public void renderImpl(Appendable buffer,boolean blah) {
        try {
            buffer.append("\n    <h1>");
            buffer.append(toS(blah));
            buffer.append("</h1>\n    ");
} catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
    public static String render() {
        StringBuilder sb = new StringBuilder();
        renderInto(sb);
        return sb.toString();
    }

     public static void renderInto(Appendable buffer) {INSTANCE.renderImpl(buffer);}    public void renderImpl(Appendable buffer) {
        try {
            buffer.append("<!DOCTYPE html>\n<html lang=\"en\">\n    ");
            boolean blah = false;
            buffer.append("\n    ");
            if(blah) {
        int str = 0;
            buffer.append("\n        ");
            shouldBeInt.renderInto(buffer, str);
            buffer.append("\n    ");
            } else {
        String str = "i am a int str wow";
            buffer.append("\n        ");
            mySection.renderInto(buffer, str);
            buffer.append("\n    ");
            }
            buffer.append("\n    ");
            shouldBeABoolean.renderInto(buffer, blah);
            buffer.append("\n</html>\n");
} catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}