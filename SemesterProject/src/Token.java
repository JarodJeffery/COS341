public class Token {

    public final RecSPLLexer.TokenType type;
    public final String lexeme;

    public Token(RecSPLLexer.TokenType type, String lexeme) {
        this.type = type;
        this.lexeme = lexeme;
    }

    @Override
    public String toString() {
        return String.format("Token(%s, %s)", type.name(), lexeme);
    }
}