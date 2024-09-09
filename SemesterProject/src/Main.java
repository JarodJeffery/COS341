
import java.util.List;

public class Main {

    public static void main(String[] args) {
        String input = "main begin V_x = 42; if V_x < 50 then begin end else begin end end";

        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();

        for (Token token : tokens) {
            System.out.println(token);
        }

        // Example input based on your grammar
        // String input1 = "main begin V_x = 42; print V_x end";
        // Lexer lexer1 = new Lexer(input1);
        // List<Token> tokens1 = lexer1.tokenize();
        // // Create a parser and parse the tokens
        // Parser parser = new Parser(tokens1);
        // Parser.ProgramNode program = parser.parse();
        // // Optionally, print the parsed program or process the AST
        // System.out.println("Parsed program successfully!");
        // System.out.println(program);
    }
}
