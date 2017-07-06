package bb.egen.demo;
import java.util.TreeSet;
public class ImportSingleTest extends bb.runtime.BaseBBTemplate {

private static ImportSingleTest INSTANCE = new ImportSingleTest();
    public static String render() {
        StringBuilder sb = new StringBuilder();
        renderInto(sb);
        return sb.toString();
    }

     public static void renderInto(Appendable buffer) {INSTANCE.renderImpl(buffer);}    public void renderImpl(Appendable buffer) {
        try {
            buffer.append("<!DOCTYPE html>\n");
            buffer.append("\n<html lang=\"en\">\n<head>\n    <meta charset=\"UTF-8\">\n    <title>Import Single Test</title>\n</head>\n<body>\n    <h1>I am going to import some stuff right now</h1>\n    <p>About to use the TreeMap </p>\n    ");
            TreeSet<Integer> myTreeSet = new TreeSet<>();
            buffer.append("\n    ");
            for (int i = 0; i < 10; i += 1) {
        myTreeSet.add(i);
    }
    for(int a: myTreeSet) {
            buffer.append("\n        <p> This is paragraph ");
            buffer.append(toS(a));
            buffer.append(" </p>\n       ");
            }
            buffer.append("\n</body>\n</html>");
} catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}