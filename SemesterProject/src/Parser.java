import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

public class Parser {
    private NodeList tokenNodes;  // List of tokens from the XML
    private int currentTokenIndex; // Pointer to current token in NodeList
    private Document xmlDocument;  // XML Document for output
    private Element rootElement;   // Root element for output XML

    // Constructor to initialize the parser with tokens from an XML file
    public Parser(String lexerXmlFile) throws Exception {
        // Load the Lexer XML
        loadLexerXml(lexerXmlFile);

        // Initialize XML document for output
        initializeXmlOutput();
    }

    // Loads and parses the XML file outputted by the Lexer
    private void loadLexerXml(String lexerXmlFile) throws Exception {
        File inputFile = new File(lexerXmlFile);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(inputFile);
        doc.getDocumentElement().normalize();

        // Extract all token elements
        tokenNodes = doc.getElementsByTagName("token");
        currentTokenIndex = 0;  // Start at the first token
    }

    // Initialize the XML output structure
    private void initializeXmlOutput() throws Exception {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        xmlDocument = docBuilder.newDocument();
        rootElement = xmlDocument.createElement("Program");
        xmlDocument.appendChild(rootElement);
    }

    // Fetch the current token from the NodeList
    private Token getCurrentToken() throws Exception {
        if (currentTokenIndex < tokenNodes.getLength()) {
            Element tokenElement = (Element) tokenNodes.item(currentTokenIndex);
            String tokenType = tokenElement.getElementsByTagName("type").item(0).getTextContent();
            String tokenValue = tokenElement.getElementsByTagName("value").item(0).getTextContent();
            return new Token(Token.TokenType.valueOf(tokenType), tokenValue);
        } else {
            return new Token(Token.TokenType.EOF, ""); // Return EOF token if no more tokens are available
        }
    }

    // Advance to the next token in the NodeList
    private void advanceToken() {
        currentTokenIndex++;
    }

    // Main entry point for parsing
    public void parseProgram() throws Exception {
        Element mainElement = xmlDocument.createElement("MainBlock");
        rootElement.appendChild(mainElement);

        parseMain(mainElement);
        parseGlobVars(mainElement);
        parseAlgo(mainElement);
        parseFunctions(mainElement);

        // Write the parsed structure to an XML file
        writeXMLToFile("parsed_program.xml");
    }

    // Method to eat a token and advance if it matches the expected type
    private void eat(Token.TokenType expectedType) throws Exception {
        Token currentToken = getCurrentToken();
        if (currentToken.type == expectedType) {
            advanceToken();
        } else {
            throw new Exception("Syntax Error: Expected " + expectedType + ", but found " + currentToken);
        }
    }

    // Parsing the MAIN block
    private void parseMain(Element parent) throws Exception {
        eat(Token.TokenType.MAIN);
        Element mainElement = xmlDocument.createElement("Main");
        parent.appendChild(mainElement);
    }

    // Parsing global variables
    private void parseGlobVars(Element parent) throws Exception {
        Element globalsElement = xmlDocument.createElement("GlobalVariables");
        parent.appendChild(globalsElement);

        while (getCurrentToken().type == Token.TokenType.NUM || getCurrentToken().type == Token.TokenType.TEXT) {
            parseVType(globalsElement); // Capture the variable type
            Element vNameElement = xmlDocument.createElement("VariableName"); // Create an element for the variable name
            vNameElement.setTextContent(getCurrentToken().value); // Set the variable name value
            globalsElement.appendChild(vNameElement); // Append the variable name to globalsElement
            eat(Token.TokenType.VNAME); // Move to the next token

            while (getCurrentToken().type == Token.TokenType.COMMA) {
                eat(Token.TokenType.COMMA); // Consume the comma
                parseVType(globalsElement); // Capture the next variable type
                vNameElement = xmlDocument.createElement("VariableName"); // Create a new element for the variable name
                vNameElement.setTextContent(getCurrentToken().value); // Set the variable name value
                globalsElement.appendChild(vNameElement); // Append the variable name to globalsElement
                eat(Token.TokenType.VNAME); // Move to the next token
            }
        }
    }


    private void parseVType(Element parent) throws Exception {
        if (getCurrentToken().type == Token.TokenType.NUM) {
            eat(Token.TokenType.NUM);
            Element numElement = xmlDocument.createElement("NumType");
            parent.appendChild(numElement);
        } else if (getCurrentToken().type == Token.TokenType.TEXT) {
            eat(Token.TokenType.TEXT);
            Element textElement = xmlDocument.createElement("TextType");
            parent.appendChild(textElement);
        } else {
            throw new Exception("Syntax Error: Expected variable type, found " + getCurrentToken());
        }
    }

    // Parsing algorithm block
    private void parseAlgo(Element parent) throws Exception {
        eat(Token.TokenType.BEGIN);
        Element algoElement = xmlDocument.createElement("Algorithm");
        parent.appendChild(algoElement);
        parseInstruc(algoElement);
        eat(Token.TokenType.END);
    }

    private void parseInstruc(Element parent) throws Exception {
        while (true) { // Loop to parse multiple instructions
            if (getCurrentToken().type == Token.TokenType.SKIP ||
                    getCurrentToken().type == Token.TokenType.HALT ||
                    getCurrentToken().type == Token.TokenType.PRINT ||
                    getCurrentToken().type == Token.TokenType.VNAME ||
                    getCurrentToken().type == Token.TokenType.IF) {

                parseCommand(parent); // Call parseCommand for each recognized command
            } else if (getCurrentToken().type == Token.TokenType.END) {
                break; // Exit on END token
            } else {
                break; // Break the loop if no valid instruction is found
            }
        }
    }


    private void parseCommand(Element parent) throws Exception {
        Token currentToken = getCurrentToken();
        if (currentToken.type == Token.TokenType.SKIP) {
            eat(Token.TokenType.SKIP);
            Element commandElement = xmlDocument.createElement("Command");
            commandElement.setAttribute("type", "Skip");
            parent.appendChild(commandElement);
        } else if (currentToken.type == Token.TokenType.HALT) {
            eat(Token.TokenType.HALT);
            Element commandElement = xmlDocument.createElement("Command");
            commandElement.setAttribute("type", "Halt");
            parent.appendChild(commandElement);
        } else if (currentToken.type == Token.TokenType.PRINT) {
            eat(Token.TokenType.PRINT); // Eat the PRINT token
            Element commandElement = xmlDocument.createElement("Command"); // Create Command element
            commandElement.setAttribute("type", "Print"); // Set the type attribute

            // Check for atomic value to print
            if (getCurrentToken().type == Token.TokenType.NUMBER) {
                Element atomicElement = xmlDocument.createElement("Atomic"); // Create Atomic element
                atomicElement.setAttribute("type", "NumberLiteral"); // Set atomic type
                atomicElement.setTextContent(getCurrentToken().value); // Set the value for the atomic element
                commandElement.appendChild(atomicElement); // Append atomic element to command
                eat(Token.TokenType.NUMBER); // Eat the NUMBER token
            }

            if (getCurrentToken().type == Token.TokenType.SEMICOLON) {
                eat(Token.TokenType.SEMICOLON); // Eat the SEMICOLON token
            }

            parent.appendChild(commandElement); // Append command element to the parent
        } else if (currentToken.type == Token.TokenType.VNAME) {
            Element commandElement = xmlDocument.createElement("Command");
            commandElement.setAttribute("type", "Assignment");
            parent.appendChild(commandElement);
            parseAssign(commandElement);
        } else if (currentToken.type == Token.TokenType.IF) {
            Element commandElement = xmlDocument.createElement("Command");
            commandElement.setAttribute("type", "IfStatement");
            parent.appendChild(commandElement);
            parseBranch(commandElement);
        } else {
            throw new Exception("Unrecognized command type: " + currentToken);
        }
    }



    private void parseAssign(Element parent) throws Exception {
        eat(Token.TokenType.VNAME);
        Element assignElement = xmlDocument.createElement("Assignment");
        parent.appendChild(assignElement);

        if (getCurrentToken().type == Token.TokenType.ASSIGNMENT) {
            eat(Token.TokenType.ASSIGNMENT);
            if (getCurrentToken().type == Token.TokenType.INPUT) {
                eat(Token.TokenType.INPUT);
                Element inputElement = xmlDocument.createElement("Input");
                assignElement.appendChild(inputElement);
            } else {
                parseTerm(assignElement);
            }
        }
    }

    private void parseBranch(Element parent) throws Exception {
        eat(Token.TokenType.IF);
        Element branchElement = xmlDocument.createElement("IfStatement");
        parent.appendChild(branchElement);

        parseCond(branchElement);
        eat(Token.TokenType.THEN);
        parseAlgo(branchElement);
        eat(Token.TokenType.ELSE);
        parseAlgo(branchElement);
    }

    private void parseTerm(Element parent) throws Exception {
        Element termElement = xmlDocument.createElement("Term");
        parent.appendChild(termElement);
        parseAtomic(termElement);
        parseOp(termElement);
        parseAtomic(termElement);
    }

    private void parseAtomic(Element parent) throws Exception {
        Token currentToken = getCurrentToken();
        Element atomicElement = xmlDocument.createElement("Atomic");
        parent.appendChild(atomicElement);

        if (currentToken.type == Token.TokenType.VNAME) {
            eat(Token.TokenType.VNAME);
            atomicElement.setAttribute("type", "Variable");
        } else if (currentToken.type == Token.TokenType.STRING) {
            eat(Token.TokenType.STRING);
            atomicElement.setAttribute("type", "StringLiteral");
        } else if (currentToken.type == Token.TokenType.NUMBER) {
            eat(Token.TokenType.NUMBER);
            atomicElement.setAttribute("type", "NumberLiteral");
        } else if (currentToken.type == Token.TokenType.CALL) {
            parseCall(atomicElement);
        }
    }

    private void parseCall(Element parent) throws Exception {
        eat(Token.TokenType.CALL);
        Element callElement = xmlDocument.createElement("FunctionCall");
        parent.appendChild(callElement);

        eat(Token.TokenType.FNAME);
        eat(Token.TokenType.LPAREN);
        parseAtomic(callElement);
        eat(Token.TokenType.COMMA);
        parseAtomic(callElement);
        eat(Token.TokenType.RPAREN);
    }

    private void parseOp(Element parent) throws Exception {
        if (getCurrentToken().type == Token.TokenType.UNOP) {
            eat(Token.TokenType.UNOP);
            Element opElement = xmlDocument.createElement("UnaryOp");
            parent.appendChild(opElement);
        } else if (getCurrentToken().type == Token.TokenType.OPERATOR) {
            eat(Token.TokenType.OPERATOR);
            Element opElement = xmlDocument.createElement("BinaryOp");
            parent.appendChild(opElement);
        }
    }

    private void parseCond(Element parent) throws Exception {
        Element condElement = xmlDocument.createElement("Condition");
        parent.appendChild(condElement);
        parseAtomic(condElement);
        eat(Token.TokenType.OPERATOR);
        parseAtomic(condElement);
    }

    // Parsing functions
    private void parseFunctions(Element parent) throws Exception {
        while (getCurrentToken().type == Token.TokenType.FNAME) {
            parseFunc(parent);
        }
    }

    private void parseFunc(Element parent) throws Exception {
        eat(Token.TokenType.FNAME);
        Element funcElement = xmlDocument.createElement("Function");
        parent.appendChild(funcElement);

        eat(Token.TokenType.FNAME);
        eat(Token.TokenType.LPAREN);
        parseAtomic(funcElement);
        eat(Token.TokenType.COMMA);
        parseAtomic(funcElement);
        eat(Token.TokenType.RPAREN);
        parseAlgo(funcElement);
    }

    // Method to write the final XML output to a file
    private void writeXMLToFile(String fileName) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(xmlDocument);
            StreamResult result = new StreamResult(new File(fileName)); // Use the given filename

            transformer.transform(source, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
