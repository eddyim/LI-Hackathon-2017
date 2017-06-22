package bb.tokenizer;

import java.io.*;
import java.util.*;

public class ETemplateGen {
    private static final String additionalDirectory = "/bb/egen";
    private static final String bufferCallBeginning = "buffer.append(";

    public static void main(String[] args) {
        String inputDir = args[0];
        String outputDir = args[1] + additionalDirectory; //TODO: REMOVE THIS AFTER DONE, THIS IS TO ENSURE TESTING WORKS
        ITokenizer tokenizer = new ETokenizer();
        Map<File, String> files = new HashMap<File, String>();
        File startDirectory = new File(inputDir);
        scanDirectory(startDirectory, "", files);
        for (File f: files.keySet()) {
            try {
                String content = new Scanner(f).useDelimiter("\\Z").next();
                List<Token> tokens = tokenizer.tokenize(content);
                String relPath = files.get(f);
                StringBuilder s = new StringBuilder();
                s.append(getIntro(getFileName(f), outputDir + relPath));
                s.append(getRenderMethod());
                s.append(getToSMethod());
                s.append(getRenderIntoMethod(tokens));
                s.append("}");
                File toWrite = new File(parseOutputFile(f, outputDir, relPath));
                File directory = new File(outputDir + relPath);
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                BufferedWriter bw = new BufferedWriter(new FileWriter(toWrite.getAbsoluteFile()));
                bw.write(s.toString());
                bw.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    static String parseOutputFile(File f, String outputDir, String relativePath) {
        String path = outputDir + relativePath + "/" + getFileName(f);
        while (path.charAt(0) == '.' || path.charAt(0) == '/') {
            path = path.substring(1);
        }
        return path;
    }

    static String getFileName(File f) {
        String fileName = f.getName();
        fileName = fileName.substring(0, fileName.indexOf("bb")) + "java";
        return fileName;
    }
    /** Scans the directory for files that have an ending of .bb.*.
     *  Upon seeing a valid filename, will add to validFiles, with the value being
     *  the relative path from the initial directory. If a given file is a directory, the
     * method will recursively scan the directory.
     * @param directory the directory to search
     * @param relativePath in a recursive call, the file path relative to the initial directory
     * @param validFiles the set of validFiles that will be added to
     */
    static void scanDirectory(File directory, String relativePath, Map<File, String> validFiles) {
        File[] files = directory.listFiles();
        for (File file: files) {
            if (file.isDirectory()) {
                scanDirectory(file, "/" + file.getName(), validFiles);
            }
            else if (file.getName().matches(".*\\.bb\\..*")) {
                validFiles.put(file, relativePath);
            }
        }
    }

    static String getString(Token t) {
        String content = t.getContent();
        content = content.replaceAll("\r", "");
        content = content.replaceAll("\n", "\\\\n");
        content = content.replaceAll("\\\"", "\\\\\"");
        return bufferCallBeginning + "\"" + content + "\");\n";
    }

    static String getOtherTokens(Token t) {
        String content = t.getContent();
        content = content.replaceAll("\r", "");
        content = content.replaceAll("\n", "\\\\n");
        return bufferCallBeginning + "toS(" + content + "));\n";
    }

    static String evalStatement(Token t) {
        String content = t.getContent();
        content = content.replaceAll("\r", "");
        content = content.replaceAll("\n", "\\\\n");
        return content + "\n";
    }

    static String getIntro(String fileName, String filePath) {
        String s = "";
        s = s + getPackageStatement(filePath) + "\n";
        s = s + "import java.io.IOException;\n";
        String classStatement = "public class " + fileName.replace(".java", "") + " {\n";
        s = s + classStatement;

        return s;
    }

    static String getPackageStatement(String outputDir) {
        String fullPath = outputDir;
        if (fullPath.contains("java")) {
            int javaIndex = fullPath.indexOf("java");
            fullPath = fullPath.substring(0, javaIndex) + fullPath.substring(javaIndex + 5);
        }
        fullPath = fullPath.replaceAll("/", ".");
        while(fullPath.charAt(0) == '.') {
            fullPath = fullPath.substring(1);
        }
        return "package " + fullPath + ";";
    }

    private static String getRenderMethod() {
        return "    public static String render() {\n" +
                "        StringBuilder sb = new StringBuilder();\n" +
                "        renderInto(sb);\n" +
                "        return sb.toString();\n" +
                "    }\n\n";
    }

    private static String getToSMethod() {
        return "    private static String toS(Object o) {\n" +
                "        return o == null ? \"\" : o.toString();\n" +
                "    }\n\n";
    }

    static String getNextToken(Token t) {
        if (t.getType() == Token.TokenType.STRING_CONTENT) {
            return getString(t);
        }
        else if (t.getType() == Token.TokenType.STATEMENT) {
            return evalStatement(t);
        } else {
            return getOtherTokens(t);
        }
    }
    private static String getRenderIntoMethod(List<Token> tokens) {
        String toReturn = "    public static void renderInto(Appendable buffer) {\n" +
                "        try {\n";
        for (Token t: tokens) {
            toReturn = toReturn + getNextToken(t);
        }
        toReturn = toReturn + "} catch (IOException e) {\n" +
                "            throw new RuntimeException(e);\n" +
                "        }\n" +
                "    }\n";
        return toReturn;
    }
}