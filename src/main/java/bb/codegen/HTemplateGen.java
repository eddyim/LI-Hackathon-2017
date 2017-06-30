package bb.codegen;

import bb.tokenizer.HTokenizer;
import bb.tokenizer.Token;

import java.io.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiPredicate;

import static bb.codegen.HTemplateGen.Directive.DirType.*;
import static bb.tokenizer.Token.TokenType.*;


public class HTemplateGen {
    private static final String BASE_CLASS_NAME = "extends bb.runtime.BaseBBTemplate";

    private static class fileTypeChecker implements BiPredicate {
        public boolean test(Object path, Object attr){
            String regexStr = ".*\\.bb\\..*";
            return path.toString().matches(regexStr);
        }
    }

    static class ClassInfo {
        ClassInfo outerClass;
        Map<Integer, ClassInfo> nestedClasses = new HashMap<>();
        String params = null;
        String[][] paramsList = null;
        String name;
        String superClass = BASE_CLASS_NAME;
        int startTokenPos;
        Integer endTokenPos;
        int depth;

        //only for the outermost class
        ClassInfo(Iterator<Directive> dirIterator, String name, Integer endTokenPos, boolean outermost) {
            assert(outermost);
            this.outerClass = null;
            this.name = name;
            this.startTokenPos = 0;
            this.endTokenPos = endTokenPos;
            this.depth = 0;

            fillClassInfo(dirIterator);
        }

        ClassInfo(ClassInfo outerClass, Iterator<Directive> dirIterator, String name, String params, String[][] paramList, int startTokenPos, int depth) {
            this.outerClass = outerClass;
            this.name = name;
            this.params = params;
            this.paramsList = paramList;
            this.startTokenPos = startTokenPos;
            this.depth = depth;

            fillClassInfo(dirIterator);
        }

        void fillClassInfo(Iterator<Directive> dirIterator) {
            boolean endSec = false;

            outerLoop:
            while (dirIterator.hasNext()) {
                Directive dir = dirIterator.next();
                switch (dir.dirType) {
                    case IMPORT:
                        break;
                    case INCLUDE:
                        break;
                    case EXTENDS:
                        if (superClass.equals(BASE_CLASS_NAME)) {
                            superClass = dir.className;
                        } else {
                            throw new RuntimeException("Invalid Extends Directive on line " + dir.token.getLine() + "class cannot extend 2 classes.");
                        }
                        break;
                    case PARAMS:
                        if (depth == 0) {
                            if (params == null) {
                                params = dir.params;
                                paramsList = dir.paramsList;
                            } else {
                                throw new RuntimeException("Invalid Params Directive on line " + dir.token.getLine() + "cannot have 2 Params Directives.");
                            }
                        } else {
                            throw new RuntimeException("Invalid Params Directive on line " + dir.token.getLine() + "cannot have Param Directive within section.");
                        }
                        break;
                    case SECTION:
                        addNestedClass(new ClassInfo(this, dirIterator, dir.className, dir.params, dir.paramsList, dir.tokenPos + 1, depth + 1));
                        break;
                    case END_SECTION:
                        if (endTokenPos == null) {
                            endTokenPos = dir.tokenPos;
                        } else {
                            throw new RuntimeException("End Section Directive without matching Section Directive on line " + endTokenPos + ".");
                        }
                        endSec = true;
                        break outerLoop;
                }
            }
            if (endSec == false) {
                if (depth == 0) {
                    assert(startTokenPos == 0);
                    //done with file
                } else {
                    throw new RuntimeException("File ended before " + name + "section ended.");
                }
            }
        }

        void addNestedClass(ClassInfo nestedClass) {
            nestedClasses.put(nestedClass.startTokenPos, nestedClass);
        }
    }

    static class Directive {
        int tokenPos;
        Token token;

        enum DirType {
            IMPORT,     //className
            EXTENDS,    //className
            PARAMS,     //           params, paramsList
            INCLUDE,     //className, params
            SECTION,    //className, params, paramsList
            END_SECTION//
            }

        DirType dirType;

        //import "[class_name]"
        //extends "[class_name]"
        //params ([paramType paramName], [paramType paramName],...)                  <---nothing stored for params or end section
        //include "[templateName]"([paramVal], [paramVal],...)
        //section "[sectionName]"([paramType paramName], [paramType paramName],...)
        //end section
        String className;

        //iff section, params, and include (empty string if params not given for include)
        String params;

        //iff section and params only (include doesn't need it broken down bc types aren't given)
        String[][] paramsList;

        Directive(int tokenPos, Token token, List<Token> tokens) {
            assert(token.getType() == DIRECTIVE);
            this.tokenPos = tokenPos;
            this.token = token;

            identifyType();
            fillVars(tokens);
        }

        private void identifyType() {
            String content = token.getContent();

            if (content.matches("import.*")) {
                dirType = IMPORT;
            } else if (content.matches("extends.*")) {
                dirType = EXTENDS;
            } else if (content.matches("params.*")) {
                dirType = PARAMS;
            } else if (content.matches("include.*")) {
                dirType = INCLUDE;
            } else if (content.matches("section.*")) {
                dirType = SECTION;
            } else if (content.trim().matches("end section")) {
                dirType = END_SECTION;
            } else {
                throw new RuntimeException("Unsupported Directive Type on Line " + token.getLine());
            }
        }

        private void fillVars(List<Token> tokens) {
            switch (dirType) {

                case IMPORT:
                    className = token.getContent().substring(6).trim();
                    break;
                case EXTENDS:
                    className = token.getContent().substring(7).trim();
                    break;
                case PARAMS:
                    String content = token.getContent().substring(6);
                    params = content.trim().substring(1, content.length() - 1);
                    paramsList = splitParamsList(params);
                    break;
                case INCLUDE:
                    String[] parts = token.getContent().substring(8).trim().split("\\(", 2);
                    className = parts[0];

                    if (parts.length == 2) {
                        String temp = parts[1].substring(0, parts[1].length() - 1).trim();
                        if (temp.length() > 0) {
                            params = temp;
                        }
                    }

                    break;
                case SECTION:
                    String[] temp = token.getContent().substring(7).trim().split("\\(", 2);
                    className = temp[0];

                    if (temp.length == 2) {
                        params = temp[1].substring(0, temp[1].length() - 1).trim();
                        paramsList = splitParamsList(params);
                        findParamTypes(paramsList, tokenPos, tokens);
                        params = makeParamsString(paramsList);
                    }
                    break;
                case END_SECTION:
                    break;
            }
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
            String withoutFileType = bbFile.toString().split(fileName + "\\.bb\\.")[0];
            //@TODO: \bb\hgen is temporary
            relativePath = "bb\\hgen" + withoutFileType.substring(inputDir.length(), withoutFileType.length() - 1);
            javaWholePath = outputDir + "\\" + relativePath + "\\" + fileName + ".java";

        }

    }
    private static class State {
        Name name;
        int tokenPos = 0;
        int classDepth = 0;
        List<Token> tokens;
        Iterator<Token> tokenIterator;
        StringBuilder header = new StringBuilder();

        State(Name name, List<Token> tokens) {
            this.name = name;
            this.tokens = tokens;
            tokenIterator = tokens.iterator();
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


    //given a trimmed string of variables,
    // returns a list with a string list per variable with the type and variable name (when both are given)
    // or just the name if both aren't given
    private static String[][] splitParamsList(String params) {
        params = params.replaceAll(" ,", ",").replace(", ", ",");
        String[] parameters = params.split(",");
        String[][] paramsList = new String[parameters.length][2];
        for (int i = 0; i < parameters.length; i++) {
            paramsList[i] = parameters[i].split(" ", 2);
        }
        return paramsList;
    }

    //given a list of 2 element String lists (0th elem is type and 1st elem is value), returns the string form
    //ex. [[String, str],[int,5]] returns "String str, int 5"
    private static String makeParamsString(String[][] paramsList) {
        String params = "" + paramsList[0][0] + " " + paramsList[0][1];
        for (int i = 1; i < paramsList.length; i++) {
            params += ", " + paramsList[i][0] + " " + paramsList[i][1];
        }
        return params;
    }


    //@TODO: seems resource heavy, should fix
    private static String findType(String name, int tokenPos, List<Token> tokens) {

        for (int i = tokenPos - 1; i >= 0; i--) {
            Token t = tokens.get(i);
            if (t.getType() == STATEMENT) {
                String[] content = t.getContent().split("\\s+");
                for (int j = content.length - 1; j >= 0; j--) {
                    if (content[j].matches(name + "(.*)")) {
                        if (content[j].length() == name.length() || content[j].charAt(name.length()) == ';') {
                            return content[j - 1];
                        }
                    }
                }
            }
        }
        throw new RuntimeException("variable " + name + " not found");
    }


    private static void findParamTypes(String[][] params, int tokenPos, List<Token> tokens) {
        for (int i = 0; i < params.length; i++) {
            if (params[i].length == 1) {
                    String name = params[i][0];
                    params[i] = new String[2];
                    params[i][0] = findType(name, tokenPos, tokens);
                    params[i][1] = name;
            }
        }
    }

    private static List<Directive> getDirectivesList(List<Token> tokens) {
        ArrayList<Directive> dirList = new ArrayList<>();

        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (token.getType() == DIRECTIVE) {
                dirList.add(new Directive(i, token, tokens));
            }
        }
        return dirList;
    }

    private static Map<Integer, Directive> getDirectivesMap(List<Directive> dirList) {
        Map<Integer, Directive> dirMap = new HashMap();
        for (Directive dir : dirList) {
            dirMap.put(dir.tokenPos, dir);

        }
        return dirMap;
    }

    private static void addImports(StringBuilder sb, List<Directive> dirList) {
        for (Directive dir: dirList) {
            if (dir.dirType == IMPORT) {
                sb.append("import " + dir.className + ";\n");
            }
        }
    }

    private static void addHeader(StringBuilder sb, ClassInfo classInfo) {
        if (classInfo.depth == 0) {
            sb.append("\npublic class " + classInfo.name + " " + classInfo.superClass + " {\n");
        } else {
            sb.append("\npublic static class " + classInfo.name + " " + classInfo.superClass + " {\n");
        }

        sb.append("\nprivate static " + classInfo.name + " INSTANCE = new " + classInfo.name + "();\n\n");


    }

    private static void addRenders(StringBuilder sb, String params, String[][] paramsList) {
        if (paramsList == null) {
            sb.append("\n" +
                    "    public static String render() {\n" +
                    "        StringBuilder sb = new StringBuilder();\n" +
                    "        renderInto(sb);\n" +
                    "        return sb.toString();\n" +
                    "    }\n\n");

            sb.append("    public static void renderInto(Appendable buffer) {\n" +
                    "        INSTANCE.renderImpl(buffer);\n" +
                    "    }\n");
            sb.append("    public void renderImpl(Appendable buffer) {\n");

        } else {
            if (params == null) {
                params = makeParamsString(paramsList);
            }
            sb.append("\n" +
                    "    public static String render(" + params + ") {\n" +
                    "        StringBuilder sb = new StringBuilder();\n" +
                    "        renderInto(sb");
            for (String[] p : paramsList) {
                sb.append(", " + p[1]);
            }
            sb.append(");\n" +
                    "        return sb.toString();\n" +
                    "    }\n\n");


            sb.append("    public static void renderInto(Appendable buffer, " + params + ") {\n" +
                    "        INSTANCE.renderImpl(buffer");
            for (String[] param: paramsList) {
                sb.append(", " + param[1]);
            }
            sb.append(");\n" +
                    "    }\n\n");
            sb.append("    public void renderImpl(Appendable buffer, " + params + ") {\n");
        }
    }

    private static void addInclude(StringBuilder sb, Directive dir) {
        assert(dir.dirType == INCLUDE);
        if (dir.params != null) {
            sb.append("            " + dir.className + ".renderInto(buffer, " + dir.params + ");\n");
        } else {
            sb.append("            " + dir.className + ".renderInto(buffer);\n");
        }
    }

    private static void makeClassContent(StringBuilder sb, ClassInfo classInfo, List<Token> tokens, Map<Integer, Directive> dirMap) {
        addHeader(sb, classInfo);
        addRenders(sb, classInfo.params, classInfo.paramsList);
        ArrayList<ClassInfo> nestedClasses = new ArrayList<>();
        boolean willAppend = false;

        for (int i = classInfo.startTokenPos; i < classInfo.endTokenPos; i++) {
            Token.TokenType tokenType = tokens.get(i).getType();
            if (tokenType == STRING_CONTENT || tokenType == EXPRESSION) {
                willAppend = true;
                sb.append("        try {\n");
                break;
            }
        }

        outerLoop:
        for (int i = classInfo.startTokenPos; i < classInfo.endTokenPos; i++) {
            Token token = tokens.get(i);
            switch (token.getType()) {
                case STRING_CONTENT:
                    sb.append("            buffer.append(\"" + token.getContent().replaceAll("\"", "\\\\\"").replaceAll("\r\n", "\\\\n") + "\");\n");
                    break;
                case STATEMENT:
                    sb.append("            " + token.getContent() + "\n");
                    break;
                case EXPRESSION:
                    sb.append("            buffer.append(toS(" + token.getContent() + "));\n");
                    break;
                case COMMENT:
                    break;
                case DIRECTIVE:
                    Directive dir = dirMap.get(i);
                    if (dir.dirType == SECTION) {
                        ClassInfo classToSkipOver = classInfo.nestedClasses.get(i + 1);
                        nestedClasses.add(classToSkipOver);
                        if (classToSkipOver == null) {
                            assert(classToSkipOver.depth == 0);
                        }
                        if (classToSkipOver.endTokenPos == null) {
                            assert(classToSkipOver.depth == 0);
                        }
                        i = classToSkipOver.endTokenPos + 1;
                    } else if (dir.dirType == END_SECTION) {
                        assert(i == classInfo.endTokenPos);
                        assert(classInfo.depth > 0);
                        break outerLoop;
                    } else if (dir.dirType == INCLUDE) {
                        addInclude(sb, dir);
                    }
                    break;
            }
        }
        //close the renderImpl
        if (willAppend) {
            sb.append("        } catch (IOException e) {\n" +
                    "            throw new RuntimeException(e);\n" +
                    "        }\n");
        }
         sb.append("    }\n");

        for (ClassInfo nested : nestedClasses) {
            makeClassContent(sb, nested, tokens, dirMap);
        }

        //close class
        sb.append("}\n");

    }

    public static String generateCode(String fullyQualifiedName, String source) {
        String[] parts = fullyQualifiedName.split("\\.");
        String className = parts[parts.length - 1];
        String packageName = parts[1];
        for (int i = 2; i < parts.length - 1; i++) {
            packageName += "." + parts[i];
        }
        HTokenizer tokenizer = new HTokenizer();
        List<Token> tokens = tokenizer.tokenize(source);
        StringBuilder sb = new StringBuilder();
        List<Directive> dirList = getDirectivesList(tokens);
        Map<Integer, Directive> dirMap = getDirectivesMap(dirList);
        ClassInfo currClass = new ClassInfo(dirList.iterator(), className, tokens.size(), true);


        sb.append("package " + packageName + ";\n\n");
        sb.append("import java.io.IOException;\n\n");
        addImports(sb, dirList);

        makeClassContent(sb, currClass, tokens, dirMap);

        return sb.toString();
    }


        //TODO: scan input token for all files with .bb.* ending and generate
        // a corresponding java file to the given output token, preserving the package
        // relative to the input token root, with a .render() static function that
        // renders the template

}
