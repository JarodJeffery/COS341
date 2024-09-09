public class Token {

    public final RecSPLLexer.TokenType type;
    public final String lexeme;

    public Token(RecSPLLexer.TokenType type, String lexeme) {
        this.type = type;
        this.lexeme = lexeme;
    }

    public RecSPLLexer.TokenType getType() {
        return type;
    }

    public String getLexeme() {
        return lexeme;
    }

    @Override
    public String toString() {
        return String.format("Token(%s, %s)", type.name(), lexeme);
    }

    public String getValue() {
        return lexeme;
    }
}