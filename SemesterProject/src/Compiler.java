import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Compiler {

    public static void main(String[] args) throws Exception {
        //Scanner scanner = new Scanner(System.in);
        //System.out.print("Enter the input file path: ");
        //String filePath = scanner.nextLine();
        Lexer lexer = new Lexer("RecursiveCalls.txt", "Lexer_out.xml");
        TokenReader tokenReader = new TokenReader("Lexer_out.xml");
        List<Token> tokens = tokenReader.getTokens();

        // Create and run the SLR parser
        Parser parser = new Parser("Lexer_out.xml" );
        parser.parsePROG();
        ScopeAnalyzer analyzer = new ScopeAnalyzer("Parser_out.xml");
        analyzer.analyze();
        TypeChecker checker = new TypeChecker("Parser_out.xml");
        checker.check();
        CodeGenerator generator = new CodeGenerator("Parser_out.xml");
        generator.generateBASIC();
        //scanner.close();
    }
}
