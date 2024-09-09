public class RecSPLLexer {
    // Token types

    public enum TokenType {
        MAIN("main"),
        BEGIN("begin"),
        END("end"),
        NUM("num"),
        TEXT("text"),
        VNAME("V_[a-z]([a-z]|[0-9])*"), // user-defined variable names
        FNAME("F_[a-z]([a-z]|[0-9])*"), // user-defined function names
        STRING("\"[A-Z][a-z]{0,7}\""), // short snippets of text (strings)
        NUMBER("-?[0-9]+(\\.[0-9]+)?"), // numbers (integers or real numbers)
        ASSIGNMENT("<|="), // assignment operators
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
        EOF(""); // end of file

        public final String pattern;

        TokenType(String pattern) {
            this.pattern = pattern;
        }
    }

}
