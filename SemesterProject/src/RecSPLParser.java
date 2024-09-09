import java.util.*;

// Token and Lexer classes (reuse from the previous implementation)
public class RecSPLParser {
    public static class Parser {
        private final List<Token> tokens;
        private int currentTokenIndex = 0;

        public Parser(List<Token> tokens) {
            this.tokens = tokens;
        }

        // Helper methods
        private Token currentToken() {
            return tokens.get(currentTokenIndex);
        }

        private Token match(RecSPLLexer.TokenType expected) {
            Token token = currentToken();
            if (token.type == expected) {
                currentTokenIndex++;
                return token;
            } else {
                throw new RuntimeException("Expected token " + expected + " but found " + token.type);
            }
        }

        private boolean check(RecSPLLexer.TokenType expected) {
            return currentToken().type == expected;
        }

        // Grammar rule methods

        // PROG ::= main GLOBVARS ALGO FUNCTIONS
        public ProgramNode parseProgram() {
            match(RecSPLLexer.TokenType.MAIN);
            List<DeclarationNode> globVars = parseGlobVars();
            AlgoNode algo = parseAlgo();
            List<DeclarationNode> functions = parseFunctions();
            return new ProgramNode(functions, algo);
        }

        // GLOBVARS ::= // nullable
        // GLOBVARS ::= VTYP VNAME , GLOBVARS
        public List<DeclarationNode> parseGlobVars() {
            List<DeclarationNode> vars = new ArrayList<>();
            if (check(RecSPLLexer.TokenType.NUM) || check(RecSPLLexer.TokenType.TEXT)) {
                while (check(RecSPLLexer.TokenType.NUM) || check(RecSPLLexer.TokenType.TEXT)) {
                    String type = match(currentToken().type).lexeme;
                    String name = match(RecSPLLexer.TokenType.VNAME).lexeme;
                    vars.add(new DeclarationNode(type, name));
                    if (check(RecSPLLexer.TokenType.COMMA)) {
                        match(RecSPLLexer.TokenType.COMMA);
                    } else {
                        break;
                    }
                }
            }
            return vars;
        }

        // ALGO ::= begin INSTRUC end
        public AlgoNode parseAlgo() {
            match(RecSPLLexer.TokenType.BEGIN);
            List<CommandNode> instructions = parseInstruc();
            match(RecSPLLexer.TokenType.END);
            return new AlgoNode(instructions);
        }

        // INSTRUC ::= // nullable
        // INSTRUC ::= COMMAND ; INSTRUC
        public List<CommandNode> parseInstruc() {
            List<CommandNode> commands = new ArrayList<>();

            // Check if we have a command, otherwise INSTRUC is nullable, and we can return an empty list
            if (check(RecSPLLexer.TokenType.SKIP) || check(RecSPLLexer.TokenType.PRINT) || check(RecSPLLexer.TokenType.HALT) || check(RecSPLLexer.TokenType.VNAME)) {
                // Parse the first command
                commands.add(parseCommand());

                if(check(RecSPLLexer.TokenType.END)){
                    return commands;
                }else{
                    match(RecSPLLexer.TokenType.SEMICOLON);
                }
                // Match the semicolon after the command


                // Recursively parse the remaining instructions (or handle the nullable case)
                commands.addAll(parseInstruc());  // recursive call for remaining instructions
            }

            // If no commands found, return the empty list (nullable case)
            return commands;
        }

        // COMMAND ::= skip | halt | print ATOMIC | ASSIGN
        public CommandNode parseCommand() {
            if (check(RecSPLLexer.TokenType.SKIP)) {
                match(RecSPLLexer.TokenType.SKIP);
                return new CommandNode("skip", null);
            } else if (check(RecSPLLexer.TokenType.HALT)) {
                match(RecSPLLexer.TokenType.HALT);
                return new CommandNode("halt", null);
            } else if (check(RecSPLLexer.TokenType.PRINT)) {
                match(RecSPLLexer.TokenType.PRINT);
                ASTNode atomic = parseAtomic();
                return new PrintCommandNode(atomic);
            } else if (check(RecSPLLexer.TokenType.VNAME)) {
                ASTNode vname = parseAtomic();// Parse the variable name

                if (check(RecSPLLexer.TokenType.ASSIGNMENT)) { // Make sure to check for '=' or '<'
                    ASTNode assignment = parseAssign();
                    ASTNode term = parseTerm(); // Parse the assigned term
                    return new AssignCommandNode(vname, assignment, term);
                } else {
                    throw new RuntimeException("Expected assignment operator after VNAME but found: " + currentToken());
                }
            } else {
                throw new RuntimeException("Unexpected command token: " + currentToken());
            }
        }

        // ASSIGN ::= = | <
        public ASTNode parseAssign(){
            if(check(RecSPLLexer.TokenType.ASSIGNMENT)){
                match(RecSPLLexer.TokenType.ASSIGNMENT);
                return new CommandNode("ASSIGNMENT", new ASTNode() {});
            }else{
                throw new RuntimeException("Unexpected Assign token: " + currentToken());
            }
        }

        // ATOMIC ::= VNAME | CONST
        public ASTNode parseAtomic() {
            if (check(RecSPLLexer.TokenType.VNAME)) {
                match(RecSPLLexer.TokenType.VNAME);
                return new CommandNode("VNAME", new ASTNode() {});  // Just an example, you can create a better AST structure
            } else if (check(RecSPLLexer.TokenType.NUMBER)) {
                match(RecSPLLexer.TokenType.NUMBER);
                return new CommandNode("CONST", new ASTNode() {});
            } else {
                throw new RuntimeException("Unexpected atomic token: " + currentToken());
            }
        }

        // TERM ::= ATOMIC | CALL | OP
        public ASTNode parseTerm() {
            if (check(RecSPLLexer.TokenType.VNAME)) {
                match(RecSPLLexer.TokenType.VNAME);
                return parseAtomic();  // Either a variable name or constant is an ATOMIC
            } else if(check(RecSPLLexer.TokenType.NUMBER)){
                return parseAtomic();
            }else{
                throw new RuntimeException("Unexpected term token: " + currentToken());
            }
        }

        // FUNCTIONS ::= // nullable | DECL FUNCTIONS
        public List<DeclarationNode> parseFunctions() {
            // Simplified, as this rule is nullable or contains function declarations.
            return new ArrayList<>();
        }

        // Start parsing
        public ProgramNode parse() {
            return parseProgram();
        }
    }
}
