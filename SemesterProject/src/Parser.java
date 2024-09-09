
import java.util.List;

/**
 *
 * @author Jarod Joffery
 */
public class Parser {

    private final List<Token> tokens;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Token> getTokens() {
        return tokens;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Token token : tokens) {
            String temp = token + "\n";
            sb.append(temp);
        }
        return sb.toString();
    }

}
