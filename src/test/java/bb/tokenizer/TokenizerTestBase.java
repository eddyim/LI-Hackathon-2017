package bb.tokenizer;

import bb.tokenizer.Token.TokenType;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

abstract public class TokenizerTestBase {

    protected abstract ITokenizer createTokenizer();

    @Test
    public void bootStrapTest() {
        ITokenizer  tokenizer = createTokenizer();
        assertEquals(Collections.emptyList(), tokenizer.tokenize(""));

        asssertTokenTypesAre(tokenizer.tokenize("<html></html>"), TokenType.STRING_CONTENT);

        asssertTokenTypesAre(tokenizer.tokenize("<html>${2 + 2}</html>"),
                TokenType.STRING_CONTENT, TokenType.EXPRESSION, TokenType.STRING_CONTENT);

        asssertTokenTypesAre(tokenizer.tokenize("<html><% if(true) { %> foo <% } else { %> bar <% } %></html>"),
                TokenType.STRING_CONTENT, TokenType.STATEMENT, TokenType.STRING_CONTENT, TokenType.STATEMENT,
                TokenType.STRING_CONTENT, TokenType.STATEMENT, TokenType.STRING_CONTENT);
    }

    public void asssertTokenTypesAre(List<Token> tokenize, TokenType... stringContent) {
        assertEquals(tokenize.size(), stringContent.length);
        for (int i = 0; i < tokenize.size(); i++) {
            Token token = tokenize.get(i);
            assertEquals(token.getType(), stringContent[i]);
        }
    }


    @Test
    public void tempTest() {

    }

}
