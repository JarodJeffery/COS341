
public class Compiler {

    public static void main(String[] args) {
        String input = "main num V_x, text V_y begin skip; end"; // Example input

        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        try {
            parser.parseProgram();
            String javaCode = CodeGenerator.generateCode(parser);
            System.out.println(javaCode);
        } catch (Exception e) {
            System.err.println("Compilation failed: " + e.getMessage());
        }
    }
}
