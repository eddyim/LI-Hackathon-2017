//package bb.egen.demo;
//import org.junit.Test;
//
//import static junit.framework.TestCase.assertEquals;
//
///**
// * Created by eim on 6/27/2017.
// */
//public class ETemplateTest {
//    @Test
//    public void testNestedImports() {
//        String a = NestedImportTest.render();
//        a = a.replace("\n", "").replace("\r", "");
//        String b = "<!DOCTYPE html>" +
//                "<html lang=\"en\">" +
//                "<head>" +
//                "    <meta charset=\"UTF-8\">" +
//                "    <title>Nested Import Tests</title>" +
//                "</head>" +
//                "<body>" +
//                "    <h1>This will make sure that nested imports are handled correctly.</h1>" +
//                "    " +
//                "        " +
//                "        " +
//                "        <h2 style=\"font-size: 1\">Font size: 1</h2>" +
//                "        " +
//                "        <h2 style=\"font-size: 2\">Font size: 2</h2>" +
//                "        " +
//                "        <h2 style=\"font-size: 3\">Font size: 3</h2>" +
//                "        ";
//        assertEquals(a.trim(), b.trim());
//    }
//
//    @Test
//    public void testImportSingle() {
//        String a = ImportSingleTest.render();
//        a = a.replace("\n", "").replace("\r", "").trim();
//        String b = "<!DOCTYPE html>" +
//                "" +
//                "<html lang=\"en\">" +
//                "<head>" +
//                "    <meta charset=\"UTF-8\">" +
//                "    <title>Import Single Test</title>" +
//                "</head>" +
//                "<body>" +
//                "    <h1>I am going to import some stuff right now</h1>" +
//                "    <p>About to use the TreeMap </p>" +
//                "    " +
//                "    " +
//                "        <p> This is paragraph 0 </p>" +
//                "       " +
//                "        <p> This is paragraph 1 </p>" +
//                "       " +
//                "        <p> This is paragraph 2 </p>" +
//                "       " +
//                "        <p> This is paragraph 3 </p>" +
//                "       " +
//                "        <p> This is paragraph 4 </p>" +
//                "       " +
//                "        <p> This is paragraph 5 </p>" +
//                "       " +
//                "        <p> This is paragraph 6 </p>" +
//                "       " +
//                "        <p> This is paragraph 7 </p>" +
//                "       " +
//                "        <p> This is paragraph 8 </p>" +
//                "       " +
//                "        <p> This is paragraph 9 </p>" +
//                "       " +
//                "</body>" +
//                "</html>";
//        assertEquals(a, b);
//    }
//
//    @Test
//    public void testIncludeNestedSection() {
//        assertEquals(aIncludeNestedSectionTest.render(), NestedImportTest.mySection.render());
//    }
//}
