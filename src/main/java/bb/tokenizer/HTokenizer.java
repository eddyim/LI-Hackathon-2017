package bb.tokenizer;

import java.util.ArrayList;
import java.util.List;

public class HTokenizer implements ITokenizer {
    private class location {
        int line = 0;
        int col = 0;
        int pos = 0;
    }

    public List<Token> tokenize(String str) {
        ArrayList<Token> result = new ArrayList<Token>();
        location loc = new location();
        char[] chars = str.toCharArray();

        while (loc.pos < chars.length) {
            if (chars[loc.pos] == '<') {
                loc.pos++;
                if (chars[loc.pos] == '%') { //is a statement
                    loc.pos++;
                    result.add(getStatementToken(str, chars, loc));
                } else {
                    loc.pos--;
                    result.add(getStringContentToken(str, chars, loc));
                }
            }

            else if (chars[loc.pos] == '$') {
                loc.pos++;
                if (chars[loc.pos] == '{') {  //is an expression
                    loc.pos++;
                    result.add(getExprToken(str, chars, loc));
                } else {
                    loc.pos--;
                    result.add(getStringContentToken(str, chars, loc));
                }
            }

            else if (!adjustLoc(chars, loc)) {  //is a string statement
                result.add(getStringContentToken(str, chars, loc));
            }
            loc.pos++;
        }
        return result;
    }

    private boolean adjustLoc(char[] chars, location loc) {
        if (chars[loc.pos] == '\n') {
            loc.line++;
            loc.col = 0;
            return true;
        } if (chars[loc.pos] == '\t') {
            loc.col++;
            return true;
        } if (chars[loc.pos] == ' ') {
            return true;
        }
        return false;
    }

    //start with the pos after the <%, end with it at the > in the %>
    private Token getStatementToken(String str, char[] chars, location loc) {
        int start = loc.pos;
        int end;
        //TODO: catch the error
        while (true) {
            if (chars[loc.pos-1] == '%' && chars[loc.pos] == '>') {
                end = loc.pos - 2;
                break;
            }
            adjustLoc(chars, loc);
            loc.pos++;
        }
        //@TODO: nums
        return new Token(Token.TokenType.STATEMENT, str.substring(start, end).trim(), 0, 0, 0);
    }

    //@TODO: is repetitive, consolidate
    //start with the pos after the ${, end with it at the }
    private Token getExprToken(String str, char[] chars, location loc) {
        int start = loc.pos;
        int end;
        //TODO: catch the error
        while (true) {
            if (chars[loc.pos] == '}') {
                end = loc.pos - 1;
                break;
            }
            adjustLoc(chars, loc);
            loc.pos++;
        }
        //@TODO: nums
        return new Token(Token.TokenType.EXPRESSION, str.substring(start, end).trim(), 0, 0, 0);
    }

    private Token getStringContentToken(String str, char[] chars, location loc) {
        //TODO: catch the error
        int start = loc.pos;
        int end = chars.length;
        while (loc.pos < chars.length) {
            if ((chars[loc.pos] == '<' && chars[loc.pos + 1] == '%') ||
                    (chars[loc.pos] == '$' && chars[loc.pos + 1] == '{')) {
                loc.pos--;
                end = loc.pos;
                break;
            }
            adjustLoc(chars, loc);
            loc.pos++;
        }
        //TODO: nums
        return new Token(Token.TokenType.STRING_CONTENT ,str.substring(start, end), 0, 0, 0);
    }

}
