package bb.codegen;

import bb.tokenizer.BBTokenizer;
import bb.tokenizer.Token;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static bb.codegen.BBTemplateGen.Directive.DirType.*;
import static bb.tokenizer.Token.TokenType.*;

public class BBTemplateGen {
    private final String BASE_CLASS_NAME = "bb.runtime.BaseBBTemplate";
    private final String LAYOUT_INTERFACE = "bb.runtime.ILayout";

    class ClassInfo {
        Map<Integer, ClassInfo> nestedClasses = new HashMap<>();
        String params = null;
        String[][] paramsList = null;
        String name;
        String superClass = BASE_CLASS_NAME;
        int startTokenPos;
        Integer endTokenPos;
        int depth;
        boolean isLayout = false;
        boolean hasLayout = false;
        Directive layoutDir;
        int contentPos;

        //only for the outermost class
        ClassInfo(Iterator<Directive> dirIterator, String name, Integer endTokenPos, boolean outermost) {
            assert(outermost);
            this.name = name;
            this.startTokenPos = 0;
            this.endTokenPos = endTokenPos;
            this.depth = 0;

            fillClassInfo(dirIterator);
        }

        ClassInfo(Iterator<Directive> dirIterator, String name, String params, String[][] paramList, int startTokenPos, int depth, String superClass) {
            this.name = name;
            this.params = params;
            this.paramsList = paramList;
            this.startTokenPos = startTokenPos;
            this.depth = depth;
            this.superClass = superClass;

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
                        if (depth == 0) {
                            if (superClass.equals(BASE_CLASS_NAME)) {
                                superClass = dir.className;
                            } else {
                                throw new RuntimeException("Invalid Extends Directive on line " + dir.token.getLine() + "class cannot extend 2 classes.");
                            }
                        } else {
                            throw new RuntimeException("Invalid Extends Directive inside a section on line " + dir.token.getLine() + ".");
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
                        addNestedClass(new ClassInfo(dirIterator, dir.className, dir.params, dir.paramsList, dir.tokenPos + 1, depth + 1, superClass));
                        break;
                    case END_SECTION:
                        if (endTokenPos == null) {
                            endTokenPos = dir.tokenPos;
                        } else {
                            throw new RuntimeException("End Section Directive without matching Section Directive on line " + endTokenPos + ".");
                        }
                        endSec = true;
                        break outerLoop;
                    case CONTENT:
                        if (isLayout) {
                            throw new RuntimeException("Second Content Directive appears on line " + dir.token.getLine());
                        } else if (depth > 0) {
                            throw new RuntimeException("Cannot have a Content Directive inside a section. Appears on line " + dir.token.getLine());
                        } else {
                            isLayout = true;
                            contentPos = dir.tokenPos;
                        }
                        break;
                    case LAYOUT:
                        if (hasLayout) {
                            throw new RuntimeException("Second Layout Directive appears on line " + dir.token.getLine());
                        } else if (depth > 0) {
                            throw new RuntimeException("Cannot have a Layout Directive inside a section. Appears on line " + dir.token.getLine());
                        } else {
                            hasLayout = true;
                            layoutDir = dir;
                        }
                        break;

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

        protected enum DirType {
            IMPORT,     //className
            EXTENDS,    //className
            PARAMS,     //           params, paramsList
            INCLUDE,    //className, params
            SECTION,    //className, params, paramsList
            END_SECTION,//
            CONTENT,    //
            LAYOUT      //className
        }

        Directive.DirType dirType;

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
            assert (token.getType() == DIRECTIVE);
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
            } else if (content.trim().matches("content")) {
                dirType = CONTENT;
            } else if (content.trim().matches("layout.*")) {
                dirType = LAYOUT;
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
                    className = temp[0].trim();
                    if (temp.length == 2 && !temp[1].equals(")")) {
                        params = temp[1].substring(0, temp[1].length() - 1).trim();
                        paramsList = splitParamsList(params);
                        findParamTypes(paramsList, tokenPos, tokens);
                        params = makeParamsString(paramsList);
                    }
                    break;
                case END_SECTION:
                    break;
                case CONTENT:
                    break;
                case LAYOUT:
                    className = token.getContent().substring(6).trim();
                    break;
            }
        }

        /**
         * given a trimmed string of variables,
         * returns a list with a string list per variable with the type and variable name (when both are given)
         * or just the name if both aren't given
         */
        private String[][] splitParamsList(String params) {
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
        private String makeParamsString(String[][] paramsList) {
            String params = "" + paramsList[0][0] + " " + paramsList[0][1];
            for (int i = 1; i < paramsList.length; i++) {
                params += ", " + paramsList[i][0] + " " + paramsList[i][1];
            }
            return params;
        }

        private void findParamTypes(String[][] params, int tokenPos, List<Token> tokens) {
            for (int i = 0; i < params.length; i++) {
                if (params[i].length == 1) {
                    String name = params[i][0];
                    params[i] = new String[2];
                    params[i][0] = inferSingleArgumentType(name, tokenPos, tokens);
                    params[i][1] = name;
                }
            }
        }

        private String makeParamsStringWithoutTypes(String[][] paramsList) {
            String params = "" + paramsList[0][1];
            for (int i = 1; i < paramsList.length; i++) {
                params += ", " + paramsList[i][1];
            }
            return params;
        }

        private String inferSingleArgumentType(String name, int tokenPos, List<Token> tokens) {
            String pattern = "([a-zA-Z_$][a-zA-Z_$0-9]* " + //First Group: Matches Type arg format
                    name + ")|(\".*[a-zA-Z_$][a-zA-Z_$0-9]* " + //Second & Third Group: Deals with matching within strings
                    name + ".*\")|('.*[a-zA-Z_$][a-zA-Z_$0-9]* " +
                    name + ".*')";
            Pattern argumentRegex = Pattern.compile(pattern);
            for (int i = tokenPos - 1; i >= 0; i -= 1) {
                Token currentToken = tokens.get(i);
                if (currentToken.getType() == STATEMENT) {
                    Matcher argumentMatcher = argumentRegex.matcher(currentToken.getContent());
                    String toReturn = null;
                    while (argumentMatcher.find()) {
                        if (argumentMatcher.group(1) != null) {
                            toReturn = argumentMatcher.group(1);
                        }
                    }
                    if (toReturn != null) {
                        return toReturn.split(" ")[0];
                    }
                }
            }
            throw new RuntimeException("Type for argument can not be inferred: " + name);
        }

    }

    class FileGenerator {
        private BBStringBuilder sb = new BBStringBuilder();
        private ClassInfo currClass;
        private List<Token> tokens;
        private Map<Integer, Directive> dirMap;

        private class BBStringBuilder {
            private final String INDENT = "    ";
            private StringBuilder sb = new StringBuilder();

            public BBStringBuilder append(String content) {
                for (int i = 0; i < currClass.depth; i++) {
                    sb.append(INDENT);
                }
                sb.append(content);
                return this;
            }

            public BBStringBuilder reAppend(String content) {
                sb.append(content);
                return this;
            }

            public String toString() {
                return sb.toString();
            }
        }


        public FileGenerator(String fullyQualifiedName, String source) {
            String[] parts = fullyQualifiedName.split("\\.");
            String className = parts[parts.length - 1];
            String packageName = parts[0];
            for (int i = 1; i < parts.length - 1; i++) {
                packageName += "." + parts[i];
            }
            BBTokenizer tokenizer = new BBTokenizer();
            this.tokens = tokenizer.tokenize(source);
            List<Directive> dirList = getDirectivesList(tokens);
            this.dirMap = getDirectivesMap(dirList);
            this.currClass = new ClassInfo(dirList.iterator(), className, tokens.size() - 1, true);
            buildFile(packageName, dirList);
        }

        public String getFileContents() {
            return sb.toString();
        }

        private void buildFile(String packageName, List<Directive> dirList) {
            sb.append("package ").reAppend(packageName + ";\n\n");
            sb.append("import java.io.IOException;\n\n");
            addImports(dirList);
            makeClassContent();
        }

        private boolean containsStringContentOrExpr(List<Token> tokens, int start, int end) {
            for (int i = start; i <= end; i++) {
                Token token = tokens.get(i);
                Token.TokenType tokenType = token.getType();
                if (tokenType == STRING_CONTENT || tokenType == EXPRESSION) {
                    return true;
                }
            }
            return false;
        }

        private void addRenderImpl() {
            if (currClass.paramsList == null) {
                sb.append("    public void renderImpl(Appendable buffer) {\n");
            } else {
                sb.append("    public void renderImpl(Appendable buffer, ").reAppend(currClass.params).reAppend(") {\n");
            }
            boolean needsToCatchIO = currClass.isLayout || currClass.hasLayout;
            if (!needsToCatchIO) {
                needsToCatchIO = containsStringContentOrExpr(tokens, currClass.startTokenPos, currClass.endTokenPos);
            }

            if (needsToCatchIO) {
                sb.append("        try {\n");
            }

            if (currClass.hasLayout) {
                sb.append("            ").reAppend(currClass.layoutDir.className).reAppend(".asLayout().header(buffer);\n");
            }

            if (currClass.isLayout) {
                sb.append("            INSTANCE.header(buffer);\n")
                        .append("            INSTANCE.footer(buffer);\n");
            } else {
                makeFuncContent(currClass.startTokenPos, currClass.endTokenPos);
            }

            if (currClass.hasLayout) {
                sb.append("            ").reAppend(currClass.layoutDir.className).reAppend(".asLayout().footer(buffer);\n");
            }

            if (needsToCatchIO) {
                sb.append("        } catch (IOException e) {\n")
                        .append("            throw new RuntimeException(e);\n")
                        .append("        }\n");
            }
            //close the renderImpl
            sb.append("    }\n\n");
        }

        private void addRender() {
            if (currClass.paramsList == null) {
                sb.append("\n")
                        .append("    public static String render() {\n")
                        .append("        StringBuilder sb = new StringBuilder();\n")
                        .append("        renderInto(sb);\n")
                        .append("        return sb.toString();\n")
                        .append("    }\n\n");
            } else {
                sb.append("\n")
                        .append("    public static String render(").reAppend(currClass.params + ") {\n")
                        .append("        StringBuilder sb = new StringBuilder();\n")
                        .append("        renderInto(sb");
                for (String[] p : currClass.paramsList) {
                    sb.reAppend(", ").reAppend(p[1]);
                }
                sb.reAppend(");\n")
                        .append("        return sb.toString();\n")
                        .append("    }\n\n");
            }
        }

        private void addHeader() {
            sb.append("\n");
            if (currClass.depth == 0) {
                if (currClass.isLayout) {
                    sb.append("public class ").reAppend(currClass.name).reAppend(" extends ").reAppend(currClass.superClass).reAppend(" implements ").reAppend(LAYOUT_INTERFACE).reAppend(" {\n");
                } else {
                    sb.append("public class ").reAppend(currClass.name).reAppend(" extends ").reAppend(currClass.superClass).reAppend(" {\n");
                }
            } else {
                sb.append("public static class ").reAppend(currClass.name).reAppend(" extends ").reAppend(currClass.superClass).reAppend(" {\n");
            }

            sb.append("    private static ").reAppend(currClass.name).reAppend(" INSTANCE = new ").reAppend(currClass.name).reAppend("();\n\n");

        }

        private void addRenderInto() {
            if (currClass.paramsList == null) {
                sb.append("    public static void renderInto(Appendable buffer) {\n")
                        .append("        INSTANCE.renderImpl(buffer);\n")
                        .append("    }\n\n");
            } else {
                sb.append("    public static void renderInto(Appendable buffer, ").reAppend(currClass.params).reAppend(") {\n")
                        .append("        INSTANCE.renderImpl(buffer");
                for (String[] param: currClass.paramsList) {
                    sb.reAppend(", ").reAppend(param[1]);
                }
                sb.reAppend(");\n")
                        .append("    }\n\n");
            }

        }

        private void makeClassContent() {
            addHeader();
            addRender();
            addRenderInto();
            addRenderImpl();
            if (currClass.isLayout) {
                addHeaderAndFooter();
            }

            for (ClassInfo nested : currClass.nestedClasses.values()) {
                currClass = nested;
                makeClassContent();
            }

            //close class
            sb.append("}\n");
        }

        private void addHeaderAndFooter() {
            sb.append("    static ").reAppend(LAYOUT_INTERFACE).reAppend(" asLayout() {\n")
                    .append("        return INSTANCE;\n")
                    .append("    }\n\n");
            sb.append("    @Override\n")
                    .append("    public void header(Appendable buffer) throws IOException {\n");
            assert(currClass.depth == 0);
            makeFuncContent(currClass.startTokenPos, currClass.contentPos);
            sb.append("    }\n");

            sb.append("    @Override\n")
                    .append("    public void footer(Appendable buffer) throws IOException {\n");
            makeFuncContent(currClass.contentPos, currClass.endTokenPos);
            sb.append("    }\n");
        }

        private List<Directive> getDirectivesList(List<Token> tokens) {
            ArrayList<Directive> dirList = new ArrayList<>();

            for (int i = 0; i < tokens.size(); i++) {
                Token token = tokens.get(i);
                if (token.getType() == DIRECTIVE) {
                    dirList.add(new Directive(i, token, tokens));
                }
            }
            return dirList;
        }

        private Map<Integer, Directive> getDirectivesMap(List<Directive> dirList) {
            Map<Integer, Directive> dirMap = new HashMap();
            for (Directive dir : dirList) {
                dirMap.put(dir.tokenPos, dir);
            }
            return dirMap;
        }

        private void addImports(List<Directive> dirList) {
            for (Directive dir: dirList) {
                if (dir.dirType == IMPORT) {
                    sb.append("import " + dir.className + ";\n");
                }
            }
        }

        private void makeFuncContent(int startPos, int endPos) {

            outerLoop:
            for (int i = startPos; i <= endPos; i++) {
                Token token = tokens.get(i);
                switch (token.getType()) {
                    case STRING_CONTENT:
                        sb.append("            buffer.append(\"").reAppend(token.getContent().replaceAll("\"", "\\\\\"").replaceAll("\r", "").replaceAll("\n", "\\\\n") + "\");\n");
                        break;
                    case STATEMENT:
                        sb.append("            ").reAppend(token.getContent()).reAppend("\n");
                        break;
                    case EXPRESSION:
                        sb.append("            buffer.append(toS(").reAppend(token.getContent()).reAppend("));\n");
                        break;
                    case COMMENT:
                        break;
                    case DIRECTIVE:
                        Directive dir = dirMap.get(i);
                        if (dir.dirType == SECTION) {
                            ClassInfo classToSkipOver = currClass.nestedClasses.get(i + 1);
                            i = classToSkipOver.endTokenPos;
                            addSection(dir);
                        } else if (dir.dirType == END_SECTION) {
                            assert(i == endPos);
                            assert(currClass.depth > 0);
                            break outerLoop;
                        } else if (dir.dirType == INCLUDE) {
                            addInclude(dir);
                        } else if (dir.dirType == CONTENT) {
                            assert(i == endPos);
                        }
                        break;
                }
            }

        }

        private void addInclude(Directive dir) {
            assert(dir.dirType == INCLUDE);
            if (dir.params != null) {
                sb.append("            ").reAppend(dir.className).reAppend(".renderInto(buffer, ").reAppend(dir.params).reAppend(");\n");
            } else {
                sb.append("            ").reAppend(dir.className).reAppend(".renderInto(buffer);\n");
            }
        }

        private void addSection(Directive dir) {
            assert(dir.dirType == SECTION);
            if (dir.params != null) {
                String paramsWithoutTypes = dir.makeParamsStringWithoutTypes(dir.paramsList);
                sb.append("            ").reAppend(dir.className).reAppend(".renderInto(buffer, ").reAppend(paramsWithoutTypes).reAppend(");\n");
            } else {
                sb.append("            ").reAppend(dir.className).reAppend(".renderInto(buffer);\n");
            }
        }

    }

    public String generateCode(String fullyQualifiedName, String source) {
        FileGenerator generator = new FileGenerator(fullyQualifiedName, source);
        return generator.getFileContents();
    }

}
