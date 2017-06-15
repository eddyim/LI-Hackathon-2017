package bb.tokenizer;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import bb.tokenizer.Token.TokenType;

import static bb.tokenizer.Token.TokenType.*;

public class ETokenizer implements ITokenizer {
    class TokenBuilder implements Iterator<Token> {
        String tokenString;
        int line, col;
        int index;

        TokenBuilder(String str) {
            this.tokenString = str;
            line = 1;
            col = 1;
            index = 0;
        }

        private Character peekBehind() {
            return peekBehind(1);
        }

        private Character peekBehind(int distance) {
            if (index - distance < 0) {
                return null;
            }
            return tokenString.charAt(index - distance);
        }

        private Character peekForward() {
            return peekForward(1);
        }

        private Character peekForward(int distance) {
            if (index + distance < tokenString.length()) {
                return tokenString.charAt(index + distance);
            }
            return null;
        }

        public boolean hasNext() {
            if (tokenString == null) {
                return false;
            }
            return index < tokenString.length();
        }

        public Token next() {
            if (index >= tokenString.length()) {
                throw new NoSuchElementException();
            }
            TokenType nextType = getNextTokenType();
            int pos = this.index;
            int col = this.col;
            int line = this.line;
            Token toReturn;
            if (nextType == STRING_CONTENT) {
                toReturn = next(nextType, false, line, col, pos,"<%", "${");
            } else if (nextType == STATEMENT) {
                advancePosition();
                advancePosition();
                toReturn = next(nextType, true, line, col, pos,"%>");
                advancePosition();
                advancePosition();
            } else if (nextType == EXPRESSION) {
                advancePosition();
                advancePosition();
                toReturn = next(nextType, true, line, col, pos,"}");
                advancePosition();

            } else if (nextType == DIRECTIVE) {
                advancePosition();
                advancePosition();
                advancePosition();
                toReturn = next(nextType, true, line, col, pos,"%>");
                advancePosition();
                advancePosition();
            } else {
                throw new RuntimeException("Error at line " + line + "and column " + col);
            }
            return toReturn;
        }

        public void remove() {
            throw new UnsupportedOperationException("remove");
        }


        private Token next(TokenType type, boolean quoteSensitive, int line, int col, int pos, String... terminateConditions) {
            int contentStartPos = index;
            int length = tokenString.length();
            List<Character> termStart = new ArrayList<Character>();
            for (String s: terminateConditions) {
                termStart.add(s.charAt(0));
            }
            int quoteState = 0;
            while (index < length) {
                char current = tokenString.charAt(index);
                if (current == '"' && quoteSensitive) {
                    if (quoteState == 1 && peekBehind() != '\\') {
                        quoteState = 0;
                    } else if (quoteState == 0) {
                        quoteState = 1;
                    }
                } else if (current == '\'') {
                    if (quoteState == 2 && peekBehind() != '\\') {
                        quoteState = 0;
                    } else if (quoteState == 0) {
                        quoteState = 2;
                    }
                } else if (quoteState == 0) {
                    if (termStart.contains(current)) {
                        if (checkIfTerminates(terminateConditions)) {
                            String currentTokenString = tokenString.substring(contentStartPos, index);
                            if (type != STRING_CONTENT) {
                                currentTokenString = currentTokenString.trim();
                            }
                            return new Token(type, currentTokenString, line, col, pos);
                        }
                    }
                    checkIllegalOpenings();
                }
                advancePosition();
            }
            if (type == STRING_CONTENT) {
                return new Token(type, tokenString.substring(contentStartPos), line, col, pos);
            }
            throw new RuntimeException("Error: " + type + " beginning at col " + col + " and line " + line + "is not closed");
        }

        private boolean checkIfTerminates(String[] terminateConditions) {
            for (String cond: terminateConditions) {
                boolean terminates = true;
                for (int i = 0; i < cond.length(); i += 1) {
                    Character c = peekForward(i);
                    if (c == null || cond.charAt(i) != c) {
                        terminates = false;
                    }
                }
                if (terminates) {
                    return true;
                }
            }
            return false;
        }


        private void checkIllegalOpenings() {
            if (tokenString.charAt(index) == '<' && peekForward() == '%') {
                throw new RuntimeException("Attempted to open new statement within statement");
            }
            if (tokenString.charAt(index) == '$' && peekForward() == '{') {
                throw new RuntimeException("Attempted to open new expression within statement");
            }
        }

        /** Returns the correct token type to be parsed. */
        private TokenType getNextTokenType() {
            Character next = peekForward();
            if (tokenString.charAt(index) == '<' && next == '%') {
                if (peekForward(2) != null && peekForward(2) == '@') {
                    return DIRECTIVE;
                }
                return STATEMENT;
            } else if (tokenString.charAt(index) == '$' && next == '{') {
                return EXPRESSION;
            } else {
                return STRING_CONTENT;
            }
        }

        private void advancePosition() {
            char current = tokenString.charAt(index);
            if (current == 10) {
                this.line += 1;
                this.col = 0;
            }
            this.col += 1;
            index += 1;
        }

    }

    /*private int line, column, position;

    ETokenizer() {
        line = 1;
        column = 1;
        position = 0;
    }*/


    public List<Token> tokenize(String str) {
        ArrayList<Token> tokens = new ArrayList<Token>();
        TokenBuilder builder = new TokenBuilder(str);
        while (builder.hasNext()) {
            tokens.add(builder.next());
        }
        return tokens;
    }

    /*public List<Token> tokenize(String str) {
        ArrayList<Token> tokens = new ArrayList<Token>();
        if (str == null) {
            return tokens;
        }
        while(str.length() > 0) {
            str = addNextToken(str, tokens);
        }
        line = 1;
        column = 1;
        position = 0;
        return tokens;
    }

    /** Creates the next token from the given String, and adds it to tokens.
    private String addNextToken(String str, ArrayList<Token> tokens) {
        TokenType nextToken = getNextTokenType(str);
        if (nextToken == STRING_CONTENT) {
            return addStringContent(str, tokens);
        } else if (nextToken == STATEMENT) {
            return addStatement(str.substring(2), tokens);
        } else if (nextToken == EXPRESSION) {
            return addExpression(str.substring(2), tokens);
        } else if (nextToken == DIRECTIVE) {
            return addDirective(str.substring(3), tokens);
        }
        throw new RuntimeException("Error at line " + line + "and column " + column);
    }

    /** Returns the correct token type to be parsed.
    private TokenType getNextTokenType(String str) {
        if (str.indexOf("<%@") == 0) {
            return DIRECTIVE;
        } else if (str.indexOf("<%") == 0) {
            return STATEMENT;
        } else if (str.indexOf("${") == 0) {
            return EXPRESSION;
        } else {
            return STRING_CONTENT;
        }
    }

    /** Helper method: Given that the next token to add is a STRING_CONTENT,
     *  correctly processes the token, adds it to tokens, and returns a string
     *  with the token removed.
     *
    private String addStringContent(String str, ArrayList<Token> tokens) {
        int index = 0;
        int tokenStartCol = column;
        int tokenStartLine = line;
        int tokenStartPos = position;
        Character current = null;
        Character previous;
        while (index < str.length()) {
            previous = current;
            current = str.charAt(index);
            if (previous != null && ((current == '%' && previous == '<') || (current == '{' && previous == '$'))) {
                Token currentToken = new Token(STRING_CONTENT, str.substring(0, index - 1), tokenStartLine, tokenStartCol, tokenStartPos);
                tokens.add(currentToken);
                return str.substring(index - 1);
            }
            if (current == 10) {
                line += 1;
                column = 0;
            }
            index += 1;
            position += 1;
            column += 1;
        }
        Token currentToken = new Token(STRING_CONTENT, str, tokenStartLine, tokenStartCol, 0);
        tokens.add(currentToken);
        return "";
    }

    /** Helper method: Given that the next token to add is a STATEMENT,
     *  correctly processes the token, adds it to tokens, and returns a string
     *  with the token removed.
    private String addStatement(String str, ArrayList<Token> tokens) {
        int index = 0;
        int tokenStartCol = column;
        int tokenStartLine = line;
        int tokenStartPos = position;
        Character current = null;
        Character previous;
        int quoteState = 0;
        while (index < str.length()) {
            previous = current;
            current = str.charAt(index);
            if (current == '"') {
                if (quoteState == 1 && previous != '\\') {
                    quoteState = 0;
                } else if (quoteState == 0) {
                    quoteState = 1;
                }
            } else if (current == '\'') {
                if (quoteState == 2 && previous != '\\') {
                    quoteState = 0;
                } else if (quoteState == 0) {
                    quoteState = 2;
                }
            } else if (quoteState == 0 && previous != null) {
                if (current == '>' && '%' == previous ) {
                    Token currentToken = new Token(STATEMENT, str.substring(0, index - 1).trim(), tokenStartLine, tokenStartCol, tokenStartPos);
                    tokens.add(currentToken);
                    return str.substring(index + 1);
                }
                if (current == '%' && previous == '<') {
                    throw new RuntimeException("Attempted to open new statement within statement");
                }
                if (current == '{' && previous == '$') {
                    throw new RuntimeException("Attempted to open new expression within statement");
                }
            }
            advancePosition(current);
            index += 1;        }
        throw new RuntimeException("Error: Statement beginning at col " + tokenStartCol + " and line " + tokenStartLine + "is not closed");
    }

    /** Helper method: Given that the next token to add is an EXPRESSION,
     *  correctly processes the token, adds it to tokens, and returns a string
     *  with the token removed.
     *
    private String addExpression(String str, ArrayList<Token> tokens) {
        int index = 0;
        int tokenStartCol = column;
        int tokenStartLine = line;
        int tokenStartPos = position;
        Character current = null;
        Character previous;
        int quoteState = 0;
        while (index < str.length()) {
            previous = current;
            current = str.charAt(index);
            if (current == '"') {
                if (quoteState == 1 && previous != '\\') {
                    quoteState = 0;
                } else if (quoteState == 0) {
                    quoteState = 1;
                }
            } else if (current == '\'') {
                if (quoteState == 2 && previous != '\\') {
                    quoteState = 0;
                } else if (quoteState == 0) {
                    quoteState = 2;
                }
            }
            else if (quoteState == 0) {
                if (current.equals('}')) {
                    Token currentToken = new Token(EXPRESSION, str.substring(0, index).trim(), tokenStartLine, tokenStartCol, tokenStartPos);
                    tokens.add(currentToken);
                    return str.substring(index + 1);
                } else if (previous != null) {
                    if (current == '{' && previous == '$') {
                        throw new RuntimeException("Error: Attempted to open new expression within expression");
                    } else if (current == '%' && previous == '<') {
                        throw new RuntimeException("Attempted to open new statement within statement");
                    }
                }
            }
            advancePosition(current);
            index += 1;        }
        throw new RuntimeException("Error: Expression beginning at col " + tokenStartCol + " and line " + tokenStartLine + "is not closed");
    }

    private String addDirective(String str, ArrayList<Token> tokens) {
        int index = 0;
        int tokenStartCol = column;
        int tokenStartLine = line;
        int tokenStartPos = position;
        Character current = null;
        Character previous;
        int quoteState = 0;
        while (index < str.length()) {
            previous = current;
            current = str.charAt(index);
            if (current == '"') {
                if (quoteState == 1 && previous != '\\') {
                    quoteState = 0;
                } else if (quoteState == 0) {
                    quoteState = 1;
                }
            } else if (current == '\'') {
                if (quoteState == 2 && previous != '\\') {
                    quoteState = 0;
                } else if (quoteState == 0) {
                    quoteState = 2;
                }
            } else if (quoteState == 0 && previous != null) {
                if (current == '>' && previous == '%') {
                    Token currentToken = new Token(DIRECTIVE, str.substring(0, index - 1).trim(), tokenStartLine, tokenStartCol, tokenStartPos);
                    tokens.add(currentToken);
                    return str.substring(index + 1);
                }
                if (current == '%' && previous == '<') {
                    throw new RuntimeException("Attempted to open new statement within directive");
                }
                if (current == '{' && previous == '$') {
                    throw new RuntimeException("Attempted to open new expression within directive");
                }
            }
            advancePosition(current);
            index += 1;
        }
        throw new RuntimeException("Error: Directive beginning at col " + tokenStartCol + " and line " + tokenStartLine + "is not closed");
    }

    private void advancePosition(char current) {
        if (current == 10) {
            line += 1;
            column = 0;
        }
        position += 1;
        column += 1;
    }*/
}
