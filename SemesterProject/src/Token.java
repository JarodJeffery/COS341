
public class Token {

    enum TokenType {
        MAIN("main"),
        BEGIN("begin"),
        END("end"),
        NUM("num"),
        TEXT("text"),
        VOID("void"),
        VNAME("V_[a-z]([a-z]|[0-9])*"), // user-defined variable names
        FNAME("F_[a-z]([a-z]|[0-9])*"), // user-defined function names
        STRING("\"[A-Z][a-z]{0,7}\""), // short text strings
        NUMBER("-?[0-9]+(\\.[0-9]+)?"), // numbers (integers or real numbers)
        ASSIGNMENT("<|="), // assignment operators
        INPUT("input"), // input keyword
        OPERATOR("(or|and|eq|grt|add|sub|mul|div)"), // binary operators
        UNOP("(not|sqrt)"), // unary operators
        IF("if"),
        THEN("then"),
        ELSE("else"),
        PRINT("print"),
        HALT("halt"),
        SKIP("skip"),
        CALL("call"),
        LPAREN("\\("),
        RPAREN("\\)"),
        COMMA(","),
        SEMICOLON(";"),
        LBRACE("\\{"),
        RBRACE("\\}"),
        EOF(""); // end of file// end of file

        private final String pattern;

        TokenType(String pattern) {
            this.pattern = pattern;
        }

        public String getPattern() {
            return pattern;
        }
    }
    public final TokenType type;
    public final String value;

    public Token(TokenType type, String value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("Token(%s, %s)", type.name(), value);
    }
}
