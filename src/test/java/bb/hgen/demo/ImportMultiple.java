package bb.hgen.demo;

import java.io.IOException;

import java.util.LinkedList;
import java.util.HashSet;
import java.util.TreeSet;

public class ImportMultiple extends bb.runtime.BaseBBTemplate {

private static ImportMultiple INSTANCE = new ImportMultiple();


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
            buffer.append("<!DOCTYPE html>\n");
            buffer.append("\n<html lang=\"en\">\n<head>\n    <meta charset=\"UTF-8\">\n    <title>Test for Multiple Imports</title>\n</head>\n<body>\n    <h1>This test will make sure that multiple imports in random places is valid</h1>\n    ");
            LinkedList<Integer> myLinkedList = new LinkedList<>();
       HashSet<LinkedList<Integer>> myHashSet = new HashSet<>();
            buffer.append("\n    ");
            buffer.append("\n    ");
            TreeSet<Integer> myTreeSet = new TreeSet<>();
       myLinkedList.add(5);
       myHashSet.add(myLinkedList);
       myTreeSet.add(100);
       for(Integer a: myTreeSet) {
            buffer.append("\n            <h1> ");
            buffer.append(toS(a));
            buffer.append(" </h1>\n        ");
            }
            buffer.append("\n\n</body>\n</html>\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
