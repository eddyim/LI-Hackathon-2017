package bb.tokenizer;


public class Token {

    enum TokenType {
        STRING_CONTENT,
        EXPRESSION,
        STATEMENT,
        DIRECTIVE
    }

    private TokenType _type;
    private String _content;
    private int _offset;
    private int _line;
    private int _position;

    public Token(TokenType type, String content, int offset, int line, int position) {
        this._type = type;
        this._content = content;
        this._offset = offset;
        this._line = line;
        this._position = position;
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
