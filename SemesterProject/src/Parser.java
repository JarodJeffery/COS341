import java.util.List;

// Class for parser
public class Parser {
    private List<Token> tokens;
    private int position = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    // Check if the current token matches the expected type
    private boolean check(RecSPLLexer.TokenType type) {
        return currentToken().getType() == type;
    }

    // Return the current token
    private Token currentToken() {
        return tokens.get(position);
    }

    // Advance to the next token
    private void advance() {
        position++;
    }

    // Match the expected token type and advance
    private void match(RecSPLLexer.TokenType expectedType) {
        if (check(expectedType)) {
            advance();
        } else {
            throw new RuntimeException("Expected token " + expectedType + " but found " + currentToken().getType());
        }
    }

    public ProgramNode parse() {
        // Assuming that the first token should be `main`
        match(RecSPLLexer.TokenType.MAIN); // Match 'main'

        ASTNode globalVars = parseASTGlobalVars();
        ASTNode algorithm = parseASTAlgorithm();
        ASTNode functions = parseASTFunctions();

        // Construct and return the root node of the AST
        return new ProgramNode(globalVars, algorithm, functions);
    }

    // Main parsing method for the program (PROG ::= main GLOBVARS ALGO FUNCTIONS)
    public ASTNode parseProgram() {
        match(RecSPLLexer.TokenType.MAIN);  // Match 'main'
        parseGlobVars();        // Match GLOBVARS (nullable)
        parseAlgo();            // Match ALGO
        parseFunctions();       // Match FUNCTIONS (nullable)
        return new ASTNode();    // Return a root node for the AST (could be enhanced)
    }

    // Parse GLOBVARS (GLOBVARS ::= nullable | VTYP VNAME , GLOBVARS)
    private void parseGlobVars() {
        // GLOBVARS is nullable, so we can skip if it's not present
        if (check(RecSPLLexer.TokenType.TEXT)) {
            parseVarDecl();
            if (check(RecSPLLexer.TokenType.COMMA)) {
                match(RecSPLLexer.TokenType.COMMA);
                parseGlobVars();  // Recursively parse more global variables
            }
        }
    }

    // Parse ALGO (ALGO ::= begin INSTRUC end)
    private void parseAlgo() {
        match(RecSPLLexer.TokenType.BEGIN);
        parseInstruc();   // Match INSTRUC (nullable)
        match(RecSPLLexer.TokenType.END);
    }

    // Parse INSTRUC (INSTRUC ::= nullable | COMMAND ; INSTRUC)
    private void parseInstruc() {
        if (check(RecSPLLexer.TokenType.END)) {
            return;  // Nullable, so we stop if we encounter 'end'
        }

        parseCommand();
        match(RecSPLLexer.TokenType.SEMICOLON);
        parseInstruc();  // Recursively parse more instructions
    }

    // Parse a COMMAND (COMMAND ::= skip | halt | print ATOMIC | ASSIGN | CALL | BRANCH)
    private CommandNode parseCommand() {
        if (check(RecSPLLexer.TokenType.SKIP)) {
            match(RecSPLLexer.TokenType.SKIP);
            return new CommandNode("skip");
        } else if (check(RecSPLLexer.TokenType.HALT)) {
            match(RecSPLLexer.TokenType.HALT);
            return new CommandNode("halt");
        } else if (check(RecSPLLexer.TokenType.PRINT)) {
            match(RecSPLLexer.TokenType.PRINT);
            ASTNode atomic = parseAtomic();
            return new PrintCommandNode(atomic);
        } else if (check(RecSPLLexer.TokenType.VNAME)) {
            return parseAssign();
        } else {
            throw new RuntimeException("Unexpected command token: " + currentToken());
        }
    }

    // Parse an assignment (ASSIGN ::= VNAME < input | VNAME = TERM)
    private CommandNode parseAssign() {
        ASTNode vname = new VariableNode(currentToken().getValue());
        match(RecSPLLexer.TokenType.VNAME);

        if (check(RecSPLLexer.TokenType.INPUT)) {
            match(RecSPLLexer.TokenType.INPUT);  // VNAME < input
            return new InputAssignCommandNode(vname);
        } else if (check(RecSPLLexer.TokenType.ASSIGNMENT)) {
            match(RecSPLLexer.TokenType.ASSIGNMENT);  // VNAME = TERM
            ASTNode term = parseTerm();
            return new AssignCommandNode(vname, term);
        } else {
            throw new RuntimeException("Expected token ASSIGNMENT or INPUT but found " + currentToken().getType());
        }
    }

    // Parse a TERM (TERM ::= ATOMIC | CALL | OP)
    private ASTNode parseTerm() {
        if (check(RecSPLLexer.TokenType.VNAME) || check(RecSPLLexer.TokenType.NUMBER)) {
            return parseAtomic();
        } else if (check(RecSPLLexer.TokenType.CALL)) {
            return parseCall();
        } else if (check(RecSPLLexer.TokenType.OPERATOR) || check(RecSPLLexer.TokenType.UNOP)) {
            return parseOp();
        } else {
            throw new RuntimeException("Unexpected term token: " + currentToken());
        }
    }

    // Parse ATOMIC (ATOMIC ::= VNAME | CONST)
    private ASTNode parseAtomic() {
        if (check(RecSPLLexer.TokenType.VNAME)) {
            ASTNode vname = new VariableNode(currentToken().getValue());
            match(RecSPLLexer.TokenType.VNAME);
            return vname;
        } else if (check(RecSPLLexer.TokenType.NUMBER) || check(RecSPLLexer.TokenType.STRING)) {
            ASTNode constant = new ConstantNode(currentToken().getValue());
            match(currentToken().getType());
            return constant;
        } else {
            throw new RuntimeException("Unexpected atomic token: " + currentToken());
        }
    }

    // Parse variable declarations (VTYP VNAME)
    private void parseVarDecl() {
        match(RecSPLLexer.TokenType.TEXT);  // Match VTYP (num, text)
        match(RecSPLLexer.TokenType.VNAME);  // Match VNAME
    }

    // Parse functions (nullable) (FUNCTIONS ::= DECL FUNCTIONS)
    private void parseFunctions() {
        if (check(RecSPLLexer.TokenType.NUM)) {
            parseDecl();
            parseFunctions();  // Recursively parse more functions
        }
    }

    // Parse a function declaration (DECL ::= HEADER BODY)
    private void parseDecl() {
        parseHeader();
        parseBody();
    }

    // Parse function header (HEADER ::= FTYP FNAME(VNAME, VNAME, VNAME))
    private void parseHeader() {
        match(RecSPLLexer.TokenType.TEXT);
        match(RecSPLLexer.TokenType.FNAME);
        match(RecSPLLexer.TokenType.LPAREN);
        match(RecSPLLexer.TokenType.VNAME);
        match(RecSPLLexer.TokenType.COMMA);
        match(RecSPLLexer.TokenType.VNAME);
        match(RecSPLLexer.TokenType.COMMA);
        match(RecSPLLexer.TokenType.VNAME);
        match(RecSPLLexer.TokenType.RPAREN);
    }

    // Parse function body (BODY ::= PROLOG LOCVARS ALGO EPILOG SUBFUNCS end)
    private void parseBody() {
        match(RecSPLLexer.TokenType.LPAREN);
        parseLocVars();
        parseAlgo();
        match(RecSPLLexer.TokenType.RPAREN);
        parseFunctions();  // SUBFUNCS
        match(RecSPLLexer.TokenType.END);
    }

    // Parse local variables (LOCVARS ::= VTYP VNAME , VTYP VNAME , VTYP VNAME ,)
    private void parseLocVars() {
        parseVarDecl();
        match(RecSPLLexer.TokenType.COMMA);
        parseVarDecl();
        match(RecSPLLexer.TokenType.COMMA);
        parseVarDecl();
        match(RecSPLLexer.TokenType.COMMA);
    }

    // Parse a function call (CALL ::= FNAME( ATOMIC , ATOMIC , ATOMIC ))
    private ASTNode parseCall() {
        match(RecSPLLexer.TokenType.FNAME);         // Match function name (FNAME)
        match(RecSPLLexer.TokenType.LPAREN);        // Match opening parenthesis '('

        ASTNode arg1 = parseAtomic();   // Parse the first argument
        match(RecSPLLexer.TokenType.COMMA);         // Match comma
        ASTNode arg2 = parseAtomic();   // Parse the second argument
        match(RecSPLLexer.TokenType.COMMA);         // Match comma
        ASTNode arg3 = parseAtomic();   // Parse the third argument

        match(RecSPLLexer.TokenType.RPAREN);        // Match closing parenthesis ')'

        // Return a node representing the function call
        return new FunctionCallNode(arg1, arg2, arg3);
    }

    // Parse an operation (OP ::= UNOP( ARG ) | BINOP( ARG , ARG ))
    private ASTNode parseOp() {
        if (check(RecSPLLexer.TokenType.UNOP)) {
            // Parse unary operation (UNOP( ARG ))
            RecSPLLexer.TokenType unopType = currentToken().getType();
            match(RecSPLLexer.TokenType.UNOP);       // Match the unary operator
            match(RecSPLLexer.TokenType.LPAREN);     // Match opening parenthesis '('
            ASTNode arg = parseAtomic(); // Parse the argument
            match(RecSPLLexer.TokenType.RPAREN);     // Match closing parenthesis ')'

            // Return a node representing the unary operation
            return new UnaryOpNode(unopType, arg);
        } else if (check(RecSPLLexer.TokenType.OPERATOR)) {
            // Parse binary operation (BINOP( ARG , ARG ))
            RecSPLLexer.TokenType binopType = currentToken().getType();
            match(RecSPLLexer.TokenType.OPERATOR);       // Match the binary operator
            match(RecSPLLexer.TokenType.LPAREN);      // Match opening parenthesis '('
            ASTNode arg1 = parseAtomic(); // Parse the first argument
            match(RecSPLLexer.TokenType.COMMA);       // Match comma
            ASTNode arg2 = parseAtomic(); // Parse the second argument
            match(RecSPLLexer.TokenType.RPAREN);      // Match closing parenthesis ')'

            // Return a node representing the binary operation
            return new BinaryOpNode(binopType, arg1, arg2);
        } else {
            throw new RuntimeException("Unexpected operation token: " + currentToken().getType());
        }
    }

    private ASTNode parseASTGlobalVars() {
        if (check(RecSPLLexer.TokenType.NUM) || check(RecSPLLexer.TokenType.TEXT)) {
            // Parse global variables
            // Example implementation
            ASTNode var = parseVarDecl();
            ASTNode moreVars = parseASTGlobalVars(); // Recursively parse more variables
            return new GlobalVarsNode(var, moreVars); // Assume `GlobalVarsNode` is defined
        }
        return new EmptyNode(); // Assuming `EmptyNode` represents no global variables
    }

    private ASTNode parseASTAlgorithm() {
        match(RecSPLLexer.TokenType.BEGIN);
        ASTNode instructions = parseInstructions(); // Implement `parseInstructions` as needed
        match(RecSPLLexer.TokenType.END);
        return new AlgorithmNode(instructions); // Assume `AlgorithmNode` is defined
    }

    private ASTNode parseASTFunctions() {
        if (check(RecSPLLexer.TokenType.FTYPE_NUM) || check(RecSPLLexer.TokenType.FTYPE_VOID)) {
            // Parse functions
            ASTNode function = parseFunction();
            ASTNode moreFunctions = parseFunctions(); // Recursively parse more functions
            return new FunctionsNode(function, moreFunctions); // Assume `FunctionsNode` is defined
        }
        return new EmptyNode(); // Assuming `EmptyNode` represents no functions
    }

    public class ProgramNode {
        private ASTNode globalVars;
        private ASTNode algorithm;
        private ASTNode functions;

        public ProgramNode(ASTNode globalVars, ASTNode algorithm, ASTNode functions) {
            this.globalVars = globalVars;
            this.algorithm = algorithm;
            this.functions = functions;
        }

        public ASTNode getGlobalVars() {
            return globalVars;
        }

        public ASTNode getAlgorithm() {
            return algorithm;
        }

        public ASTNode getFunctions() {
            return functions;
        }

        @Override
        public String toString() {
            return "ProgramNode(globalVars: " + globalVars + ", algorithm: " + algorithm + ", functions: " + functions + ")";
        }
    }
}
