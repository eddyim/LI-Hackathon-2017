package bb.tokenizer;


public class Token {

    public enum TokenType {
        STRING_CONTENT,
        EXPRESSION,
        STATEMENT,
        DIRECTIVE,
        COMMENT
    }

    private TokenType _type;
    private String _content;
    private int _offset;
    private int _line;
    private int _position;

    public Token(TokenType type, String content, int line, int column, int position) {
        _type = type;
        _content = content;
        _offset = column;
        _line = line;
        _position = position;
    }

    public TokenType getType() {
        return _type;
    }

    public String getContent() {
        return _content;
    }

    public int getOffset() {
        return _offset;
    }

    public int getLine() {
        return _line;
    }

    public int getPosition() {
        return _position;
    }
}
