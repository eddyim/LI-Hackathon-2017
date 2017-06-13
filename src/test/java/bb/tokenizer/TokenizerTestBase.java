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

    @Test
    public void contentTest() {
        ITokenizer  tokenizer = createTokenizer();
        assertEquals(Collections.emptyList(), tokenizer.tokenize(""));

        assertEquals(tokenizer.tokenize("<html></html>").get(0).getContent(),"<html></html>");

        assertContentsAre(tokenizer.tokenize("<html>${2 + 2}</html>"),"<html>", "2 + 2", "</html>");
    }

    @Test
    public void lineColPosTest() {
        ITokenizer  tokenizer = createTokenizer();
        assertEquals(Collections.emptyList(), tokenizer.tokenize(""));

        assertLineColPosAre(tokenizer.tokenize("<html></html>"),1, 1, 0);

        assertLineColPosAre(tokenizer.tokenize("<html>${2 + 2}</html>"),1, 1, 0, 1, 1, 8, 1, 1, 14);
    }

    private void assertLineColPosAre(List<Token> tokenize, int ... vals) {
        assertEquals(tokenize.size() * 3, vals.length);
        for (int i = 0; i < tokenize.size(); i++) {
            Token token = tokenize.get(i);
            assertEquals(token.getLine(), vals[i * 3]);
            assertEquals(token.getOffset(), vals[i * 3 + 1]);
            assertEquals(token.getPosition(), vals[i * 3 + 2]);
        }
    }

    private void assertContentsAre(List<Token> tokenize, String ... content) {
        assertEquals(tokenize.size(), content.length);
        for (int i = 0; i < tokenize.size(); i++) {
            Token token = tokenize.get(i);
            assertEquals(token.getContent(), content[i]);
        }
    }

    public void asssertTokenTypesAre(List<Token> tokenize, TokenType... stringContent) {
        assertEquals(tokenize.size(), stringContent.length);
        for (int i = 0; i < tokenize.size(); i++) {
            Token token = tokenize.get(i);
            assertEquals(token.getType(), stringContent[i]);
        }
    }


}
