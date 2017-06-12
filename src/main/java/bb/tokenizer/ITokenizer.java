package bb.tokenizer;

import java.util.List;

public interface ITokenizer {

    public List<Token> tokenize(String str);
}
