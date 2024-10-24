package lexer;

public class Token {
    public enum TokenType {
        MAIN("main", true),
        BEGIN("begin", true),
        END("end", true),
        IF("if", true),
        THEN("then", true),
        ELSE("else", true),
        SKIP("skip", true),
        HALT("halt", true),
        PRINT("print", true),
        RETURN("return", true),
        N("num", true),
        T("text", true),
        V("V_[a-z]([a-z]|[0-9])*", true), // User-defined variable names
        F("F_[a-z]([a-z]|[0-9])*", true), // User-defined function names
        STRING("\"[A-Z][a-z]{0,7}\"", true),
        NUMBER("-?[0-9]+(\\.[0-9]+)?", true),
        ASSIGNMENT("=", true),
        INPUT("\\binput\\b", true),
        VOID("void", true),
        INPUT_OPERATOR("<", true),
        BINOP("(or|and|eq|grt|add|sub|mul|div)", true),
        UNOP("(not|sqrt)", true),
        LPAREN("\\(", true),
        RPAREN("\\)", true),
        COMMA(",", true),
        SEMICOLON(";", true),
        PROLOG("\\{", true),
        EPILOG("\\}", true),
        EOF("", true),
        // Non-terminals (set isTerminal to false for non-terminals)
        PROG("", false),
        GLOBVARS("", false),
        VTYP("", false),
        VNAME("", false),
        ALGO("", false),
        INSTRUC("", false),
        COMMAND("", false),
        ASSIGN("", false),
        CALL("", false),
        BRANCH("", false),
        ATOMIC("", false),
        CONST("", false),
        TERM("", false),
        OP("", false),
        ARG("", false),
        COND("", false),
        SIMPLE("", false),
        COMPOSIT("", false),
        FNAME("", false),
        FUNCTIONS("", false),
        HEADER("", false),
        BODY("", false),
        PROLOG_NT("", false), // renamed to avoid conflict with terminal
        EPILOG_NT("", false), // renamed to avoid conflict with terminal
        LOCVARS("", false),
        SUBFUNCS("", false);

        private final String pattern;
        private final boolean isTerminal;

        TokenType(String pattern, boolean isTerminal) {
            this.pattern = pattern;
            this.isTerminal = isTerminal;
        }

        public String getPattern() {
            return pattern;
        }

        public boolean isTerminal() {
            return isTerminal;
        }
    }

    private final TokenType type;
    private final String value;

    public Token(TokenType type, String value) {
        this.type = type;
        this.value = value;
    }

    public TokenType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("Token(%s, %s)", type, value);
    }
}
