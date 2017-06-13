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
    public void testStatementError() {
        ITokenizer tokenizer = createTokenizer();
        boolean errorCaught = false;
        try {
            tokenizer.tokenize("<html><% if(true) { %> foo <% else {  bar <% } %></html>");
        } catch (RuntimeException e) {
            System.out.println(e);
            errorCaught = true;
        }
        assertEquals(errorCaught, true);
    }

    @Test
    public void testQuotedExpressions() {
        ITokenizer tokenizer = createTokenizer();
        List<Token> doubleQuotedExpression = tokenizer.tokenize("<html>${\"}\"}</html>");
        List<Token> singleQuotedExpression = tokenizer.tokenize("<html>${\'}\'}</html>");
        asssertTokenTypesAre(doubleQuotedExpression, TokenType.STRING_CONTENT, TokenType.EXPRESSION, TokenType.STRING_CONTENT);
        assertEquals("\"}\"", doubleQuotedExpression.get(1).getContent());
        assertEquals("\'}\'", singleQuotedExpression.get(1).getContent());

    }

    @Test
    public void testNestedQuotedExpressions() {
        ITokenizer tokenizer = createTokenizer();
        List<Token> nestedSingleExpression = tokenizer.tokenize("<html>${\"'hello }'\"}</html>");
        List<Token> nestedDoubleExpression = tokenizer.tokenize("<html>${'\"hello }\"'}</html>");
        assertEquals("\"'hello }'\"", nestedSingleExpression.get(1).getContent());
        assertEquals("'\"hello }\"'", nestedDoubleExpression.get(1).getContent());

    }


    public void asssertTokenTypesAre(List<Token> tokenize, TokenType... stringContent) {
        assertEquals(tokenize.size(), stringContent.length);
        for (int i = 0; i < tokenize.size(); i++) {
            Token token = tokenize.get(i);
            assertEquals(token.getType(), stringContent[i]);
        }
    }


}
