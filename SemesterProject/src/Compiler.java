
public class Compiler {

    public static void main(String[] args) throws Exception {
        String input = "main num V_x, text V_y begin print 1; end"; // Example input
        Lexer lexer = new Lexer(input, "Lexer_out.xml");
        Parser parser = new Parser("Lexer_out.xml");
        parser.parseProgram();
        try {
            CodeGenerator generator = new CodeGenerator("parsed_program.xml");
            generator.generateBasicCode();
            generator.writeBasicFile("GeneratedProgram.bas");
            System.out.println("BASIC code generated successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
