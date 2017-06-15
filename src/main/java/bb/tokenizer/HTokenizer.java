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
        ArrayList<Token> result = new ArrayList<Token>();
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
                    } else {
                        result.add(getStatementToken(state));
                    }
                } else {
                    result.add(getStringContentToken(state));
                }
            } else if (state.getCurr() == '$') {
                if (state.hasNext() && state.getNext() == '{') {  //is an expression
                    result.add(getExprToken(state));
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
    private Token getExprToken(State state) {
        final int FRONT_LEN = 2;
        final int END_LEN = 1;
        return makeToken(Token.TokenType.EXPRESSION, FRONT_LEN, END_LEN, state);
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
                advanceExpression(state);
                break;
        }

        state.endOfLastSEorD = state.copyCurrLoc();
        state.advance();
        return new Token(type, state.str.substring(start.pos + startLen, state.getPos() - endLen).trim(), start.line, start.col, start.pos);
    }

    private void advanceDirective(State state) {
        try {
            while (true) {
                state.adjustLoc();
                if (state.getPrev() == '%' && state.getCurr() == '>') {
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
    }

    private void advanceStatement(State state) {
        try {
            while (true) {
                state.adjustLoc();
                if (state.getPrev() == '%' && state.getCurr() == '>') {
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
    }

    private void advanceExpression(State state) {
        try {
            while (true) {
                state.adjustLoc();
                if (state.getCurr() == '}') {
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
    }



/*
        //start with the pos st the < in <%@, end with it at the > in the %>
    private Token getDirectiveToken(String str, State state) {
        final int FRONT_LEN = 3;
        final int END_LEN = 2;
        Location start = state.copyCurrLoc();
        for (int i = 0; i < FRONT_LEN; i++) {
            state.advance();
        }

        try {
            while (true) {
                state.adjustLoc();
                if (state.getPrev() == '%' && state.getCurr() == '>') {
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
        return new Token(Token.TokenType.DIRECTIVE, str.substring(start.pos + FRONT_LEN, state.getPos() - END_LEN).trim(), start.line, start.col, start.pos);
    }

    //start with the pos st the < in <%, end with it at the > in the %>
    private Token getStatementToken(String str, State state) {
        final int FRONT_LEN = 2;
        final int END_LEN = 2;
        Location start = state.copyCurrLoc();
        for (int i = 0; i < FRONT_LEN; i++) {
            state.advance();
        }

        try {
            while (true) {
                state.adjustLoc();
                if (state.getPrev() == '%' && state.getCurr() == '>') {
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
        return new Token(Token.TokenType.STATEMENT, str.substring(start.pos + FRONT_LEN, state.getPos() - END_LEN).trim(), start.line, start.col, start.pos);
    }

    //start with the pos at $ from the ${, end with it at the }
    private Token getExprToken(String str, State state) {
        final int FRONT_LEN = 2;
        final int END_LEN = 1;
        Location start = state.copyCurrLoc();
        for (int i = 0; i < FRONT_LEN; i++) {
            state.advance();
        }

        try {
            while (true) {
                state.adjustLoc();
                if (state.getCurr() == '}') {
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
        return new Token(Token.TokenType.EXPRESSION, str.substring(start.pos + FRONT_LEN, state.getPos() - END_LEN).trim(), start.line, start.col, start.pos);
    }

    */

}
