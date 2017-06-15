package bb.tokenizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class HTokenizer implements ITokenizer {
    private class Location {
        int line;
        int col;
        int pos;

        Location() {
            line = 1;
            col = 1;
            pos = 0;
        }

        Location(int line, int col, int pos) {
            this.line = line;
            this.col = col;
            this.pos = pos;
        }
    }

    private class State {
        char[] chars;
        Location curr = new Location();
        Location endOfLastSEorD = new Location(1, 0, -1);
        int lastLineLen;

        State(char[] chars) {
            this.chars = chars;
        }

        int getPos() {
            return curr.pos;
        }

        char getCurr() {
            return chars[curr.pos];
        }
        char getNext() {
            return chars[curr.pos + 1];
        }
        char getNextNext() {
            return chars[curr.pos + 2];
        }
        char getPrev() {
            return chars[curr.pos - 1];
        }

        int getPosLastSEorD() {
            return endOfLastSEorD.pos;
        }
        int getColLastSEorD() {
            return endOfLastSEorD.col;
        }
        int getLineLastSEorD() {
            return endOfLastSEorD.line;
        }

        boolean hasNext() {
            return (curr.pos + 1 < chars.length);
        }
        boolean hasNextNext() {
            return (curr.pos + 2 < chars.length);
        }

        void advance() {
            curr.pos++;
            curr.col++;
        }
        void retreat() {
            curr.pos--;
            curr.col--;
            if (curr.col == 0) {
                curr.col = lastLineLen;
                curr.line--;
            }
        }

        void adjustLoc() {
            if (Character.isWhitespace(this.getCurr())) {
                if (this.getCurr() == '\n') {
                    lastLineLen = curr.col + 1;
                    curr.line++;
                    curr.col = 0;
                }
                this.advance();
                adjustLoc();
            }
        }
        Location copyCurrLoc() {
            return new Location(curr.line, curr.col, curr.pos);
        }

        void passQuotes() {
            Stack<Character> quotes = new Stack<Character>();

            if (this.getCurr() == '"') {
                quotes.push('"');
            } else if (this.getCurr() == '\''){
                quotes.push('\'');
            }
            advance();

            while (!quotes.empty() && hasNext()) {
                adjustLoc();
                if (this.getCurr() == '"' && this.getPrev() != '\\') {
                    if (quotes.peek() == '"') {
                        quotes.pop();
                    } else {
                        quotes.push('"');
                    }
                } else if (this.getCurr() == '\''){
                    if (quotes.peek() == '\'') {
                        quotes.pop();
                    } else {
                        quotes.push('\'');
                    }
                }
                advance();
            }
        }

        boolean tokenOpenerPresent() {
            return (hasNext() && ((getCurr() == '<' && getNext() == '%') ||
                    (getCurr() == '$' && getNext() == '{')));
        }

    }


    public List<Token> tokenize(String str) {
        ArrayList<Token> result = new ArrayList<Token>();
        State state = new State(str.toCharArray());

        while (state.getPos() < state.chars.length) {
            state.adjustLoc();
            if (state.getCurr() == '<') {
                if (state.hasNext() && state.getNext() == '%') { //is a statement or directive
                    //@TODO: if there is no nextnext it is a(n incomplete) statement
                    if (state.hasNextNext() && state.getNextNext() == '@') {
                        result.add(getDirectiveToken(str, state));
                    } else {
                        result.add(getStatementToken(str, state));
                    }
                } else {
                    result.add(getStringContentToken(str, state));
                }
            } else if (state.getCurr() == '$') {
                if (state.hasNext() && state.getNext() == '{') {  //is an expression
                    result.add(getExprToken(str, state));
                } else {
                    result.add(getStringContentToken(str, state));
                }
            } else {  //is a string statement
                result.add(getStringContentToken(str, state));
            }
        }
        return result;
    }

    //start with the pos st the < in <%@, end with it at the > in the %>
    private Token getDirectiveToken(String str, State state) {
        final int FRONT_LEN = 3;
        final int END_LEN = 2;
        Location start = state.copyCurrLoc();
        int end;
        for (int i = 0; i < FRONT_LEN; i++) {
            state.advance();
        }

        try {
            while (true) {
                state.adjustLoc();
                if (state.getPrev() == '%' && state.getCurr() == '>') {
                    end = state.getPos();
                    break;
                } else if (state.tokenOpenerPresent()) {
                    throw new RuntimeException("Cannot start a new token inside a Directive");
                } else if ((state.getCurr() == '"' && state.getPrev() != '\\') || state.getCurr() == '\'') {
                    state.passQuotes();
                } else {
                    state.advance();
                }
            }
        } catch (IndexOutOfBoundsException e) {
            throw new RuntimeException("File ended before closing Directive");
        }
        state.endOfLastSEorD = state.copyCurrLoc();
        state.advance();
        return new Token(Token.TokenType.DIRECTIVE, str.substring(start.pos + FRONT_LEN, end - END_LEN + 1).trim(), start.line, start.col, start.pos);
    }

    //start with the pos st the < in <%, end with it at the > in the %>
    private Token getStatementToken(String str, State state) {
        final int FRONT_LEN = 2;
        final int END_LEN = 2;
        Location start = state.copyCurrLoc();
        int end;
        for (int i = 0; i < FRONT_LEN; i++) {
            state.advance();
        }

        try {
            while (true) {
                state.adjustLoc();
                if (state.getPrev() == '%' && state.getCurr() == '>') {
                    end = state.getPos();
                    break;
                } else if (state.tokenOpenerPresent()) {
                    throw new RuntimeException("Cannot start a new token inside a Statement");
                } else if ((state.getCurr() == '"' && state.getPrev() != '\\') || state.getCurr() == '\'') {
                    state.passQuotes();
                } else {
                    state.advance();
                }
            }
        } catch (IndexOutOfBoundsException e) {
            throw new RuntimeException("File ended before closing Statement");
        }
        state.endOfLastSEorD = state.copyCurrLoc();
        state.advance();
        return new Token(Token.TokenType.STATEMENT, str.substring(start.pos + FRONT_LEN, end - END_LEN + 1).trim(), start.line, start.col, start.pos);
    }

    //start with the pos at $ from the ${, end with it at the }
    private Token getExprToken(String str, State state) {
        final int FRONT_LEN = 2;
        final int END_LEN = 1;
        Location start = state.copyCurrLoc();
        int end;
        for (int i = 0; i < FRONT_LEN; i++) {
            state.advance();
        }

        try {
            while (true) {
                state.adjustLoc();
                if (state.getCurr() == '}') {
                    end = state.getPos();
                    break;
                } else if (state.tokenOpenerPresent()) {
                    throw new RuntimeException("Cannot start a new token inside an Expression");
                } else if ((state.getCurr() == '"' && state.getPrev() != '\\') || state.getCurr() == '\'') {
                    state.passQuotes();
                } else {
                    state.advance();
                }
            }
        } catch (IndexOutOfBoundsException e) {
            throw new RuntimeException("File ended before closing Expression");
        }
        state.endOfLastSEorD = state.copyCurrLoc();
        state.advance();
        return new Token(Token.TokenType.EXPRESSION, str.substring(start.pos + FRONT_LEN, end - END_LEN + 1).trim(), start.line, start.col, start.pos);
    }

    private Token getStringContentToken(String str, State state) {
        int end;
        while (state.getPos() < state.chars.length) {
            state.adjustLoc();
            if (state.tokenOpenerPresent()) {
                break;
            }
            state.advance();
        }
        end = state.getPos() - 1;
        return new Token(Token.TokenType.STRING_CONTENT, str.substring(state.getPosLastSEorD() + 1, end + 1),
                state.getLineLastSEorD(), state.getColLastSEorD() + 1, state.getPosLastSEorD() + 1);
    }

}
