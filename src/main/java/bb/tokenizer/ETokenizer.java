package bb.tokenizer;

import java.util.List;
import java.util.ArrayList;

public class ETokenizer implements ITokenizer {
    int line, column;

    public ETokenizer() {
        line = 0;
        column = 0;
    }
    public List<Token> tokenize(String str) {
        ArrayList<Token> tokens = new ArrayList<Token>();
        while(str.length() > 0) {
            str = addNextToken(str, tokens);
        }
        line = 0;
        column = 0;
        return tokens;
    }

    /** Creates the next token from the given String, and adds it to tokens. */
    private String addNextToken(String str, ArrayList<Token> tokens) {
        if (getNextTokenType(str).equals("STRING_CONTENT")) {
            return addStringContent(str, tokens);
        } else if (getNextTokenType(str).equals("STATEMENT")) {
            return addStatement(str, tokens);
        } else if (getNextTokenType(str).equals("EXPRESSION")) {
            return addExpression(str, tokens);
        } else if (getNextTokenType(str).equals("DIRECTIVE")) {
            return addDirective(str, tokens);
        }
        throw new RuntimeException("Error at line " + line + "and column " + column);
    }

    /** Returns the correct token type to be parsed. */
    private String getNextTokenType(String str) {
        if (str.length() >= 2) {
            if (str.charAt(0) == '<' && str.charAt(1) == '%') {
                return "STATEMENT";
            }
            if (str.charAt(0) == '$' && str.charAt(1) == '{') {
                return "EXPRESSION";
            }
        }
        return "STRING_CONTENT";
    }

    private String addStringContent(String str, ArrayList<Token> tokens) {
        int index = 0;
        int tokenStartCol = column;
        int tokenStartLine = line;
        Character current = null;
        Character previous;
        while (index < str.length() - 1) {
            previous = current;
            current = str.charAt(index);
            if ((current.equals('%') && previous.equals('<')) || (current.equals('{') && previous.equals('$'))) {
                Token currentToken = new Token(Token.TokenType.STRING_CONTENT, str.substring(0, index - 1), tokenStartLine, tokenStartCol, 0);
                tokens.add(currentToken);
                return str.substring(index - 1);
            }
            if (current == 12) {
                line += 1;
                column = 0;
            }
            index += 1;
            column += 1;
        }
        Token currentToken = new Token(Token.TokenType.STRING_CONTENT, str, tokenStartLine, tokenStartCol, 0);
        tokens.add(currentToken);
        return "";
    }

    private String addStatement(String str, ArrayList<Token> tokens) {
        int index = 0;
        int tokenStartCol = column;
        int tokenStartLine = line;
        Character current = null;
        Character previous;
        while (index < str.length() - 1) {
            previous = current;
            current = str.charAt(index);
            if (current.equals('>') && previous.equals('%')) {
                Token currentToken = new Token(Token.TokenType.STATEMENT, str.substring(0, index - 1), tokenStartLine, tokenStartCol, 0);
                tokens.add(currentToken);
                return str.substring(index + 1);
            }
            if (current == 12) {
                line += 1;
                column = 0;
            }
            index += 1;
            column += 1;
        }
        throw new RuntimeException("Error: Statement beginning at col " + tokenStartCol + " and line " + tokenStartLine + "is not closed");
    }

    private String addExpression(String str, ArrayList<Token> tokens) {
        int index = 0;
        int tokenStartCol = column;
        int tokenStartLine = line;
        Character current;
        while (index < str.length() - 1) {
            current = str.charAt(index);
            if (current.equals('}')) {
                Token currentToken = new Token(Token.TokenType.EXPRESSION, str.substring(0, index - 1), tokenStartLine, tokenStartCol, 0);
                tokens.add(currentToken);
                return str.substring(index + 1);
            }
            if (current == 12) {
                line += 1;
                column = 0;
            }
            index += 1;
            column += 1;
        }
        throw new RuntimeException("Error: Expression beginning at col " + tokenStartCol + " and line " + tokenStartLine + "is not closed");
    }

    private String addDirective(String str, ArrayList<Token> tokens) {
        return null;
    }
}
