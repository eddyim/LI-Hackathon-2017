package bb.tokenizer;

import java.util.ArrayList;
import java.util.List;

public class HTokenizer implements ITokenizer {
    private class location {
        int line = 1;
        int col = 1;
        int pos = 0;
        int lastLineLen;
        int endOfLastSEorD = 0;
        char[] chars;

        location(char[] chars) {
            this.chars = chars;
        }

        char getCurr() {
            return chars[pos];
        }

        char getNext() {
            return chars[pos + 1];
        }

        char getPrev() {
            return chars[pos - 1];
        }

        void advance() {
            pos++;
            col++;
        }

        void retreat() {
            pos--;
            col--;
            if (col == 0) {
                col = lastLineLen;
                line--;
            }
        }
    }

    public List<Token> tokenize(String str) {
        ArrayList<Token> result = new ArrayList<Token>();
        location loc = new location(str.toCharArray());

        while (loc.pos < str.length()) {
            if (loc.getCurr() == '<') {
                loc.advance();
                if (loc.getCurr() == '%') { //is a statement
                    loc.advance();
                    result.add(getStatementToken(str, loc));
                } else {
                    loc.retreat();
                    result.add(getStringContentToken(str, loc));
                }
            }

            else if (loc.getCurr() == '$') {
                loc.advance();
                if (loc.getCurr() == '{') {  //is an expression
                    loc.advance();
                    result.add(getExprToken(str, loc));
                } else {
                    loc.retreat();
                    result.add(getStringContentToken(str, loc));
                }
            }

            else if (!adjustLoc(loc)) {  //is a string statement
                result.add(getStringContentToken(str, loc));
            }
            loc.advance();
        }
        return result;
    }

    private boolean adjustLoc(location loc) {
        if (loc.getCurr() == '\n') {
            loc.line++;
            loc.col = 1;
            return true;
        } if (loc.getCurr() == '\t') {
            loc.col++;
            return true;
        } if (loc.getCurr() == ' ') {
            return true;
        }
        return false;
    }

    //start with the pos after the <%, end with it at the > in the %>
    private Token getStatementToken(String str, location loc) {
        int startCol = loc.col;
        int startLine = loc.line;
        int start = loc.pos;
        int end;

        //TODO: catch the error
        while (true) {
            if (loc.getPrev() == '%' && loc.getCurr() == '>') {
                end = loc.pos;
                break;
            }
            adjustLoc(loc);
            loc.advance();
        }
        loc.endOfLastSEorD = end + 1;
        //@TODO: nums
        return new Token(Token.TokenType.STATEMENT, str.substring(start, end - 1).trim(), startLine, startCol, start);
    }

    //@TODO: is repetitive, consolidate
    //start with the pos after the ${, end with it at the }
    private Token getExprToken(String str, location loc) {
        int startCol = loc.col;
        int startLine = loc.line;
        int start = loc.pos;
        int end;
        //TODO: catch the error
        while (true) {
            if (loc.getCurr() == '}') {
                end = loc.pos;
                break;
            }
            adjustLoc(loc);
            loc.advance();
        }
        loc.endOfLastSEorD = end + 1;
        //@TODO: nums
        return new Token(Token.TokenType.EXPRESSION, str.substring(start, end).trim(), startLine, startCol, start);
    }

    private Token getStringContentToken(String str, location loc) {
        //TODO: catch the error
        int startCol = loc.col;
        int startLine = loc.line;
        int end;
        while (loc.pos < loc.chars.length) {
            if ((loc.getCurr() == '<' && loc.getNext() == '%') ||
                    (loc.getCurr() == '$' && loc.getNext() == '{')) {
                break;
            } else {
                adjustLoc(loc);
                loc.advance();
            }
        }
        end = loc.pos - 1;
        loc.retreat();
        //TODO: nums
        return new Token(Token.TokenType.STRING_CONTENT, str.substring(loc.endOfLastSEorD, end + 1), startLine, startCol, loc.endOfLastSEorD);
    }

}
