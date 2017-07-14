package bb.tokenizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class BBTokenizer {
    class Location {
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

    class State {

        String str;
        Location curr = new Location();
        Location endOfLastSEorD = new Location(1, 0, -1);
        int lastLineLen;

        State(String str) {
            this.str = str;
        }

        int getPos() {
            return curr.pos;
        }

        char getCurr() {
            return str.charAt(curr.pos);
        }
        char getNext() {
            return str.charAt(curr.pos + 1);
        }
        char getNextNext() {
            return str.charAt(curr.pos + 2);
        }
        char getNextNextNext() {
            return str.charAt(curr.pos + 3);
        }
        char getPrev() {
            return str.charAt(curr.pos - 1);
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

        boolean hasCurr() {
            return (curr.pos < str.length());
        }
        boolean hasNext() {
            return (curr.pos + 1 < str.length());
        }
        boolean hasNextNext() {
            return (curr.pos + 2 < str.length());
        }
        boolean hasNextNextNext() {
            return (curr.pos + 3 < str.length());
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
            if (this.hasCurr() && Character.isWhitespace(this.getCurr())) {
                if (this.getCurr() == '\n') {
                    lastLineLen = curr.col + 1;
                    curr.line++;
                    curr.col = 0;
                }
                this.advance();
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
        List<Token> result = new ArrayList<Token>();
        if (str == null) {
            return result;
        }
        State state = new State(str);

        while (state.hasCurr()) {
            state.adjustLoc();
            if (state.getCurr() == '<') {
                if (state.hasNext() && state.getNext() == '%') { //is a statement or directive
                    //@TODO: if there is no nextnext it is a(n incomplete) statement
                    if (state.hasNextNext() && state.getNextNext() == '@') {
                        result.add(getDirectiveToken(state));
                    } else if (state.hasNextNext() && state.getNextNext() == '=') {
                        result.add(getExprToken(state, 1));
                    } else if (state.hasNextNext() && state.getNextNext() == '-' && state.hasNextNextNext() && state.getNextNextNext() == '-') {
                        result.add(getCommentToken(state));
                    } else {
                        result.add(getStatementToken(state));
                    }
                } else {
                    result.add(getStringContentToken(state));
                }
            } else if (state.getCurr() == '$') {
                if (state.hasNext() && state.getNext() == '{') {  //is an expression
                    result.add(getExprToken(state, 0));
                } else {
                    result.add(getStringContentToken(state));
                }
            } else {  //is a string statement
                result.add(getStringContentToken(state));
            }
        }
        return result;
    }


    private Token getStringContentToken(State state) {
        while (state.hasCurr()) {
            state.adjustLoc();
            if (state.tokenOpenerPresent()) {
                break;
            }
            state.advance();
        }
        return new Token(Token.TokenType.STRING_CONTENT, state.str.substring(state.getPosLastSEorD() + 1, state.getPos()),
                state.getLineLastSEorD(), state.getColLastSEorD() + 1, state.getPosLastSEorD() + 1);
    }

    //start with the pos st the < in <%@, end with it at the > in the %>
    private Token getDirectiveToken(State state) {
        final int FRONT_LEN = 3;
        final int END_LEN = 2;
        return makeToken(Token.TokenType.DIRECTIVE, FRONT_LEN, END_LEN, state);
    }
    //start with the pos st the < in <%, end with it at the > in the %>
    private Token getStatementToken(State state) {
        final int FRONT_LEN = 2;
        final int END_LEN = 2;
        return makeToken(Token.TokenType.STATEMENT, FRONT_LEN, END_LEN, state);
    }
    //start with the pos at $ from the ${, end with it at the }
    private Token getExprToken(State state, int additionalLen) {
        final int FRONT_LEN = 2 + additionalLen;
        final int END_LEN = 1 + additionalLen;
        return makeToken(Token.TokenType.EXPRESSION, FRONT_LEN, END_LEN, state);
    }

    //start with the pos st the < in <%--, end with it at the > in the --%>
    private Token getCommentToken(State state) {
        final int FRONT_LEN = 4;
        final int END_LEN = 4;
        return makeToken(Token.TokenType.COMMENT, FRONT_LEN, END_LEN, state);
    }


    private Token makeToken(Token.TokenType type, int startLen, int endLen, State state) {
        Location start = state.copyCurrLoc();
        for (int i = 0; i < startLen; i++) {
            state.advance();
        }

        switch (type) {
            case DIRECTIVE:
                advanceDirective(state);
                break;
            case STATEMENT:
                advanceStatement(state);
                break;
            case EXPRESSION:
                advanceExpression(state, startLen);
                break;
            case COMMENT:
                advanceComment(state);
                break;
        }

        state.endOfLastSEorD = state.copyCurrLoc();
        state.advance();
        return new Token(type, state.str.substring(start.pos + startLen, state.getPos() - endLen).trim(), start.line, start.col, start.pos);
    }

    private void advanceDirective(State state) {
        boolean finished = false;
        while (state.hasCurr()) {
            state.adjustLoc();
            if (state.getPrev() == '%' && state.getCurr() == '>') {
                finished = true;
                break;
            } else if (state.tokenOpenerPresent()) {
                throw new RuntimeException("Cannot start a new token inside a Directive");
            } else if ((state.getCurr() == '"' && state.getPrev() != '\\') || state.getCurr() == '\'') {
                state.passQuotes();
            } else {
                state.advance();
            }
        } if (!finished) {
            throw new RuntimeException("File ended before closing Directive");
        }
    }

    private void advanceStatement(State state) {
        boolean finished = false;
        while (state.hasCurr()) {
            state.adjustLoc();
            if (state.getPrev() == '%' && state.getCurr() == '>') {
                finished = true;
                break;
            } else if (state.tokenOpenerPresent()) {
                throw new RuntimeException("Cannot start a new token inside a Statement");
            } else if ((state.getCurr() == '"' && state.getPrev() != '\\') || state.getCurr() == '\'') {
                state.passQuotes();
            } else {
                state.advance();
            }
        }
        if (!finished) {
            throw new RuntimeException("File ended before closing Statement");
        }
    }

    private void advanceExpression(State state, int startLen) {
        boolean finished = false;
        while (state.hasCurr()) {
            state.adjustLoc();
            if ((startLen == 2 && state.getCurr() == '}')
                    || (startLen == 3 && state.getPrev() == '%' && state.getCurr() == '>')) {
                finished = true;
                break;
            } else if (state.tokenOpenerPresent()) {
                throw new RuntimeException("Cannot start a new token inside an Expression");
            } else if ((state.getCurr() == '"' && state.getPrev() != '\\') || state.getCurr() == '\'') {
                state.passQuotes();
            } else {
                state.advance();
            }
        }
        if (!finished) {
            throw new RuntimeException("File ended before closing Expression");
        }
    }

    private void advanceComment(State state) {
        boolean finished = false;
        while (state.hasCurr()) {
            state.adjustLoc();
            if (state.getCurr() == '-' && state.hasNextNextNext() && state.getNext() == '-' && state.getNextNext() == '%'  && state.getNextNextNext() == '>') {
                state.advance();
                state.advance();
                state.advance();
                finished = true;
                break;
            } else {
                state.advance();
            }
        } if (!finished) {
            throw new RuntimeException("File ended before closing Comment");
        }
    }
}
