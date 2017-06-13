package bb.tokenizer;

import java.util.ArrayList;
import java.util.List;

public class HTokenizer implements ITokenizer {
    private class location {
        int line = 1;
        int col = 1;
        int pos = 0;
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
        }

        void retreat() {
            pos--;
        }
    }

    public List<Token> tokenize(String str) {
        ArrayList<Token> result = new ArrayList<Token>();
        location loc = new location(str.toCharArray());

        while (loc.pos < str.length()) {
            if (loc.getCurr() == '<') {
                loc.pos++;
                if (loc.getCurr() == '%') { //is a statement
                    loc.pos++;
                    result.add(getStatementToken(str, loc));
                } else {
                    loc.pos--;
                    result.add(getStringContentToken(str, loc));
                }
            }

            else if (loc.getCurr() == '$') {
                loc.pos++;
                if (loc.getCurr() == '{') {  //is an expression
                    loc.pos++;
                    result.add(getExprToken(str, loc));
                } else {
                    loc.pos--;
                    result.add(getStringContentToken(str, loc));
                }
            }

            else if (!adjustLoc(loc)) {  //is a string statement
                result.add(getStringContentToken(str, loc));
            }
            loc.pos++;
        }
        return result;
    }

    private boolean adjustLoc(location loc) {
        if (loc.getCurr() == '\n') {
            loc.line++;
            loc.col = 1;
            int spaces = 0;
            while (loc.getNext() == ' ') {
                loc.pos++;
                spaces++;
                if (spaces == 4) {
                    spaces = 0;
                    loc.col++;
                }
            }
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
        int start = loc.pos;
        int end;
        //TODO: catch the error
        while (true) {
            if (loc.getPrev() == '%' && loc.getCurr() == '>') {
                end = loc.pos - 1;
                break;
            }
            adjustLoc(loc);
            loc.pos++;
        }
        //@TODO: nums
        return new Token(Token.TokenType.STATEMENT, str.substring(start, end), loc.line, loc.col, start);
    }

    //@TODO: is repetitive, consolidate
    //start with the pos after the ${, end with it at the }
    private Token getExprToken(String str, location loc) {
        int start = loc.pos;
        int end;
        //TODO: catch the error
        while (true) {
            if (loc.getCurr() == '}') {
                end = loc.pos;
                break;
            }
            adjustLoc(loc);
            loc.pos++;
        }
        //@TODO: nums
        return new Token(Token.TokenType.EXPRESSION, str.substring(start, end), loc.line, loc.col, start);
    }

    private Token getStringContentToken(String str, location loc) {
        //TODO: catch the error
        int start = loc.pos;
        int end = 0;
        while (loc.pos < loc.chars.length) {
            if ((loc.getCurr() == '<' && loc.getNext() == '%') ||
                    (loc.getCurr() == '$' && loc.getNext() == '{')) {
                break;
            } else {
                adjustLoc(loc);
                loc.pos++;
            }
        }
        end = loc.pos;
        loc.pos--;
        //TODO: nums
        return new Token(Token.TokenType.STRING_CONTENT ,str.substring(start, end), loc.line, loc.col, start);
    }

}
