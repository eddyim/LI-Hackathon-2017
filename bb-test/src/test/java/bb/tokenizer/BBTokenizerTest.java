package bb.tokenizer;

import bb.tokenizer.Token.TokenType;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static bb.tokenizer.Token.TokenType.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

abstract public class BBTokenizerTest {

    protected abstract ITokenizer createTokenizer();

    @Test
    public void bootStrapTest() {
        ITokenizer  tokenizer = createTokenizer();
        assertEquals(Collections.emptyList(), tokenizer.tokenize(""));

        asssertTokenTypesAre(tokenizer.tokenize("<html></html>"), STRING_CONTENT);

        asssertTokenTypesAre(tokenizer.tokenize("<html>${2 + 2}</html>"),
                STRING_CONTENT, TokenType.EXPRESSION, STRING_CONTENT);

        asssertTokenTypesAre(tokenizer.tokenize("<html><% if(true) { %> foo <% } else { %> bar <% } %></html>"),
                STRING_CONTENT, STATEMENT, STRING_CONTENT, STATEMENT,
                STRING_CONTENT, STATEMENT, STRING_CONTENT);
    }

    @Test
    public void contentTest() {
        ITokenizer  tokenizer = createTokenizer();
        assertEquals(Collections.emptyList(), tokenizer.tokenize(""));

        assertEquals("<html></html>", tokenizer.tokenize("<html></html>").get(0).getContent());

        assertContentsAre(tokenizer.tokenize("<html>${2 + 2}</html>"),"<html>", "2 + 2", "</html>");

        assertContentsAre(tokenizer.tokenize("<html><% if(true) { %> \n foo <% } else { %> bar <% } %></html>"),
                "<html>", "if(true) {", " \n foo ", "} else {", " bar ", "}", "</html>");

    }

    @Test

    public void lineColPosTest() {
        ITokenizer  tokenizer = createTokenizer();
        assertEquals(Collections.emptyList(), tokenizer.tokenize(""));

        assertLineColPosAre(tokenizer.tokenize("<html></html>"),1, 1, 0);

        assertLineColPosAre(tokenizer.tokenize("<html>${2 + 2}</html>"),1, 1, 0, 1, 7, 6, 1, 15, 14);

        //assertLineColPosAre(tokenizer.tokenize("<html><% if(true) { %> foo <% } else { %> bar <% } %></html>"),
        //        1, 1, 0, 1, 1, 8, 1, 1, 14);

    }

    private void assertLineColPosAre(List<Token> tokenize, int ... vals) {
        assertEquals(tokenize.size() * 3, vals.length);
        for (int i = 0; i < tokenize.size(); i++) {
            Token token = tokenize.get(i);
            assertEquals(vals[i * 3], token.getLine());
            assertEquals(vals[i * 3 + 1], token.getOffset());
            assertEquals(vals[i * 3 + 2], token.getPosition());
        }
    }

    private void assertContentsAre(List<Token> tokenize, String ... content) {
        assertEquals(tokenize.size(), content.length);
        for (int i = 0; i < tokenize.size(); i++) {
            Token token = tokenize.get(i);
            assertEquals(content[i], token.getContent());
        }
    }


    @Test
    public void statementErrorTest() {
        ITokenizer tokenizer = createTokenizer();
        boolean caught = false;
        try {
            tokenizer.tokenize("<% foo");
            System.out.println("Failed to throw exception when not closing statement");
        } catch (RuntimeException e) {
            caught = true;
        }
        assertTrue(caught);
        caught = false;
        try {
            tokenizer.tokenize("<% abc <% abc %> %>");
            System.out.println("Failed to throw exception when opening statement within statement.");
        } catch (RuntimeException e) {
            caught = true;
        }
        assertTrue(caught);
        caught = false;
        try {
            tokenizer.tokenize("<% ${ } %>");
            System.out.println("Failed to throw exception when opening expression within statement.");
        } catch (RuntimeException e) {
            caught = true;
        }
        assertTrue(caught);
        caught = false;
        try {
            tokenizer.tokenize("<% Abc <%@ abc %> %>");
            System.out.println("Failed to throw exception when opening directive within statement");
        } catch (RuntimeException e) {
            caught = true;
        }
        assertTrue(caught);
    }

    @Test
    public void expressionErrorTest() {
        ITokenizer tokenizer = createTokenizer();
        boolean caught = false;
        try {
            tokenizer.tokenize("${ foo");
            System.out.println("Failed to throw exception when not closing expression");
        } catch (RuntimeException e) {
            caught = true;
        }
        assertTrue(caught);
        caught = false;
        try {
            tokenizer.tokenize("${ abc <% abc %> }");
            System.out.println("Failed to throw exception when opening statement within expression.");
        } catch (RuntimeException e) {
            caught = true;
        }
        assertTrue(caught);
        caught = false;
        try {
            tokenizer.tokenize("${ ${ } }");
            System.out.println("Failed to throw exception when opening expression within expression.");
        } catch (RuntimeException e) {
            caught = true;
        }
        assertTrue(caught);
        caught = false;
        try {
            tokenizer.tokenize("${ Abc <%@ abc %> }");
            System.out.println("Failed to throw exception when opening directive within expression");
        } catch (RuntimeException e) {
            caught = true;
        }
        assertTrue(caught);
    }

    @Test
    public void testQuotedExpressions() {
        ITokenizer tokenizer = createTokenizer();
        List<Token> doubleQuotedExpression = tokenizer.tokenize("<html>${\"}\"}</html>");
        List<Token> singleQuotedExpression = tokenizer.tokenize("<html>${\'}\'}</html>");
        asssertTokenTypesAre(doubleQuotedExpression, STRING_CONTENT, TokenType.EXPRESSION, STRING_CONTENT);
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

    @Test
    public void testQuotedStatement() {
        ITokenizer tokenizer = createTokenizer();
        List<Token> doubleQuotedStatement = tokenizer.tokenize("<% \"%>\" %>");
        List<Token> singleQuotedStatement = tokenizer.tokenize("<% '%>' %>");
        assertEquals("\"%>\"", doubleQuotedStatement.get(0).getContent());
        assertEquals("'%>'", singleQuotedStatement.get(0).getContent());

    }

    @Test
    public void testNestedQuotedStatement() {
        ITokenizer tokenizer = createTokenizer();
        List<Token> nestedSingleStatement = tokenizer.tokenize("<%\"'hello }'\"%>");
        List<Token> nestedDoubleStatement = tokenizer.tokenize("<%'\"hello }\"'%>");
        assertEquals("\"'hello }'\"", nestedSingleStatement.get(0).getContent());
        assertEquals("'\"hello }\"'", nestedDoubleStatement.get(0).getContent());

    }

    /** Tests that within string literals in statements, expressions
     * and directives, \" is recognized as an escape character.
     */
    @Test
    public void testEscape() {
        ITokenizer tokenizer = createTokenizer();
        tokenizer.tokenize("<%\"\\\"%>\"%>");
        tokenizer.tokenize("${\"\\\"}\"}");
        tokenizer.tokenize("<%@\"\\\"%>\"%>");
    }

    /** Tests that ending files with various types of tokens doesn't create errors */
    @Test
    public void endFileTest() {
        ITokenizer tokenizer = createTokenizer();
        tokenizer.tokenize("HELLO");
        tokenizer.tokenize("${ else }");
        tokenizer.tokenize("<% foo bar %>");
        tokenizer.tokenize("<%@ foo bar %>");
    }

    @Test
    public void longerTest() {
        ITokenizer tokenizer = createTokenizer();
        List<Token> tokens = tokenizer.tokenize("<html>\n" +
                "   <head><title>Hello World</title></head>\n" +
                "   \n" +
                "   <body>\n" +
                "      Hello World!<br/>\n" +
                "      <%\n" +
                "         out.println(\"Your IP address is \" + request.getRemoteAddr());\n" +
                "      %>\n" +
                "   </body>\n" +
                "</html>");
        asssertTokenTypesAre(tokens, STRING_CONTENT, STATEMENT, STRING_CONTENT);
    }

    @Test
    public void testDirectiveBasic() {
        ITokenizer tokenizer = createTokenizer();
        asssertTokenTypesAre(tokenizer.tokenize("<html><%@ directives, yo%></html>"),
                STRING_CONTENT, DIRECTIVE, STRING_CONTENT);
    }

    @Test
    public void directiveErrorTest() {
        ITokenizer tokenizer = createTokenizer();
        boolean caught = false;
        try {
            tokenizer.tokenize("<%@ foo");
            System.out.println("Failed to throw exception when not closing directive");
        } catch (RuntimeException e) {
            caught = true;
        }
        assertTrue(caught);
        caught = false;
        try {
            tokenizer.tokenize("<%@ abc <% abc %> %>");
            System.out.println("Failed to throw exception when opening statement within directive.");
        } catch (RuntimeException e) {
            caught = true;
        }
        assertTrue(caught);
        caught = false;
        try {
            tokenizer.tokenize("<%@ ${ } %>");
            System.out.println("Failed to throw exception when opening expression within directive.");
        } catch (RuntimeException e) {
            caught = true;
        }
        assertTrue(caught);
        caught = false;
        try {
            tokenizer.tokenize("<%@ Abc <%@ abc %> %>");
            System.out.println("Failed to throw exception when opening directive within directive");
        } catch (RuntimeException e) {
            caught = true;
        }
        assertTrue(caught);
    }

    @Test
    public void emptyTest() {
        ITokenizer tokenizer = createTokenizer();
        List<Token> tokens = tokenizer.tokenize("${}<%%><%@%>");
        List<Token> tokensWhiteSpace = tokenizer.tokenize("${  }<%  %><%@    %>");

        asssertTokenTypesAre(tokens, EXPRESSION, STATEMENT, DIRECTIVE);
        for (int i = 0; i < tokens.size(); i += 1) {
            assertEquals(tokens.get(i).getContent(), tokensWhiteSpace.get(i).getContent());
            assertEquals(tokens.get(i).getType(), tokens.get(i).getType());
        }
    }

    @Test
    public void nullTest() {
        ITokenizer tokenizer = createTokenizer();
        List<Token> tokens = tokenizer.tokenize(null);
        assertEquals(0, tokens.size());
    }

    @Test
    public void blankStringTest() {
        ITokenizer tokenizer = createTokenizer();
        List<Token> tokens = tokenizer.tokenize("       ");
        assertEquals("       ", tokens.get(0).getContent());
        assertEquals(STRING_CONTENT, tokens.get(0).getType());
    }

    @Test
    public void commentTest() {
        ITokenizer tokenizer = createTokenizer();
        List<Token> tokens = tokenizer.tokenize("<%-- This is a comment test. --%>");
        assertEquals("This is a comment test.", tokens.get(0).getContent());
        assertEquals(COMMENT, tokens.get(0).getType());
    }


    private void asssertTokenTypesAre(List<Token> tokenize, TokenType... stringContent) {
        assertEquals(tokenize.size(), stringContent.length);
        for (int i = 0; i < tokenize.size(); i++) {
            Token token = tokenize.get(i);
            assertEquals(stringContent[i], token.getType());
        }
    }

    private void assertTokenContentsAre(List<Token> tokenize, String... stringContent) {
        assertEquals(tokenize.size(), stringContent.length);
        for (int i = 0; i < tokenize.size(); i++) {
            Token token = tokenize.get(i);
            assertEquals(stringContent[i], token.getContent());
        }
    }
}
