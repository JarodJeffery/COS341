
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Lexer {

    private final String input;
    private int pos;
    private Token currentToken;

    public Lexer(String input) {
        this.input = input;
        this.pos = 0;
        this.currentToken = nextToken();
    }

    public Token getCurrentToken() {
        return currentToken;
    }

    public Token nextToken() {
        if (pos >= input.length()) {
            return new Token(Token.TokenType.EOF, "");
        }

        for (Token.TokenType type : Token.TokenType.values()) {
            if (type == Token.TokenType.EOF) {
                continue;  // Skip EOF pattern check
            }
            Pattern pattern = Pattern.compile("^" + type.getPattern());
            Matcher matcher = pattern.matcher(input.substring(pos));

            if (matcher.find()) {
                String tokenValue = matcher.group();
                pos += tokenValue.length();
                return new Token(type, tokenValue);
            }
        }

        throw new RuntimeException("Unrecognized token at position " + pos);
    }

    public void advance() {
        while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
            pos++;
        }
        currentToken = nextToken();
    }
}
