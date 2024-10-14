
public class Compiler {

    public static void main(String[] args) throws Exception {
        String input = "main num V_x, text V_y begin skip; end"; // Example input

        Lexer lexer = new Lexer(input, "Lexer_out.xml");
        Parser parser = new Parser("Lexer_out.xml");
        parser.parseProgram();
        try {
            String javaCode = CodeGenerator.generateCode(parser);
            System.out.println(javaCode);
        } catch (Exception e) {
            System.err.println("Compilation failed: " + e.getMessage());
        }
    }
}
