package bb.tokenizer;

import java.util.List;
import java.util.ArrayList;

public class ETokenizer implements ITokenizer {
    private int line, column;

    ETokenizer() {
        line = 1;
        column = 1;
    }


    public List<Token> tokenize(String str) {
        ArrayList<Token> tokens = new ArrayList<Token>();
        while(str.length() > 0) {
            str = addNextToken(str, tokens);
        }
        line = 1;
        column = 1;
        return tokens;
    }

    /** Creates the next token from the given String, and adds it to tokens. */
    private String addNextToken(String str, ArrayList<Token> tokens) {
        if (getNextTokenType(str).equals("STRING_CONTENT")) {
            return addStringContent(str, tokens);
        } else if (getNextTokenType(str).equals("STATEMENT")) {
            return addStatement(str.substring(2), tokens);
        } else if (getNextTokenType(str).equals("EXPRESSION")) {
            return addExpression(str.substring(2), tokens);
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

    /** Helper method: Given that the next token to add is a STRING_CONTENT,
     *  correctly processes the token, adds it to tokens, and returns a string
     *  with the token removed. */
    private String addStringContent(String str, ArrayList<Token> tokens) {
        int index = 0;
        int tokenStartCol = column;
        int tokenStartLine = line;
        Character current = null;
        Character previous;
        while (index < str.length() - 1) {
            previous = current;
            current = str.charAt(index);
            if ((current == '%' && previous == '<') || (current == '{' && previous == '$')) {
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

    /** Helper method: Given that the next token to add is a STATEMENT,
     *  correctly processes the token, adds it to tokens, and returns a string
     *  with the token removed.*/
    //TODO: Support quotations within statements
    //TODO: Support proper indexing so that position is correct
    private String addStatement(String str, ArrayList<Token> tokens) {
        int index = 0;
        int tokenStartCol = column;
        int tokenStartLine = line;
        Character current = null;
        Character previous;
        while (index < str.length() - 1) {
            previous = current;
            current = str.charAt(index);
            if (current == '>' && previous == '%') {
                Token currentToken = new Token(Token.TokenType.STATEMENT, str.substring(0, index - 1), tokenStartLine, tokenStartCol, 0);
                tokens.add(currentToken);
                return str.substring(index + 1);
            }
            if (current == '%' && previous == '<') {
                throw new RuntimeException("Attempted to open new statement within statement");
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

    /** Helper method: Given that the next token to add is an EXPRESSION,
     *  correctly processes the token, adds it to tokens, and returns a string
     *  with the token removed.
     *  */
    private String addExpression(String str, ArrayList<Token> tokens) {
        int index = 0;
        int tokenStartCol = column;
        int tokenStartLine = line;
        Character current = null;
        Character previous;
        int quotestate = 0;
        while (index < str.length()) {
            previous = current;
            current = str.charAt(index);
            if (current == '"') {
                if (quotestate == 1 && previous != '\\') {
                    quotestate = 0;
                } else if (quotestate == 0) {
                    quotestate = 1;
                }
            } else if (current == '\'') {
                if (quotestate == 2 && previous != '\\') {
                    quotestate = 0;
                } else if (quotestate == 0) {
                    quotestate = 2;
                }
            }
            else if (quotestate == 0) {
                if (current.equals('}')) {
                    Token currentToken = new Token(Token.TokenType.EXPRESSION, str.substring(0, index), tokenStartLine, tokenStartCol, 0);
                    tokens.add(currentToken);
                    return str.substring(index + 1);
                } else if (index < str.length() - 1 && current == '{' && previous == '$') {
                    throw new RuntimeException("Error: Attempted to open new expression within expression");
                }
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

    /* Trying to create a better tokenize, WIP
    public List<Token> tokenize2(String str) {
        ArrayList<Token> tokens = new ArrayList<Token>();
        int index = 0;
        int line = 1;
        int column = 1;
        int currentTokenIndex = index;
        int currentTokenLine = line;
        int currentTokenColumn = column;
        boolean changeState = false;
        String currentState = getNextTokenType(str);
        String previousState = null;
        while (index < str.length()) {
            while (currentState.equals("STRING_CONTENT")) {
                if (index < str.length() - 1) {
                    if (str.charAt(index) == '<' && str.charAt(index + 1) == '%') {
                        previousState = currentState;
                        currentState = "STATEMENT";
                        changeState = true;
                        index += 1;
                    }
                    if (str.charAt(index) == '%' && str.charAt(index + 1) == '{') {
                        previousState = currentState;
                        currentState = "EXPRESSION";
                        changeState = true;
                        index += 1;
                    }
                }
                if (str.charAt(index) == 12) {
                    line += 1;
                    column = 0;
                }
                index += 1;
                column += 1;
            }
            if (currentState.equals("STATEMENT")) {
                if (index < str.length() - 1) {
                    if (str.charAt(index) == '%' && str.charAt(index + 1) == '>') {
                        previousState = currentState;
                        currentState = getNextTokenType(str.substring(index + 2));
                        changeState = true;
                        index += 1;
                    }
                } else {
                    throw new RuntimeException("Error: Statement beginning at col " + currentTokenIndex + " and line " + currentTokenLine + "is not closed");
                }
            }
            if (changeState) {
                String currentToken = str.substring(currentTokenIndex, index);
                Token temp = new Token(parseType(previousState), currentToken, line, column, currentTokenIndex);
                tokens.add(temp);
                changeState = false;
            }

        }
        return tokens;
    }
    /** Given a string of the tokentype, returns the correct TokenType
    private Token.TokenType parseType(String str) {
        if (str.equals("STRING_CONTENT")) {
            return Token.TokenType.STRING_CONTENT;
        } else if (str.equals("STATEMENT")) {
            return Token.TokenType.STATEMENT;
        } else if (str.equals("EXPRESSION")) {
            return Token.TokenType.EXPRESSION;
        } else if (str.equals("DIRECTIVE")) {
            return Token.TokenType.DIRECTIVE;
        } else {
            throw new IllegalArgumentException("Type " + str + " is not valid.");
        }
    }
*/
}
