package bb.tokenizer;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static bb.tokenizer.Token.TokenType.*;

public class ETemplateGen {
    private static final String additionalDirectory = "/bb/egen";
    private static final String bufferCallBeginning = "            buffer.append(";
    private String outputPath;
    private String inputPath;

    /** This class takes in a list of tokens and generates the correct file. */
    class FileGenerator {
        File templateFile;
        private List<Token> tokens;
        private List<String> pastStatements;
        private Map<String, StringBuilder> content;
        String relativePath;
        boolean isSection;
        String name;

        FileGenerator(File f, String relativePath) {
            templateFile = f;
            pastStatements = new ArrayList<>();
            this.name = templateFile.getName();
            isSection = false;
            this.relativePath = relativePath;
            parseFile();
            this.content = new HashMap<>();
            content.put("renderIntoMethod", new StringBuilder());
            content.put("importStatement", new StringBuilder("import java.io.IOException;\n"));
            content.put("extendsKeyword", new StringBuilder());
            content.put("additionalParameters", new StringBuilder());
            content.put("additionalClasses", new StringBuilder());
        }

        FileGenerator(List<Token> tokens, List<String> pastStatements, String name, String params) {
            this.tokens = tokens;
            this.pastStatements = pastStatements;
            isSection = true;
            this.content = new HashMap<>();
            this.name = name;
            content.put("renderIntoMethod", new StringBuilder());
            content.put("importStatement", new StringBuilder(""));
            content.put("extendsKeyword", new StringBuilder());
            content.put("additionalParameters", new StringBuilder(params));
            content.put("additionalClasses", new StringBuilder());
        }

        Map<String, String> buildSection() {
            if (!isSection) {
                throw new RuntimeException("Cannot build section");
            }
            Map<String, String> toReturn = new HashMap<>();
            StringBuilder fileContents = new StringBuilder();
            handleTokens();
            fileContents.append(getIntro(this.name, "",
                    "", content.get("extendsKeyword").toString()));
            fileContents.append(getRenderMethod());
            fileContents.append(getToSMethod());
            fileContents.append(getRenderIntoMethod());
            fileContents.append(getRenderImplMethod());
            fileContents.append(content.get("additionalClasses"));
            fileContents.append("}\n");
            toReturn.put("sectionContent", fileContents.toString());
            toReturn.put("importStatement", content.get("importStatement").toString());
            return toReturn;
        }

        String buildFile() {
            StringBuilder fileContents = new StringBuilder();
            handleTokens();
            fileContents.append(getIntro(getFileName(templateFile), outputPath + relativePath,
                    content.get("importStatement").toString(), content.get("extendsKeyword").toString()));
            fileContents.append(getRenderMethod());
            fileContents.append(getToSMethod());
            fileContents.append(getRenderIntoMethod());
            fileContents.append(getRenderImplMethod());
            fileContents.append(content.get("additionalClasses"));
            fileContents.append("}");
            return fileContents.toString();
        }

        private Map<String, String> parseSectionDeclaration(String section) {
            Map<String, String> returnValues = new HashMap<>();
            if(section.contains("(")) {
                returnValues.put("params", inferArgumentTypes(section.substring(section.indexOf('(') + 1, section.indexOf(')'))));
                returnValues.put("name", section.substring(0, section.indexOf('(')).replaceAll("\\{", ""));
            } else {
                returnValues.put("name", section.replaceAll("\\{", ""));
            }
            return returnValues;
        }

        private String inferArgumentTypes(String arguments) {
            String[] individualArguments = arguments.split(",");
            StringBuilder completedString = new StringBuilder();
            for (String a: individualArguments) {
                if (a.trim().contains(" ")) {
                    completedString.append(a.trim());
                } else {
                    completedString.append(inferSingleArgumentType(a));
                }
            }
            return completedString.toString();
        }

        private String inferSingleArgumentType(String arg) {
            String pattern = "([a-zA-Z_$][a-zA-Z_$0-9]* " + //First Group: Matches Type arg format
                    arg + ")|(\".*[a-zA-Z_$][a-zA-Z_$0-9]* " + //Second & Third Group: Deals with matching within strings
                    arg + ".*\")|('.*[a-zA-Z_$][a-zA-Z_$0-9]* " +
                    arg + ".*')";
            Pattern argumentRegex = Pattern.compile(pattern);
            for(int i = pastStatements.size() - 1; i >= 0; i -= 1) {
                Matcher argumentMatcher = argumentRegex.matcher(pastStatements.get(i));
                String toReturn = null;
                while(argumentMatcher.find()) {
                    if(argumentMatcher.group(1) != null) {
                        toReturn = argumentMatcher.group(1);
                    }
                }
                if (toReturn != null) {
                    return toReturn;
                }
            }
            throw new RuntimeException("Type for argument can not be inferred: " + arg);
        }

        private void handleSection(Token t) {
            Token currentToken = this.tokens.remove(0);
            Map<String, String> parsedDeclaration = parseSectionDeclaration(t.getContent().substring(8));
            List<Token> sectionTokens = new ArrayList<>();
            int endSectionCount = 1;
            while(endSectionCount > 0) {
                if(currentToken.getType() == DIRECTIVE && getDirectiveType(currentToken) == DirectiveType.SECTION) {
                    endSectionCount += 1;
                } else if(currentToken.getType() == DIRECTIVE && getDirectiveType(currentToken) == DirectiveType.END_SECTION) {
                    endSectionCount -= 1;
                }
                sectionTokens.add(currentToken);
                currentToken = this.tokens.remove(0);
            }
            sectionTokens.remove(sectionTokens.size()-1);
            if (parsedDeclaration.containsKey("params")) {
                FileGenerator currentSection = new FileGenerator(sectionTokens, this.pastStatements, parsedDeclaration.get("name"), parsedDeclaration.get("params"));
                Map<String, String> section = currentSection.buildSection();
                content.get("additionalClasses").append(section.get("sectionContent"));
                content.get("importStatement").append(section.get("importStatement"));
                handleDirective(new Token(DIRECTIVE, "include " + parsedDeclaration.get("name") + "("
                        + turnIntoArguments(parsedDeclaration.get("params")) + ")",0,0,0));

            } else {
                FileGenerator currentSection = new FileGenerator(sectionTokens, this.pastStatements, parsedDeclaration.get("name"), "");
                Map<String, String> section = currentSection.buildSection();
                content.get("additionalClasses").append(section.get("sectionContent"));
                content.get("importStatement").append(section.get("importStatement"));
                handleDirective(new Token(DIRECTIVE, "include " + parsedDeclaration.get("name") ,0,0,0));
            }
        }

        private String getRenderIntoMethod() {
            String toReturn = "     public static void renderInto(Appendable buffer";
            if (content.get("additionalParameters").length() > 0) {
                toReturn = toReturn + "," + content.get("additionalParameters");
            }
            toReturn = toReturn + ") {" +
                    "INSTANCE.renderImpl(buffer";
            if (content.get("additionalParameters").length() > 0) {
                toReturn = toReturn + "," + turnIntoArguments(content.get("additionalParameters").toString());
            }
            toReturn = toReturn + ");}";
            return toReturn;
        }

        private String getRenderImplMethod() {
            String toReturn = "    public void renderImpl(Appendable buffer";
            if (content.get("additionalParameters").length() > 0) {
                toReturn = toReturn + "," + content.get("additionalParameters");
            }
            toReturn = toReturn + ") {\n";
            if (this.content.get("renderIntoMethod").length() > 0) {
                toReturn = toReturn + "        try {\n";
                toReturn = toReturn + content.get("renderIntoMethod");
                toReturn = toReturn + "} catch (Exception e) {\n" +
                        "            throw new RuntimeException(e);\n" +
                        "        }\n";
            }
            toReturn = toReturn + "    }\n";

            return toReturn;
        }

        private  String getRenderMethod() {
            String toReturn =  "    public static String render(" + content.get("additionalParameters")+ ") {\n" +
                    "        StringBuilder sb = new StringBuilder();\n" +
                    "        renderInto(sb";
            if (content.get("additionalParameters").length() > 0) {
                toReturn = toReturn + "," + turnIntoArguments(content.get("additionalParameters").toString());
            }
            toReturn = toReturn + ");\n" +
                    "        return sb.toString();\n" +
                    "    }\n\n";
            return toReturn;
        }

        private String turnIntoArguments(String parameters) {
            String arguments = "";
            String[] params = parameters.split(",");
            for(String parameter: params) {
                arguments = arguments + parameter.trim().split(" ")[1];
                arguments = arguments + ",";
            }
            return arguments.substring(0, arguments.length() - 1);
        }

        private String getToSMethod() {
            return "    private static String toS(Object o) {\n" +
                    "        return o == null ? \"\" : o.toString();\n" +
                    "    }\n\n";
        }

        private String getIntro(String fileName, String filePath, String importStatement, String extendsKeyword) {
            String s = "";
            if(!isSection) {
                s = s + getPackageStatement(filePath) + "\n";
            }
            s = s + importStatement;
            String classStatement = "class "+ fileName.replace(".java", "") + " " + extendsKeyword + " {\n";
            if (!isSection) {
                classStatement = "public " + classStatement;
            } else {
                classStatement = "static " + classStatement;
            }
            s = s + classStatement + "\n";
            s = s + "private static " + fileName.replace(".java", "") + " INSTANCE = new " + fileName.replace(".java", "") + "();\n";

            return s;
        }

        private String getPackageStatement(String outputDir) {
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

        private void parseFile() {
            try {
                Scanner contentScanner = new Scanner(templateFile).useDelimiter("\\Z");
                String content = null;
                if (contentScanner.hasNext()) {
                    content = contentScanner.next();
                }
                List<Token> tokens = new ETokenizer().tokenize(content);
                this.tokens = tokens;
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        void handleTokens() {
            while(!this.tokens.isEmpty())
                handleNextToken(this.tokens.remove(0));
        }

        private void handleNextToken(Token t) {
            if (t.getType() == STRING_CONTENT) {
                handleStringContent(t);
            } else if (t.getType() == STATEMENT) {
                handleStatement(t);
            } else if (t.getType() == EXPRESSION) {
                handleExpression(t);
            } else if (t.getType() == DIRECTIVE) {
                handleDirective(t);
            } else if (t.getType() == COMMENT) {
                // Do nothing for comments
            } else{
                throw new RuntimeException("Token Type " + t.getType() + " is not valid");
            }
        }

        private void handleDirective(Token t) {
            DirectiveType type = getDirectiveType(t);
            if (type == DirectiveType.IMPORT_STATEMENT) {
                this.content.get("importStatement").append(t.getContent() + ";\n");
            } else if (type == DirectiveType.EXTENDS) {
                this.content.get("extendsKeyword").append(t.getContent());
            } else if (type == DirectiveType.PARAM) {
                String parameterContent = cleanParameterContent(t.getContent());
                if (this.content.get("additionalParameters").length() > 0) {
                    throw new RuntimeException("Have already added parameters");
                }
                this.content.get("additionalParameters").append(parameterContent);
            } else if (type == DirectiveType.INCLUDE) {
                handleInclude(t);
            } else if (type == DirectiveType.SECTION) {
                handleSection(t);
            }
            else {
                throw new RuntimeException("Directive Type " + type + " is not valid");
            }
        }

        private void handleInclude(Token t) {
            if (t.getContent().contains("(")) {
                String[] includeContent = t.getContent().split("\\(");
                String templateName = includeContent[0].substring(8);
                if(includeContent[1].trim().equals(")")) {
                    handleStatement(new Token(EXPRESSION, templateName + ".renderInto(buffer" + includeContent[1] + ";",
                            0, 0, 0));
                } else {
                    handleStatement(new Token(EXPRESSION, templateName + ".renderInto(buffer, " + includeContent[1] + ";",
                            0, 0, 0));
                }
            } else {
                String templateName = t.getContent().substring(8);
                handleStatement(new Token(EXPRESSION, templateName + ".renderInto(buffer);", 0,0,0));
            }
        }
        private String cleanParameterContent(String s) {
            return s.substring(7, s.length() - 1);
        }

        /**
         * Given a token that is a directive, returns the correct type of directive that the token represents
         * @param token a token to parse
         * @return the DirectiveType of the particular directive
         * TODO: Change method so that it uses regex and matches more specifically
         */
        private DirectiveType getDirectiveType(Token token) {
            if (token.getContent().contains("import")) {
                return DirectiveType.IMPORT_STATEMENT;
            } else if (token.getContent().contains("extends")) {
                return DirectiveType.EXTENDS;
            } else if (token.getContent().contains("param")) {
                return DirectiveType.PARAM;
            } else if (token.getContent().contains("include")) {
                return DirectiveType.INCLUDE;
            } else if (token.getContent().contains("end section")) {
                return DirectiveType.END_SECTION;
            } else if (token.getContent().contains("section")) {
                return DirectiveType.SECTION;
            } else {
                    throw new RuntimeException("Invalid Directive");
                }
        }

        private void handleStringContent(Token t) {
            String content = t.getContent();
            StringBuilder renderInto = this.content.get("renderIntoMethod");
            content = content.replaceAll("\r", "");
            content = content.replaceAll("\n", "\\\\n");
            content = content.replaceAll("\"", "\\\\\"");
            renderInto.append(bufferCallBeginning);
            renderInto.append("\"");
            renderInto.append(content);
            renderInto.append("\");\n");
        }

        private void handleStatement(Token t) {
            String content = t.getContent();
            StringBuilder renderInto = this.content.get("renderIntoMethod");
            content = content.replaceAll("\r", "");
            renderInto.append("            " + content);
            pastStatements.add(content);
            renderInto.append("\n");
        }

        private void handleExpression(Token t) {
            String content = t.getContent();
            StringBuilder renderInto = this.content.get("renderIntoMethod");
            content = content.replaceAll("\r", "");
            content = content.replaceAll("\n", "\\\\n");
            renderInto.append(bufferCallBeginning);
            renderInto.append("toS(");
            renderInto.append(content);
            renderInto.append("));\n");
        }
    }

    public ETemplateGen(String inputPath, String outputPath) {
        this.outputPath = outputPath;
        this.inputPath = inputPath;
    }

    private void generateFiles() {
        Map<File, String> files = new HashMap<>();
        File startDirectory = new File(inputPath);
        scanDirectory(startDirectory, "", files);
        for (File f: files.keySet()) {
            String relPath = files.get(f);
            FileGenerator fileGen = new FileGenerator(f, relPath);
            try {
                File toWrite = new File(parseOutputFile(f, outputPath, relPath));
                File directory = new File(outputPath + relPath);
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                BufferedWriter bw = new BufferedWriter(new FileWriter(toWrite.getAbsoluteFile()));
                bw.write(fileGen.buildFile());
                bw.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    enum DirectiveType {
        IMPORT_STATEMENT,
        EXTENDS,
        PARAM,
        INCLUDE,
        SECTION,
        END_SECTION
    }

    public static void main(String[] args) {
        String inputDir = args[0];
        String outputDir = args[1] + additionalDirectory;
        ETemplateGen generator = new ETemplateGen(inputDir, outputDir);
        generator.generateFiles();
    }

    private static String parseOutputFile(File f, String outputDir, String relativePath) {
        String path = outputDir + relativePath + "/" + getFileName(f);
        while (path.charAt(0) == '.' || path.charAt(0) == '/') {
            path = path.substring(1);
        }
        return path;
    }


    private static String getFileName(File f) {
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
    private static void scanDirectory(File directory, String relativePath, Map<File, String> validFiles) {
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
}