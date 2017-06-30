//TODO: REMOVE SECTION CONTENT FROM STUFF
package bb.codegen;
import bb.tokenizer.ETokenizer;
import bb.tokenizer.Token;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static bb.tokenizer.Token.TokenType.*;

public class ETemplateGen implements ITemplateCodeGenerator {
    private static final String bufferCallBeginning = "            buffer.append(";

    /** This class takes in a list of tokens and generates the correct file. */
    class FileGenerator {
        private List<Token> tokens;
        private List<String> pastStatements;
        boolean isSection;
        private StringBuilder additionalParameters;
        private StringBuilder importStatements;
        String name;
        int index;


        FileGenerator(String fullyQualifiedName, String source) {
            tokens = new ETokenizer().tokenize(source);
            pastStatements = new ArrayList<>();
            this.name = fullyQualifiedName;
            isSection = false;
            additionalParameters = new StringBuilder();
            importStatements = new StringBuilder();
            index = 0;

        }

        FileGenerator(List<Token> tokens, List<String> pastStatements, String name, String params) {
            this.tokens = tokens;
            index = 0;
            this.pastStatements = pastStatements;
            isSection = true;
            this.name = name;
            additionalParameters = new StringBuilder(params);
            importStatements = new StringBuilder();
        }

        String buildSection(StringBuilder otherImports) {
            if (!isSection) {
                throw new RuntimeException("Cannot build section");
            }
            StringBuilder fileContents = new StringBuilder();
            StringBuilder renderImplContent = new StringBuilder();
            fileContents.append(getIntro(this.name, ""));
            handleTokens(renderImplContent);
            fileContents.append(getRenderMethod());
            fileContents.append(getRenderIntoMethod());
            fileContents.append(getRenderImplMethod(renderImplContent));
            fileContents.append("}\n");
            otherImports.append(importStatements);
            return fileContents.toString();
        }

        String buildFile() {
            StringBuilder fileContents = new StringBuilder();
            StringBuilder renderImplContent = new StringBuilder();
            fileContents.append(getIntro(name));
            handleTokens(renderImplContent);
            fileContents.append(getRenderMethod());
            fileContents.append(getRenderIntoMethod());
            fileContents.append(getRenderImplMethod(renderImplContent));
            fileContents.append("}");
            return fileContents.toString();
        }

        String buildLayout(String name, String packageStatement) {
            return "";
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

        private void handleSection(Token t, StringBuilder renderInto) {
            Map<String, String> parsedDeclaration = parseSectionDeclaration(t.getContent().substring(8));
            if (parsedDeclaration.containsKey("params")) {
                handleDirective(new Token(DIRECTIVE, "include " + parsedDeclaration.get("name") + "("
                        + turnIntoArguments(parsedDeclaration.get("params")) + ")",0,0,0), renderInto);

            } else {
                handleDirective(new Token(DIRECTIVE, "include " + parsedDeclaration.get("name") ,0,0,0), renderInto);
            }

        }

        private void handleSectionCreation(Token t, StringBuilder additionalClasses, StringBuilder importStatement) {
            Map<String, String> parsedDeclaration = parseSectionDeclaration(t.getContent().substring(8));
            List<Token> sectionTokens = new ArrayList<>();
            int endSectionCount = 1;
            index += 1;
            while(endSectionCount > 0) {
                Token currentToken = this.tokens.remove(index);
                if(currentToken.getType() == DIRECTIVE && getDirectiveType(currentToken) == DirectiveType.SECTION) {
                    endSectionCount += 1;
                } else if(currentToken.getType() == DIRECTIVE && getDirectiveType(currentToken) == DirectiveType.END_SECTION) {
                    endSectionCount -= 1;
                }
                sectionTokens.add(currentToken);
            }
            sectionTokens.remove(sectionTokens.size()-1);
            if (parsedDeclaration.containsKey("params")) {
                FileGenerator currentSection = new FileGenerator(sectionTokens, this.pastStatements, parsedDeclaration.get("name"), parsedDeclaration.get("params"));
                additionalClasses.append(currentSection.buildSection(importStatement));

            } else {
                FileGenerator currentSection = new FileGenerator(sectionTokens, this.pastStatements, parsedDeclaration.get("name"), "");
                additionalClasses.append(currentSection.buildSection(importStatement));
            }
        }

        private String getRenderIntoMethod() {
            StringBuilder toReturn = new StringBuilder("     public static void renderInto(Appendable buffer");
            if (additionalParameters.length() > 0) {
                toReturn.append(",").append(additionalParameters);
            }
                toReturn.append(") {").append("INSTANCE.renderImpl(buffer");
            if (additionalParameters.length() > 0) {
                toReturn.append(",").append(turnIntoArguments(additionalParameters.toString()));
            }
            toReturn.append(");}");
            return toReturn.toString();
        }

        private String getRenderImplMethod(StringBuilder content) {
            StringBuilder toReturn = new StringBuilder("    public void renderImpl(Appendable buffer");
            if (additionalParameters.length() > 0) {
                toReturn.append(",").append(additionalParameters);
            }
            toReturn.append(") {\n");
            if (content.length() > 0) {
                toReturn.append("        try {\n").append(content)
                        .append("} catch (Exception e) {\n").append("            throw new RuntimeException(e);\n").append("        }\n");
            }
            toReturn.append("    }\n");
            return toReturn.toString();
        }

        private String getRenderMethod() {
            String toReturn =  "    public static String render(" + additionalParameters + ") {\n" +
                    "        StringBuilder sb = new StringBuilder();\n" +
                    "        renderInto(sb";
            if (additionalParameters.length() > 0) {
                toReturn = toReturn + "," + turnIntoArguments(additionalParameters.toString());
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
            return "    public String toS(Object o) {\n" +
                    "        return o == null ? \"\" : o.toString();\n" +
                    "    }\n\n";
        }

        private String getIntro(String fullyQualifiedName) {
            String[] splitName = fullyQualifiedName.split("\\.");
            String packageStatement = "";
            for(int i = 0; i < splitName.length - 1; i += 1) {
                packageStatement = packageStatement + splitName[i] + ".";
            }
            return getIntro(splitName[splitName.length - 1], "package " + packageStatement.substring(0, packageStatement.length() - 1) + ";");
        }

        private String getIntro(String name, String packageStatement) {
            StringBuilder extendsKeyword = new StringBuilder();
            StringBuilder additionalClasses = new StringBuilder();
            StringBuilder intro = new StringBuilder();

            while(index < tokens.size()) {
                Token t = tokens.get(index);
                if(t.getType() == DIRECTIVE) {
                    if(getDirectiveType(t) == DirectiveType.IMPORT_STATEMENT) {
                        importStatements.append(t.getContent());
                        importStatements.append(";\n");
                    } else if(getDirectiveType(t) == DirectiveType.EXTENDS) {
                        if (extendsKeyword.length() > 0) {
                            throw new RuntimeException("Can't extend more than one class");
                        }
                        extendsKeyword.append(t.getContent());
                    } else if(getDirectiveType(t) == DirectiveType.PARAM) {
                        String parameterContent = cleanParameterContent(t.getContent());
                        if (additionalParameters.length() > 0) {
                            throw new RuntimeException("Have already added parameters");
                        }
                        additionalParameters.append(parameterContent);
                    } else if(getDirectiveType(t) == DirectiveType.SECTION) {
                        handleSectionCreation(t, additionalClasses, importStatements);
                    } else if(getDirectiveType(t) == DirectiveType.CREATE_LAYOUT) {

                    }
                } else if (t.getType() == STATEMENT) {
                    pastStatements.add(t.getContent());
                }
                index += 1;
            }
            if(extendsKeyword.length() == 0) {
                extendsKeyword.append("extends bb.runtime.BaseBBTemplate");
            }
            index = 0;
            if(!isSection) {
                intro.append(packageStatement).append("\n");
            }
            if (!isSection) {
                intro.append(importStatements);
            }
            String classStatement = "class "+ name + " " + extendsKeyword + " {\n";
            if (!isSection) {
                classStatement = "public " + classStatement;
            } else {
                classStatement = "static " + classStatement;
            }
            intro.append(classStatement).append("\n");
            intro.append("private static ").append(name).append(" INSTANCE = new ")
                    .append(name).append("();\n").append(additionalClasses);
            return intro.toString();
        }

        void handleTokens(StringBuilder renderInto) {
            while(index < tokens.size())
                handleNextToken(this.tokens.get(index++), renderInto);
        }

        private void handleNextToken(Token t, StringBuilder renderInto) {
            if (t.getType() == STRING_CONTENT) {
                handleStringContent(t, renderInto);
            } else if (t.getType() == STATEMENT) {
                handleStatement(t, renderInto);
            } else if (t.getType() == EXPRESSION) {
                handleExpression(t, renderInto);
            } else if (t.getType() == DIRECTIVE) {
                handleDirective(t, renderInto);
            } else if (t.getType() == COMMENT) {
                // Do nothing for comments
            } else{
                throw new RuntimeException("Token Type " + t.getType() + " is not valid");
            }
        }

        private void handleDirective(Token t, StringBuilder renderInto) {
            DirectiveType type = getDirectiveType(t);
            if (type == DirectiveType.IMPORT_STATEMENT || type == DirectiveType.EXTENDS || type == DirectiveType.PARAM || type == DirectiveType.END_SECTION) {
            } else if (type == DirectiveType.INCLUDE) {
                handleInclude(t, renderInto);
            } else if (type == DirectiveType.SECTION) {
                handleSection(t, renderInto);
            }
            else {
                throw new RuntimeException("Directive Type " + type + " is not valid");
            }
        }

        private void handleInclude(Token t, StringBuilder renderInto) {
            if (t.getContent().contains("(")) {
                String[] includeContent = t.getContent().split("\\(");
                String templateName = includeContent[0].substring(8);
                if(includeContent[1].trim().equals(")")) {
                    handleStatement(new Token(EXPRESSION, templateName + ".renderInto(buffer" + includeContent[1] + ";",
                            0, 0, 0), renderInto);
                } else {
                    handleStatement(new Token(EXPRESSION, templateName + ".renderInto(buffer, " + includeContent[1] + ";",
                            0, 0, 0), renderInto);
                }
            } else {
                String templateName = t.getContent().substring(8);
                handleStatement(new Token(EXPRESSION, templateName + ".renderInto(buffer);", 0,0,0), renderInto);
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
            } else if (token.getContent().equals("content")) {
                return DirectiveType.CREATE_LAYOUT;
            } else {
                    throw new RuntimeException("Invalid Directive");
                }
        }

        private void handleStringContent(Token t, StringBuilder renderInto) {
            String content = t.getContent();
            content = content.replaceAll("\r", "");
            content = content.replaceAll("\n", "\\\\n");
            content = content.replaceAll("\"", "\\\\\"");
            renderInto.append(bufferCallBeginning);
            renderInto.append("\"");
            renderInto.append(content);
            renderInto.append("\");\n");
        }

        private void handleStatement(Token t, StringBuilder renderInto) {
            String content = t.getContent();
            content = content.replaceAll("\r", "");
            renderInto.append("            " + content);
            pastStatements.add(content);
            renderInto.append("\n");
        }

        private void handleExpression(Token t, StringBuilder renderInto) {
            String content = t.getContent();
            content = content.replaceAll("\r", "");
            content = content.replaceAll("\n", "\\\\n");
            renderInto.append(bufferCallBeginning);
            renderInto.append("toS(");
            renderInto.append(content);
            renderInto.append("));\n");
        }
    }

    public String generateCode(String fullyQualifiedName, String source) {
        FileGenerator fileGen = new FileGenerator(fullyQualifiedName, source);
        return fileGen.buildFile();
    }

    enum DirectiveType {
        IMPORT_STATEMENT,
        EXTENDS,
        PARAM,
        INCLUDE,
        SECTION,
        END_SECTION,
        CREATE_LAYOUT
    }

}