package bb.tokenizer;

import java.io.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;


public class HTemplateGen {
    private static class fileTypeChecker implements BiPredicate {
        public boolean test(Object path, Object attr){
            String regexStr = ".*\\.bb\\..*";
            return path.toString().matches(regexStr);
        }
    }

    private static class Name {
        String inputDir;
        String outputDir;
        String fileName;
        String relativePath;
        String javaWholePath;


        Name(String inputDir, String outputDir, Path bbFile) {

            this.inputDir = inputDir;
            this.outputDir = outputDir;

            fileName = bbFile.toFile().getName().split("\\.bb\\.")[0];
//            String regexString = ".*" + fileName;
//            Pattern pat = Pattern.compile(regexString);
//            Matcher mat = pat.matcher(bbFile.toString());
//            mat.find();
//            String withoutFileType = mat.group(0);
            String withoutFileType = bbFile.toString().split(fileName + "\\.bb\\.")[0];
            //@TODO: \bb\hgen is temporary
            relativePath = "bb\\hgen" + withoutFileType.substring(inputDir.length(), withoutFileType.length() - 1);
            javaWholePath = outputDir  + "\\" + relativePath + "\\" + fileName + ".java";

        }

    }

//    //@TODO: \bb\hgen is temporary
//    private static String getNewFileName(String inputDir, String outputDir, String bbFileLoc) {
//        String regexString = "(.*\\.bb\\.)";
//        Pattern pat = Pattern.compile(regexString);
//        Matcher mat = pat.matcher(bbFileLoc);
//        mat.find();
//        String withoutFileType = mat.group(0);
//        String extra = withoutFileType.substring(inputDir.length(), withoutFileType.length() - 4);
//        return outputDir + "\\bb\\hgen" + extra + ".java";
//    }

    private static String makeJavaContent(Name name, String bbContent) {
        StringBuilder header = new StringBuilder();
        StringBuilder rest = new StringBuilder();
        String superClass = null;
        String params = null;
        String[][] paramsList  = null;

        //@TODO: can tokenize be static??
        HTokenizer tokenizer = new HTokenizer();
        List<Token> tokens = tokenizer.tokenize(bbContent);
        header.append("package " + name.relativePath.replaceAll("\\\\", ".") + ";\n\n");
        header.append("import java.io.IOException;\n\n");



        for (Token token : tokens) {
            switch (token.getType()) {
                case STRING_CONTENT:
                    rest.append("            buffer.append(\"" + token.getContent().replaceAll("\"", "\\\\\"").replaceAll("\r\n", "\\\\n") + "\");\n");
                    break;
                case STATEMENT:
                    rest.append("            " + token.getContent() + "\n");
                    break;
                case EXPRESSION:
                    rest.append("            buffer.append(toS(" + token.getContent() + "));\n");
                    break;
                case DIRECTIVE:
                    if (token.getContent().matches("import.*")) {
                        header.append(token.getContent() + ";\n");
                    } else if (token.getContent().matches("extends.*")) {
                        if (superClass == null) {
                            superClass = token.getContent();
                        } else {
                            throw new RuntimeException("Cannot extend 2 classes:" + superClass + " and " + token.getContent());
                        }
                    } else if (token.getContent().matches("params.*")) {
                        if (params == null) {
                            String content = token.getContent();
                            params = content = content.substring(7, content.length() - 1);
                            content = content.replaceAll(" ,", ",").replace(", ", ",");
                            String[] parameters = content.split(",");
                            paramsList = new String[parameters.length][2];
                            for (int i = 0; i < parameters.length; i++) {
                                paramsList[i] = parameters[i].split(" ");
                            }
                        } else {
                            throw new RuntimeException("Cannot have 2 params directives:" + params + " and " + token.getContent());
                        }
                    } else if (token.getContent().matches("include.*")) {
                        String content = token.getContent().substring(8);
                        String[] parts = content.split("\\(", 2);
                        if (parts.length == 1 || (parts.length == 2 && parts[1].equals("\\)"))) {
                            rest.append("            " + parts[0] + ".renderInto(buffer);\n");
                        } else {
                            rest.append("            " + parts[0] + ".renderInto(buffer, ");
                            rest.append(parts[1] + ";\n");
                        }
                    } else {
                        throw new RuntimeException("Unsupported Directive on line" + token.getLine() + ":" + token.getContent());
                    }
                    break;
            }
        }
        if (superClass == null) {
            header.append("\npublic class " + name.fileName + " {\n");
        } else {
            header.append("\npublic class " + name.fileName + " " + superClass + " {\n");
        }

        if (paramsList == null) {
            header.append("\n" +
                    "    public static String render() {\n" +
                    "        StringBuilder sb = new StringBuilder();\n" +
                    "        renderInto(sb);\n" +
                    "        return sb.toString();\n" +
                    "    }\n\n");
            header.append("    public static void renderInto(Appendable buffer) {\n" +
                    "        try {\n");
        } else {
            header.append("\n" +
                    "    public static String render(" + params + ") {\n" +
                    "        StringBuilder sb = new StringBuilder();\n" +
                    "        renderInto(sb");
            for (String[] p : paramsList) {
                header.append(", " + p[1]);
            }
            header.append(");\n" +
                    "        return sb.toString();\n" +
                    "    }\n\n");

            header.append("    public static void renderInto(Appendable buffer, " + params + ") {\n" +
                    "        try {\n");
        }


        rest.append("        } catch (IOException e) {\n" +
                "            throw new RuntimeException(e);\n" +
                "        }\n" +
                "    }\n");

        rest.append("\n" +
                "    private static String toS(Object o) {\n" +
                "        return o == null ? \"\" : o.toString();\n" +
                "    }\n" +
                "}");

        return header.append(rest).toString();
    }


    public static void main(String[] args) {
        String inputDir = args[0];
        String outputDir = args[1];

        Path root = Paths.get(inputDir);

        try {//@TODO: there is a max depth, which is problematic, actual sol can't be hacky like this...
            Object[] filesToConvert = Files.find(root, Integer.MAX_VALUE,  new fileTypeChecker()).toArray();
             for (Object p : filesToConvert){
                 Name name = new Name(inputDir, outputDir, (Path) p);

                 File writeTo = new File(name.javaWholePath);
                 if (!writeTo.getParentFile().exists()) {
                     writeTo.getParentFile().mkdirs();
                 }
                 if (writeTo.createNewFile()){
                     System.out.println("File is created!");
                 }else{
                     System.out.println("File already exists.");
                 }

                 //String content = new String(Files.readAllBytes(Paths.get(p.toString())));
                 String content = makeJavaContent(name, new String(Files.readAllBytes(Paths.get(p.toString()))));
                 FileWriter fw = null;
                 BufferedWriter bw = null;

                 try {
                     fw = new FileWriter(writeTo);
                     bw = new BufferedWriter(fw);
                     bw.write(content);
                 } catch (IOException e) {
                     e.printStackTrace();
                 } finally {
                     try {
                         if (bw != null) {
                             bw.close();
                         }
                         if (fw != null) {
                             fw.close();
                         }
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 }

//                 if (!writeTo.getParentFile().exists()) {
//                    writeTo.getParentFile().mkdirs();
//                }
//
//                try {
//                    PrintWriter writer = new PrintWriter(writeTo);
//                    writer.print("writing anything...");
//                    writer.close();
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
//
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("The given parameter is not a valid directory.");
        }





//        File root = new File(inputDir);
//
//        File[] filesToConvert = root.listFiles();
//        System.out.println(inputDir);
//
//        for (File f : filesToConvert) {
//            if (f.isDirectory()) {
//                HTemplateGen.main(new String[]{f.toString(), outputDir, Integer.toString(inputDir.length())});
//            } else if (f.toString().matches(".*\\.bb\\..*")) {
//                //@TODO: get rid of the .bb.* ending
//                String extra;
//                if (args.length == 3) {
//                    extra = f.toString().substring(Integer.parseInt(args[2]));
//                } else {
//                    extra = f.toString().substring(inputDir.length());
//                }
//                File writeTo = new File(outputDir + "/bb/hgen" + extra + ".java");
//                if (!writeTo.getParentFile().exists()) {
//                    writeTo.getParentFile().mkdirs();
//                }
//                try {
//                    PrintWriter writer = new PrintWriter(writeTo);
//                    writer.print("writing anything...");
//                    writer.close();
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
//                System.out.println(f.toString());
//
//            }
//        }




        //TODO: scan input dir for all files with .bb.* ending and generate
        // a corresponding java file to the given output dir, preserving the package
        // relative to the input dir root, with a .render() static function that
        // renders the template
    }
}
