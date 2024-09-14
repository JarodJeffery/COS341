public class Parser {
    private final Lexer lexer;
    private Token currentToken;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        this.currentToken = lexer.getCurrentToken();
    }

    // Utility methods to help with token matching and error checking
    private void eat(Token.TokenType type) throws Exception {
        if (currentToken.type == type) {
            lexer.advance();
            currentToken = lexer.getCurrentToken();
        } else {
            throw new Exception("Syntax Error: Expected " + type + ", but found " + currentToken);
        }
    }

    // Main entry point of the parser
    public void parseProgram() throws Exception {
        parseMain();
        parseGlobVars();
        parseAlgo();
        parseFunctions();
    }

    // Parsing for MAIN token
    private void parseMain() throws Exception {
        eat(Token.TokenType.MAIN);
    }

    // Parsing global variables
    private void parseGlobVars() throws Exception {
        if (currentToken.type == Token.TokenType.NUM || currentToken.type == Token.TokenType.TEXT) {
            parseVType();
            eat(Token.TokenType.VNAME);
            while (currentToken.type == Token.TokenType.COMMA) {
                eat(Token.TokenType.COMMA);
                parseVType();
                eat(Token.TokenType.VNAME);
            }
        }
    }

    private void parseVType() throws Exception {
        if (currentToken.type == Token.TokenType.NUM) {
            eat(Token.TokenType.NUM);
        } else if (currentToken.type == Token.TokenType.TEXT) {
            eat(Token.TokenType.TEXT);
        } else {
            throw new Exception("Syntax Error: Expected variable type, found " + currentToken);
        }
    }

    // Parsing algorithm block
    private void parseAlgo() throws Exception {
        eat(Token.TokenType.BEGIN);
        parseInstruc();
        eat(Token.TokenType.END);
    }

    private void parseInstruc() throws Exception {
        if (currentToken.type == Token.TokenType.SKIP || currentToken.type == Token.TokenType.HALT || currentToken.type == Token.TokenType.PRINT || currentToken.type == Token.TokenType.VNAME || currentToken.type == Token.TokenType.IF) {
            parseCommand();
            while (currentToken.type == Token.TokenType.SEMICOLON) {
                eat(Token.TokenType.SEMICOLON);
                parseCommand();
            }
        }
    }

    private void parseCommand() throws Exception {
        if (currentToken.type == Token.TokenType.SKIP) {
            eat(Token.TokenType.SKIP);
        } else if (currentToken.type == Token.TokenType.HALT) {
            eat(Token.TokenType.HALT);
        } else if (currentToken.type == Token.TokenType.PRINT) {
            eat(Token.TokenType.PRINT);
            parseAtomic();
        } else if (currentToken.type == Token.TokenType.VNAME) {
            parseAssign();
        } else if (currentToken.type == Token.TokenType.IF) {
            parseBranch();
        } else {
            System.out.println("Instruct is null");
        }
    }

    private void parseAssign() throws Exception {
        eat(Token.TokenType.VNAME);
        if (currentToken.type == Token.TokenType.ASSIGNMENT) {
            eat(Token.TokenType.ASSIGNMENT);
            if (currentToken.type == Token.TokenType.INPUT) {
                eat(Token.TokenType.INPUT);
            } else {
                parseTerm();
            }
        }
    }

    private void parseBranch() throws Exception {
        eat(Token.TokenType.IF);
        parseCond();
        eat(Token.TokenType.THEN);
        parseAlgo();
        eat(Token.TokenType.ELSE);
        parseAlgo();
    }

    private void parseTerm() throws Exception {
        if (currentToken.type == Token.TokenType.VNAME || currentToken.type == Token.TokenType.NUMBER || currentToken.type == Token.TokenType.STRING) {
            parseAtomic();
        } else if (currentToken.type == Token.TokenType.FNAME) {
            parseCall();
        } else if (currentToken.type == Token.TokenType.UNOP || currentToken.type == Token.TokenType.OPERATOR) {
            parseOp();
        } else {
            throw new Exception("Syntax Error: Unexpected token in TERM, found " + currentToken);
        }
    }

    private void parseAtomic() throws Exception {
        if (currentToken.type == Token.TokenType.VNAME) {
            eat(Token.TokenType.VNAME);
        } else if (currentToken.type == Token.TokenType.NUMBER) {
            eat(Token.TokenType.NUMBER);
        } else if (currentToken.type == Token.TokenType.STRING) {
            eat(Token.TokenType.STRING);
        } else {
            throw new Exception("Syntax Error: Expected ATOMIC, found " + currentToken);
        }
    }

    private void parseCall() throws Exception {
        eat(Token.TokenType.FNAME);
        eat(Token.TokenType.LPAREN);
        parseAtomic();
        eat(Token.TokenType.COMMA);
        parseAtomic();
        eat(Token.TokenType.COMMA);
        parseAtomic();
        eat(Token.TokenType.RPAREN);
    }

    private void parseOp() throws Exception {
        if (currentToken.type == Token.TokenType.UNOP) {
            eat(Token.TokenType.UNOP);
            eat(Token.TokenType.LPAREN);
            parseArg();
            eat(Token.TokenType.RPAREN);
        } else if (currentToken.type == Token.TokenType.OPERATOR) {
            eat(Token.TokenType.OPERATOR);
            eat(Token.TokenType.LPAREN);
            parseArg();
            eat(Token.TokenType.COMMA);
            parseArg();
            eat(Token.TokenType.RPAREN);
        }
    }

    private void parseArg() throws Exception {
        if (currentToken.type == Token.TokenType.VNAME || currentToken.type == Token.TokenType.NUMBER || currentToken.type == Token.TokenType.STRING) {
            parseAtomic();
        } else if (currentToken.type == Token.TokenType.UNOP || currentToken.type == Token.TokenType.OPERATOR) {
            parseOp();
        } else {
            throw new Exception("Syntax Error: Expected ARG, found " + currentToken);
        }
    }

    private void parseCond() throws Exception {
        if (currentToken.type == Token.TokenType.OPERATOR) {
            parseSimple();
        } else if (currentToken.type == Token.TokenType.UNOP) {
            parseComposite();
        } else {
            throw new Exception("Syntax Error: Expected a condition, found " + currentToken);
        }
    }

    private void parseSimple() throws Exception {
        eat(Token.TokenType.OPERATOR); // for binary operators like 'eq', 'grt', etc.
        eat(Token.TokenType.LPAREN);
        parseAtomic();  // first atomic argument
        eat(Token.TokenType.COMMA);
        parseAtomic();  // second atomic argument
        eat(Token.TokenType.RPAREN);
    }

    private void parseComposite() throws Exception {
        if (currentToken.type == Token.TokenType.UNOP) {  // Unary operation (e.g., not)
            eat(Token.TokenType.UNOP);
            eat(Token.TokenType.LPAREN);
            parseSimple();
            eat(Token.TokenType.RPAREN);
        } else if (currentToken.type == Token.TokenType.OPERATOR) {  // Binary operation (e.g., and, or)
            eat(Token.TokenType.OPERATOR);
            eat(Token.TokenType.LPAREN);
            parseSimple();
            eat(Token.TokenType.COMMA);
            parseSimple();
            eat(Token.TokenType.RPAREN);
        } else {
            throw new Exception("Syntax Error: Expected a composite condition, found " + currentToken);
        }
    }
    private void parseFunctions() throws Exception {
        if (currentToken.type == Token.TokenType.VOID || currentToken.type == Token.TokenType.NUM) {
            parseDecl();
            while (currentToken.type == Token.TokenType.VOID || currentToken.type == Token.TokenType.NUM) {
                parseDecl();
            }
        }
    }

    private void parseDecl() throws Exception {
        parseHeader();
        parseBody();
    }

    private void parseHeader() throws Exception {
        // Parse function type (e.g., num, void)
        if (currentToken.type == Token.TokenType.VOID) {
            eat(Token.TokenType.VOID);
        } else if (currentToken.type == Token.TokenType.NUM) {
            eat(Token.TokenType.NUM);
        } else {
            throw new Exception("Syntax Error: Expected function type (num or void), found " + currentToken);
        }

        // Parse function name
        eat(Token.TokenType.FNAME);

        // Parse function parameters: (VNAME, VNAME, VNAME)
        eat(Token.TokenType.LPAREN);
        eat(Token.TokenType.VNAME);
        eat(Token.TokenType.COMMA);
        eat(Token.TokenType.VNAME);
        eat(Token.TokenType.COMMA);
        eat(Token.TokenType.VNAME);
        eat(Token.TokenType.RPAREN);
    }

    private void parseBody() throws Exception {
        eat(Token.TokenType.LBRACE);  // Prolog: '{'

        // Parse local variables: VTYP VNAME, VTYP VNAME, VTYP VNAME
        parseLocVars();

        // Parse function algorithm (ALGO)
        parseAlgo();

        eat(Token.TokenType.RBRACE);  // Epilogue: '}'

        // Parse possible sub-functions
        parseFunctions();  // Recursive parsing of sub-functions
    }

    private void parseLocVars() throws Exception {
        for (int i = 0; i < 3; i++) {  // Expect exactly 3 local variables
            parseVType();
            eat(Token.TokenType.VNAME);
            if (i < 2) {
                eat(Token.TokenType.COMMA);
            }
        }
    }
}
