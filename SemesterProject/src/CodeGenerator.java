import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CodeGenerator {
    private Document xmlDocument; // The parsed XML document
    private StringBuilder basicCode; // StringBuilder to accumulate generated BASIC code
    private int lineNumber; // To keep track of the current line number

    // Constructor to initialize the code generator with the XML file
    public CodeGenerator(String xmlFilePath) throws Exception {
        loadXmlDocument(xmlFilePath);
        basicCode = new StringBuilder();
        lineNumber = 10; // Start line numbers at 10
    }

    // Load the XML document
    private void loadXmlDocument(String xmlFilePath) throws Exception {
        File inputFile = new File(xmlFilePath);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        xmlDocument = dBuilder.parse(inputFile);
        xmlDocument.getDocumentElement().normalize();
    }

    // Main method to generate BASIC code
    public void generateBasicCode() throws Exception {
        Element mainBlock = (Element) xmlDocument.getElementsByTagName("MainBlock").item(0);
        generateGlobalVariables(mainBlock);
        generateAlgorithm(mainBlock); // No "main" method in BASIC
    }

    // Helper method to append a line of BASIC code with line numbers
    private void appendLine(String codeLine) {
        basicCode.append(lineNumber).append(" ").append(codeLine).append("\n");
        lineNumber += 10; // Increment line number by 10
    }

    // Generate global variables (Note: BASIC doesn't require explicit declaration like Java)
    private void generateGlobalVariables(Element mainBlock) throws Exception {
        Element globalsElement = (Element) mainBlock.getElementsByTagName("GlobalVariables").item(0);
        NodeList numTypes = globalsElement.getElementsByTagName("NumType");
        NodeList textTypes = globalsElement.getElementsByTagName("TextType");

        // Numeric variables
        for (int i = 0; i < numTypes.getLength(); i++) {
            String varName = globalsElement.getElementsByTagName("VariableName").item(i).getTextContent();
            appendLine(varName + " = 0"); // Initialize numeric variables
        }

        // Text variables (add $ to their names)
        for (int i = 0; i < textTypes.getLength(); i++) {
            String varName = globalsElement.getElementsByTagName("VariableName").item(i + numTypes.getLength()).getTextContent();
            appendLine(varName + "$ = \"\""); // Initialize text variables with $
        }
    }

    // Generate algorithm section
    private void generateAlgorithm(Element mainBlock) throws Exception {
        Element algorithmElement = (Element) mainBlock.getElementsByTagName("Algorithm").item(0);
        NodeList commandNodes = algorithmElement.getElementsByTagName("Command");

        for (int i = 0; i < commandNodes.getLength(); i++) {
            Element command = (Element) commandNodes.item(i);
            String commandType = command.getAttribute("type");
            switch (commandType) {
                case "Skip":
                    appendLine("REM Skip command"); // BASIC uses REM for comments
                    break;
                case "Print":
                    generatePrintCommand(command);
                    break;
                // Add cases for other command types as necessary
                default:
                    break;
            }
        }
    }

    // Generate code for Print command
    private void generatePrintCommand(Element command) {
        StringBuilder printLine = new StringBuilder("PRINT ");

        // Assuming you can either print a variable name or a string
        NodeList atomicNodes = command.getElementsByTagName("Atomic");
        for (int j = 0; j < atomicNodes.getLength(); j++) {
            Element atomic = (Element) atomicNodes.item(j);
            String type = atomic.getAttribute("type");

            if (type.equals("Variable")) {
                // Variable printout
                String varName = atomic.getTextContent(); // Get the variable name from the XML
                if (isTextVariable(varName)) {
                    varName += "$"; // Add $ for text variables
                }
                printLine.append(varName);
            } else if (type.equals("StringLiteral")) {
                // String literal printout
                String stringValue = atomic.getTextContent();
                printLine.append("\"").append(stringValue).append("\""); // Escape quotes for string literals
            } else if (type.equals("NumberLiteral")) {
                // Number literal printout
                String numberValue = atomic.getTextContent();
                printLine.append(numberValue); // Directly append the number value
            }

            if (j < atomicNodes.getLength() - 1) {
                printLine.append(" + "); // Concatenate if there are multiple atomic elements
            }
        }

        appendLine(printLine.toString()); // Add the PRINT line with line number
    }

    // Helper method to check if a variable is a text variable
    private boolean isTextVariable(String varName) {
        NodeList textTypes = xmlDocument.getElementsByTagName("TextType");
        for (int i = 0; i < textTypes.getLength(); i++) {
            String textVarName = textTypes.item(i).getTextContent();
            if (textVarName.equals(varName)) {
                return true;
            }
        }
        return false;
    }

    // Write generated BASIC code to a file
    public void writeBasicFile(String fileName) throws IOException {
        FileWriter writer = new FileWriter(fileName);
        writer.write(basicCode.toString());
        writer.close();
    }
}
