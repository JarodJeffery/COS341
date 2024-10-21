
public class Token {
  public enum TokenType {
    MAIN("main"),
    GLOBVARS("globalvars"),
    NULLABLE("null"),
    VTYPE("vtype"),
    PROG("prog"),
    ALGO("algo"),
    FUNCTIONS("functions"),
    BEGIN("begin"),
    END("end"),
    IF("if"),
    THEN("then"),
    ELSE("else"),
    PRINT("print"),
    RETURN("return"),
    NUM("num"),
    TEXT("text"),
    SKIP("skip"),
    VOID("void"), // Added for function return types
    INPUT("\\binput\\b"),
    HALT("halt"),
    CALL("call"),
    VNAME("V_[a-z]([a-z]|[0-9])*"), // user-defined variable names
    FNAME("F_[a-z]([a-z]|[0-9])*"), // user-defined function names
    STRING("\"[A-Z][a-z]*{0,7}\""), // short text strings
    NUMBER("-?[0-9]+(\\.[0-9]+)?"), // numbers (integers or real numbers)
    ASSIGNMENT("="), // assignment operators
    INPUT_OPERATOR("<"),
    BINOP("(or|and|eq|grt|add|sub|mul|div)"), // binary operators
    UNOP("(not|sqrt)"), // unary operators
    LPAREN("\\("), // Left parenthesis
    RPAREN("\\)"), // Right parenthesis
    COMMA(","), // Comma
    SEMICOLON(";"), // Semicolon
    LBRACE("\\{"), // Prologue (left brace)
    RBRACE("\\}"), // Epilogue (right brace)
    EOF(""); // End of file

    public final String pattern;

    TokenType(String pattern) {
      this.pattern = pattern;
    }

    public String getPattern() {
      return pattern;
    }

  }

  private final TokenType type;
  public final String value;

  public Token(TokenType type, String value) {
    this.type = type;
    this.value = value;
  }

  public TokenType getType() {
    return type;
  }

  public String getValue() {
    return value ;
  }

  @Override
  public String toString() {
    return String.format("Token(%s, %s)", type, value);
  }
}
