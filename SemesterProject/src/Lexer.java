import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {

    private final String input;
    private final List<Token> tokens;
    private int currentPos = 0;

    public Lexer(String input) {
        this.input = input;
        this.tokens = new ArrayList<>();
    }

    public List<Token> tokenize() {
        while (currentPos < input.length()) {
            boolean matched = false;

            // Skip whitespaces
            if (Character.isWhitespace(input.charAt(currentPos))) {
                currentPos++;
                continue;
            }

            for (RecSPLLexer.TokenType tokenType : RecSPLLexer.TokenType.values()) {
                Pattern pattern = Pattern.compile("^" + tokenType.pattern);
                Matcher matcher = pattern.matcher(input.substring(currentPos));

                if (matcher.find()) {
                    String lexeme = matcher.group();
                    tokens.add(new Token(tokenType, lexeme));
                    currentPos += lexeme.length();
                    matched = true;
                    break;
                }
            }

            if (!matched) {
                throw new RuntimeException("Unexpected character at position " + currentPos + ": " + input.charAt(currentPos));
            }
        }

        tokens.add(new Token(RecSPLLexer.TokenType.EOF, ""));
        return tokens;
    }
}

