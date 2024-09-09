
import java.util.List;

public class Main {

    public static void main(String[] args) {
        String input = "main begin V_x = 42; if V_x < 50 then begin end else begin end end";

        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();

        for (Token token : tokens) {
            System.out.println(token);
        }

        System.out.println("\n\n\n\n");

        // Example input based on your grammar
        input = "main begin V_x = 42; print V_x end";
        lexer = new Lexer(input);
        tokens = lexer.tokenize();
        // // Create a parser and parse the tokens
        RecSPLParser.Parser parser = new RecSPLParser.Parser(tokens);
        ProgramNode program = parser.parse();
        // Optionally, print the parsed program or process the AST
        System.out.println("Parsed program successfully!");
        System.out.println(program);
    }
}
