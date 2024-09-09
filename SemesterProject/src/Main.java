import java.util.List;

public class Main {
    public static void main(String[] args) {
        String input = "main begin V_x = 42; print V_x end";

        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();

        for (Token token : tokens) {
            System.out.println(token);
        }
    }
}