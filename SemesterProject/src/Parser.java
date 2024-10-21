import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Parser {

    private List<Token> tokens; // List to store the token stream
    private int currentTokenIndex; // Current position in the token stream
    private Token currentToken; // The current token
    private Document xmlDoc;
    private Element rootElement;

    // Constructor to initialize the parser with tokens from XML
    public Parser(String filePath) {
        tokens = new ArrayList<>();
        currentTokenIndex = 0;
        loadTokensFromXML(filePath);
        currentToken = tokens.get(currentTokenIndex); // Initialize with the first token
        initXMLDocument();
    }

    // Initialize the XML Document for the parse tree
    private void initXMLDocument() {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            xmlDoc = dBuilder.newDocument();

            // Create root element for the parse tree
            rootElement = xmlDoc.createElement("PARSETREE");
            xmlDoc.appendChild(rootElement);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeXMLToFile(String outputFilePath) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(xmlDoc);

            StreamResult result = new StreamResult(new File(outputFilePath));
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(source, result);

            System.out.println("Parse tree written to: " + outputFilePath);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    // Method to load tokens from the XML file
    private void loadTokensFromXML(String filePath) {
        try {
            File inputFile = new File(filePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            NodeList tokenList = doc.getElementsByTagName("TOK");

            for (int i = 0; i < tokenList.getLength(); i++) {
                Node tokenNode = tokenList.item(i);

                if (tokenNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element tokenElement = (Element) tokenNode;

                    String id = tokenElement.getElementsByTagName("ID").item(0).getTextContent();
                    String tokenClass = tokenElement.getElementsByTagName("CLASS").item(0).getTextContent();
                    String word = tokenElement.getElementsByTagName("WORD").item(0).getTextContent();

                    tokens.add(new Token( Token.TokenType.valueOf(tokenClass.toUpperCase()), word));

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to move to the next token
    private void nextToken() {
        if (currentTokenIndex < tokens.size() - 1) {
            currentToken = tokens.get(++currentTokenIndex);
        } else {
            currentToken = new Token(Token.TokenType.EOF, "EOF"); // End of file token
        }
    }

    // Match current token class with expected class and consume it
    private void match(Token.TokenType expectedClass, Element parentElement) {
        //System.out.println("Current Token: " + currentToken.getType() + " Current value: " + currentToken.getValue());
        if (currentToken.getType().equals(expectedClass)) {
            Element tokenElement = xmlDoc.createElement("TOKEN");
            tokenElement.setAttribute("class", currentToken.getType().name());
            tokenElement.setAttribute("word", currentToken.getValue());
            parentElement.appendChild(tokenElement);
            nextToken(); // Move to the next token
        } else {
            throw new ParseException("Expected " + expectedClass + " but found " + currentToken);
        }
    }

    // Method to parse the entire program (PROG)
    public void parsePROG() {
        Element progElement = xmlDoc.createElement("PROG");
        rootElement.appendChild(progElement);
        match(Token.TokenType.MAIN, progElement);           // Expect 'main'
        Element globVarsElement = xmlDoc.createElement("GLOBVARS");
        progElement.appendChild(globVarsElement);
        parseGLOBVARS(globVarsElement);         // GLOBVARS rule
        parseALGO(progElement);             // ALGO rule
        //System.out.println("Parsing function");
        parseFunctions(progElement);        // FUNCTIONS rule
        match(Token.TokenType.EOF, progElement);          // Expect end of file
        writeXMLToFile("Parser_out.xml");
    }

    // Non-terminal: GLOBVARS ::= ε | VTYP VNAME , GLOBVARS
    public void parseGLOBVARS(Element globVarsElement) {

        if (currentToken.getType().equals(Token.TokenType.NUM) || currentToken.getType().equals(Token.TokenType.TEXT)) {
            parseVTYP(globVarsElement);           // VTYP
            parseVNAME(globVarsElement);          // VNAME
            match(Token.TokenType.COMMA, globVarsElement);        // Expect a comma
            parseGLOBVARS(globVarsElement);       // Recursive call for remaining GLOBVARS
        }
        // ε (nullable rule - do nothing if no match)
    }

    // Non-terminal: ALGO ::= begin INSTRUC end
    public void parseALGO(Element parentElement) {
        Element algoElement = xmlDoc.createElement("ALGO");
        parentElement.appendChild(algoElement);
        match(Token.TokenType.BEGIN, algoElement);            // Expect 'begin'
        parseINSTRUC(algoElement);            // INSTRUC
        match(Token.TokenType.END, algoElement);              // Expect 'end'
    }

    // Non-terminal: INSTRUC ::= ε | COMMAND ; INSTRUC
    public void parseINSTRUC(Element parentElement) {
        Element instrucElement = xmlDoc.createElement("INSTRUC");
        parentElement.appendChild(instrucElement);
        if (isCommandStart()) {    // Check if it's the start of a COMMAND
            parseCOMMAND(instrucElement);        // COMMAND
            match(Token.TokenType.SEMICOLON, instrucElement);    // Expect ';'
            parseINSTRUC(instrucElement);        // Recursive call for more INSTRUC
        }
        // ε (nullable rule - do nothing if no match)
    }

    private boolean isCommandStart() {
        return currentToken.getType().equals(Token.TokenType.SKIP) ||
                currentToken.getType().equals(Token.TokenType.HALT) ||
                currentToken.getType().equals(Token.TokenType.PRINT) ||
                currentToken.getType().equals(Token.TokenType.RETURN) ||
                currentToken.getType().equals(Token.TokenType.VNAME) ||
                currentToken.getType().equals(Token.TokenType.FNAME) ||
                currentToken.getType().equals(Token.TokenType.IF);
    }

    // Non-terminal: COMMAND ::= skip | halt | print ATOMIC | ASSIGN | CALL | BRANCH | return ATOMIC
    public void parseCOMMAND(Element parentElement) {
        Element commandElement = xmlDoc.createElement("COMMAND");
        parentElement.appendChild(commandElement);
        switch (currentToken.getType()) {
            case SKIP:
                match(Token.TokenType.SKIP, commandElement);
                break;
            case HALT:
                match(Token.TokenType.HALT, commandElement);
                break;
            case PRINT:
                match(Token.TokenType.PRINT, commandElement);
                parseATOMIC(commandElement);
                break;
            case RETURN:
                match(Token.TokenType.RETURN, commandElement);
                parseATOMIC(commandElement);
                break;
            case VNAME:
                parseASSIGN(commandElement);
                break;
            case FNAME:
                parseCALL(commandElement);
                break;
            case IF:
                parseBRANCH(commandElement);
                break;
            default:
                throw new ParseException("Unexpected command");
        }
    }

    // Non-terminal: CONST ::= String | number
    public void parseCONST(Element parentElement) {
        Element constElement = xmlDoc.createElement("CONST");
        parentElement.appendChild(constElement);
        if (currentToken.getType() == Token.TokenType.STRING) {
            match(Token.TokenType.STRING, constElement);
        } else if (currentToken.getType() == Token.TokenType.NUMBER) {
            match(Token.TokenType.NUMBER, constElement);
        } else {
            throw new ParseException("Expected a constant");
        }
    }

    // Non-terminal: ASSIGN ::= VNAME < input | VNAME = TERM
    public void parseASSIGN(Element parentElement) {
        Element assignElement = xmlDoc.createElement("ASSIGN");
        parentElement.appendChild(assignElement);
        parseVNAME(assignElement);
        if (currentToken.getType() == Token.TokenType.INPUT) {
            match(Token.TokenType.INPUT, assignElement);
        } else if (currentToken.getType() == Token.TokenType.ASSIGNMENT) {
            match(Token.TokenType.ASSIGNMENT, assignElement);
            parseTERM(assignElement);
        } else {
            throw new ParseException("Expected assignment operator");
        }
    }

    // Non-terminal: CALL ::= FNAME( ATOMIC , ATOMIC , ATOMIC )
    public void parseCALL(Element parentElement) {
        Element parseCaLL = xmlDoc.createElement("CALL");
        parentElement.appendChild(parseCaLL);
        match(Token.TokenType.FNAME, parseCaLL);
        match(Token.TokenType.LPAREN, parseCaLL);
        parseATOMIC(parseCaLL);
        match(Token.TokenType.COMMA, parseCaLL);
        parseATOMIC(parseCaLL);
        match(Token.TokenType.COMMA, parseCaLL);
        parseATOMIC(parseCaLL);
        match(Token.TokenType.RPAREN, parseCaLL);
    }

    // Non-terminal: BRANCH ::= if COND then ALGO else ALGO
    public void parseBRANCH(Element parentElement) {
        Element branchElement = xmlDoc.createElement("BRANCH");
        parentElement.appendChild(branchElement);
        match(Token.TokenType.IF, branchElement);
        parseCOND(branchElement);
        match(Token.TokenType.THEN, branchElement);
        parseALGO(branchElement);
        match(Token.TokenType.ELSE, branchElement);
        parseALGO(branchElement);
    }

    // Non-terminal: COND ::= SIMPLE | COMPOSIT
    public void parseCOND(Element parentElement) {
        Element condElement = xmlDoc.createElement("COND");
        parentElement.appendChild(condElement);
        if (isSimpleCond()) {
            parseSIMPLE(condElement);
        } else {
            parseCOMPOSIT(condElement);
        }
    }

    private boolean isSimpleCond() {
        return currentToken.getType() == Token.TokenType.BINOP;
    }

    // Non-terminal: SIMPLE ::= BINOP( ATOMIC , ATOMIC )
    public void parseSIMPLE(Element parentElement) {
        Element simpleElement = xmlDoc.createElement("SIMPLE");
        parentElement.appendChild(simpleElement);
        match(Token.TokenType.BINOP,simpleElement);
        match(Token.TokenType.LPAREN, simpleElement);
        parseATOMIC(simpleElement);
        match(Token.TokenType.COMMA, simpleElement);
        parseATOMIC(simpleElement);
        match(Token.TokenType.RPAREN, simpleElement);
    }

    // Non-terminal: COMPOSIT ::= BINOP( SIMPLE , SIMPLE ) | UNOP( SIMPLE )
    public void parseCOMPOSIT(Element parentElement) {
        Element compositElement = xmlDoc.createElement("COMPOSIT");
        parentElement.appendChild(compositElement);
        if (currentToken.getType() == Token.TokenType.BINOP) {
            match(Token.TokenType.BINOP, compositElement);
            match(Token.TokenType.LPAREN, compositElement);
            parseSIMPLE(compositElement);
            match(Token.TokenType.COMMA, compositElement);
            parseSIMPLE(compositElement);
            match(Token.TokenType.RPAREN, compositElement);
        } else if (currentToken.getType() == Token.TokenType.UNOP) {
            match(Token.TokenType.UNOP, compositElement);
            match(Token.TokenType.LPAREN, compositElement);
            parseSIMPLE(compositElement);
            match(Token.TokenType.RPAREN, compositElement);
        } else {
            throw new ParseException("Expected composite condition");
        }
    }

    // Non-terminal: VTYP ::= num | text
    public void parseVTYP(Element parentElement) {
        Element vtypeElement = xmlDoc.createElement("VTYP");
        parentElement.appendChild(vtypeElement);
        if (currentToken.getType().equals(Token.TokenType.NUM)) {
            match(Token.TokenType.NUM, vtypeElement);  // Expect 'num'
        } else if (currentToken.getType().equals(Token.TokenType.TEXT)) {
            match(Token.TokenType.TEXT, vtypeElement); // Expect 'text'
        } else {
            throw new ParseException("Expected 'num' or 'text' but found: " + currentToken);
        }
    }

    // Non-terminal: VNAME ::= V_[a-z]([a-z]|[0-9])*
    public void parseVNAME(Element parentElement) {
        Element vnameElement = xmlDoc.createElement("VNAME");
        parentElement.appendChild(vnameElement);
        if (currentToken.getType().equals(Token.TokenType.VNAME)) {
            match(Token.TokenType.VNAME, vnameElement);  // Expect a variable name token (e.g., V_[a-z])
        } else {
            throw new ParseException("Expected 'VNAME' but found: " + currentToken);
        }
    }

    // Non-terminal: TERM ::= ATOMIC | CALL | OP
    public void parseTERM(Element parentElement) {
        Element termElement = xmlDoc.createElement("TERM");
        parentElement.appendChild(termElement);
        if (currentToken.getType().equals(Token.TokenType.VNAME) || currentToken.getType().equals(Token.TokenType.NUMBER) || currentToken.getType().equals(Token.TokenType.STRING)) {
            parseATOMIC(termElement);  // ATOMIC case
        } else if (currentToken.getType().equals(Token.TokenType.FNAME)) {
            parseCALL(termElement);    // CALL case
        } else if (currentToken.getType().equals(Token.TokenType.UNOP) || currentToken.getType().equals(Token.TokenType.BINOP)) {
            parseOP(termElement);      // OP case
        } else {
            throw new ParseException("Expected 'ATOMIC', 'CALL', or 'OP' but found: " + currentToken);
        }
    }

    public class ParseException extends RuntimeException {
        public ParseException(String message) {
            super(message);
        }
    }

    // Non-terminal: ATOMIC ::= VNAME | CONST
    public void parseATOMIC(Element parentElement) {
        Element atomicElement = xmlDoc.createElement("ATOMIC");
        parentElement.appendChild(atomicElement);
        if (currentToken.getType().equals(Token.TokenType.VNAME)) {
            parseVNAME(atomicElement);  // ATOMIC can be a variable name
        } else if (currentToken.getType().equals(Token.TokenType.NUMBER) || currentToken.getType().equals(Token.TokenType.STRING)) {
            parseCONST(atomicElement);  // ATOMIC can be a constant
        } else {
            throw new ParseException("Expected 'VNAME' or 'CONST' but found: " + currentToken);
        }
    }

    // Non-terminal: OP ::= UNOP( ARG ) | BINOP( ARG , ARG )
    public void parseOP(Element parentElement) {
        Element opElement = xmlDoc.createElement("OP");
        parentElement.appendChild(opElement);
        if (currentToken.getType().equals(Token.TokenType.UNOP)) {
            parseUNOP(opElement);  // Parse unary operator
        } else if (currentToken.getType().equals(Token.TokenType.BINOP)) {
            parseBINOP(opElement); // Parse binary operator
        } else {
            throw new ParseException("Expected 'UNOP' or 'BINOP' but found: " + currentToken);
        }
    }

    // Parsing unary operator
    public void parseUNOP(Element parentElement) {
        Element commandElement = xmlDoc.createElement("UNOP");
        parentElement.appendChild(commandElement);
        match(Token.TokenType.UNOP, commandElement);       // Expect a unary operator (e.g., not, sqrt)
        match(Token.TokenType.LPAREN, commandElement);  // Expect '('
        parseARG(commandElement);           // Parse the argument
        match(Token.TokenType.RPAREN, commandElement); // Expect ')'
    }

    // Parsing binary operator
    public void parseBINOP(Element parentElement) {
        Element commandElement = xmlDoc.createElement("BINOP");
        parentElement.appendChild(commandElement);
        match(Token.TokenType.BINOP, commandElement);      // Expect a binary operator (e.g., add, mul, eq)
        match(Token.TokenType.LPAREN, commandElement);  // Expect '('
        parseARG(commandElement);           // First argument
        match(Token.TokenType.COMMA, commandElement);       // Expect ','
        parseARG(commandElement);           // Second argument
        match(Token.TokenType.RPAREN, commandElement); // Expect ')'
    }

    // Non-terminal: ARG ::= ATOMIC | OP
    public void parseARG(Element parentElement) {
        Element commandElement = xmlDoc.createElement("ARG");
        parentElement.appendChild(commandElement);
        if (currentToken.getType().equals(Token.TokenType.VNAME) || currentToken.getType().equals(Token.TokenType.NUMBER) || currentToken.getType().equals(Token.TokenType.STRING)) {
            parseATOMIC(commandElement);  // ARG can be an atomic value
        } else if (currentToken.getType().equals(Token.TokenType.UNOP) || currentToken.getType().equals(Token.TokenType.BINOP)) {
            parseOP(commandElement);      // ARG can be an operation
        } else {
            throw new ParseException("Expected 'ATOMIC' or 'OP' but found: " + currentToken);
        }
    }

    // Non-terminal: FUNCTIONS ::= (nullable) | DECL FUNCTIONS
    public void parseFunctions(Element parentElement) {
        Element commandElement = xmlDoc.createElement("FUNCTIONS");
        parentElement.appendChild(commandElement);
        // Check if the current token indicates the start of a function declaration (FTYP FNAME ...).
        if (currentToken.getType().equals(Token.TokenType.NUM) || currentToken.getType().equals(Token.TokenType.VOID)) {
            parseDecl(commandElement);     // Parse a DECL (function declaration)
            parseFunctions(commandElement); // Recursively parse more FUNCTIONS
        }
        // Otherwise, it's nullable, so do nothing.
    }

    // Non-terminal: DECL ::= HEADER BODY
    public void parseDecl(Element parentElement) {
        Element commandElement = xmlDoc.createElement("DECL");
        parentElement.appendChild(commandElement);
        parseHeader(commandElement);  // Parse the function header
        parseBody(commandElement);    // Parse the function body
    }

    // Non-terminal: HEADER ::= FTYP FNAME( VNAME , VNAME , VNAME )
    public void parseHeader(Element parentElement) {
        Element commandElement = xmlDoc.createElement("HEADER");
        parentElement.appendChild(commandElement);
        parseFTYP(commandElement);       // Parse the return type (FTYP -> num | void)
        match(Token.TokenType.FNAME, commandElement);    // Match the function name
        match(Token.TokenType.LPAREN, commandElement);  // Expect '('
        parseVNAME(commandElement);      // Parse the first argument
        match(Token.TokenType.COMMA, commandElement);    // Expect ','
        parseVNAME(commandElement);      // Parse the second argument
        match(Token.TokenType.COMMA,commandElement);    // Expect ','
        parseVNAME(commandElement);      // Parse the third argument
        match(Token.TokenType.RPAREN,commandElement); // Expect ')'
    }

    // Non-terminal: FTYP ::= num | void
    public void parseFTYP(Element parentElement) {
        Element commandElement = xmlDoc.createElement("FTYP");
        parentElement.appendChild(commandElement);
        if (currentToken.getType().equals(Token.TokenType.NUM)) {
            match(Token.TokenType.NUM,commandElement);  // Expect 'num'
        } else if (currentToken.getType().equals(Token.TokenType.VOID)) {
            match(Token.TokenType.VOID, commandElement); // Expect 'void'
        } else {
            throw new ParseException("Expected 'num' or 'void' but found: " + currentToken);
        }
    }

    // Non-terminal: BODY ::= PROLOG LOCVARS ALGO EPILOG SUBFUNCS end
    public void parseBody(Element parentElement) {
        Element commandElement = xmlDoc.createElement("BODY");
        parentElement.appendChild(commandElement);
        parseProlog(commandElement);    // Parse '{'
        parseLocvars(commandElement);   // Parse local variables
        parseAlgo(commandElement);      // Parse the algorithm (the main part of the function)
        parseEpilog(commandElement);    // Parse '}'
        parseSubfuncs(commandElement);  // Parse any nested functions (if any)
        match(Token.TokenType.END,commandElement);     // Expect 'end'
    }

    // Non-terminal: PROLOG ::= {
    public void parseProlog(Element parentElement) {
        Element commandElement = xmlDoc.createElement("PROLOG");
        parentElement.appendChild(commandElement);
        match(Token.TokenType.LBRACE, commandElement);  // Expect '{'
    }

    // Non-terminal: LOCVARS ::= VTYP VNAME , VTYP VNAME , VTYP VNAME ,
    public void parseLocvars(Element parentElement) {
        Element commandElement = xmlDoc.createElement("LOCALVARS");
        parentElement.appendChild(commandElement);
        parseVTYP(commandElement);      // Parse the first variable's type
        parseVNAME(commandElement);     // Parse the first variable's name
        match(Token.TokenType.COMMA, commandElement);   // Expect ','
        parseVTYP(commandElement);      // Parse the second variable's type
        parseVNAME(commandElement);     // Parse the second variable's name
        match(Token.TokenType.COMMA, commandElement);   // Expect ','
        parseVTYP(commandElement);      // Parse the third variable's type
        parseVNAME(commandElement);     // Parse the third variable's name
        match(Token.TokenType.COMMA, commandElement);   // Expect ',' (trailing comma)
    }

    // Non-terminal: ALGO ::= begin INSTRUC end
    public void parseAlgo(Element parentElement) {
        Element commandElement = xmlDoc.createElement("ALGO");
        parentElement.appendChild(commandElement);
        match(Token.TokenType.BEGIN, commandElement);  // Expect 'begin'
        parseINSTRUC(commandElement);  // Parse instructions
        match(Token.TokenType.END, commandElement);    // Expect 'end'
    }

    // Non-terminal: EPILOG ::= }
    public void parseEpilog(Element parentElement) {
        Element commandElement = xmlDoc.createElement("EPILOG");
        parentElement.appendChild(commandElement);
        match(Token.TokenType.RBRACE, commandElement);  // Expect '}'
    }

    // Non-terminal: SUBFUNCS ::= FUNCTIONS
    public void parseSubfuncs(Element parentElement) {
        Element commandElement = xmlDoc.createElement("SUBFUNCTION");
        parentElement.appendChild(commandElement);
        parseFunctions(commandElement);  // Parse any nested functions
    }

    public void outputParseTree(String outputFilePath) {
        writeXMLToFile(outputFilePath);
    }

}