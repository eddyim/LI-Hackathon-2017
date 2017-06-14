package bb.tokenizer;

import java.util.ArrayList;
import java.util.List;

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
            }
        }
        Location copyCurrLoc() {
            return new Location(curr.line, curr.col, curr.pos);
        }

    }


    public List<Token> tokenize(String str) {
        ArrayList<Token> result = new ArrayList<Token>();
        State state = new State(str.toCharArray());

        while (state.getPos() < str.length()) {
            state.adjustLoc();
            if (state.getCurr() == '<') {
                //@TODO: if there is no next you need to make it a string content token
                if (state.getNext() == '%') { //is a statement
                    result.add(getStatementToken(str, state));
                } else {
                    result.add(getStringContentToken(str, state));
                }
            } else if (state.getCurr() == '$') {
                //@TODO: if there is no next you need to make it a string content token
                if (state.getNext() == '{') {  //is an expression
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



    //start with the pos after the <%, end with it at the > in the %>
    private Token getStatementToken(String str, State state) {
        Location start = state.copyCurrLoc();
        int end;

        //TODO: catch the error
        while (true) {
            state.adjustLoc();
            if (state.getPrev() == '%' && state.getCurr() == '>') {
                end = state.getPos();
                break;
            }
            state.advance();
        }
        state.endOfLastSEorD = state.copyCurrLoc();
        state.advance();
        return new Token(Token.TokenType.STATEMENT, str.substring(start.pos + 2, end - 1).trim(), start.line, start.col, start.pos);
    }

    //start with the pos at $ from the ${, end with it at the }
    private Token getExprToken(String str, State state) {
        Location start = state.copyCurrLoc();
        int end;

        //TODO: catch the error
        while (true) {
            state.adjustLoc();
            if (state.getCurr() == '}') {
                end = state.getPos();
                break;
            }
            state.advance();
        }
        state.endOfLastSEorD = state.copyCurrLoc();
        state.advance();
        return new Token(Token.TokenType.EXPRESSION, str.substring(start.pos + 2, end).trim(), start.line, start.col, start.pos);
    }

    private Token getStringContentToken(String str, State state) {
        int end;
        while (state.getPos() < state.chars.length) {
            state.adjustLoc();
            if ((state.getCurr() == '<' && state.getNext() == '%') ||
                    (state.getCurr() == '$' && state.getNext() == '{')) {
                break;
            }
            state.advance();
        }
        end = state.getPos() - 1;
        return new Token(Token.TokenType.STRING_CONTENT, str.substring(state.getPosLastSEorD() + 1, end + 1),
                state.getLineLastSEorD(), state.getColLastSEorD() + 1, state.getPosLastSEorD() + 1);
    }

}
