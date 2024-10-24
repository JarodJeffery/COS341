package generator;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;

public class CodeGenerator {

    private StringBuilder basicCode;
    private FileWriter writer ;
    private Document doc;
    private Node start;
    public CodeGenerator() {
        this.basicCode = new StringBuilder();
    }

    // Main method to start code generation
    public void generateCodeFromXML(String xmlFilePath, String outputFilePath) {
        try {
            // Parse the XML file
            File inputFile = new File(xmlFilePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();
            writer = new FileWriter(outputFilePath);
            start = doc.getElementsByTagName("SYNTREE").item(0);
            // Process the root of the syntax tree
            Node root = doc.getElementsByTagName("ROOT").item(0);
            processNode(root);

            // Write the generated BASIC code to output file
            writeToFile(outputFilePath);
            System.out.println("BASIC code generated and written to " + outputFilePath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Recursively process each node in the syntax tree and generate BASIC code
    private void processNode(Node node) throws XPathExpressionException, IOException {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            String symbol = element.getElementsByTagName("SYMB").item(0).getTextContent();

            // Check symbol and generate appropriate BASIC code
            switch (symbol) {
                case "PROG":
                    processProg(node);
                    break;
                case "GLOBVARS":
                    processGlobalVars(node);
                    break;
                case "INSTRUC":
                    processInstructions(node);
                    break;
                case "COMMAND":
                    processCommand(node);
                    break;
                case "ASSIGN":
                    processAssignment(node);
                    break;
                case "CALL":
                    processFunctionCall(node);
                    break;
                case "ALGO":
                    processAlgorithm(node);
                    break;
                default:
                    // Handle other symbols or unknown cases
                    break;
            }

            // Process the children of this node recursively
            NodeList children = element.getElementsByTagName("CHILDREN");
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element temp = (Element) child;
                    String unid = temp.getElementsByTagName("ID").item(0).getTextContent();
                    Node target = findNodeByUNID(unid);
                    System.out.println("UNID: " + unid);
                    processNode(target);
                }

            }
        }
    }

    // Method to handle the "PROG" symbol
    private void processProg(Node node) throws IOException {
        // Assuming PROG is the starting point for BASIC programs
        writer.write("wqrwefgewrg");
    }

    // Method to handle "GLOBVARS" for global variable declarations
    private void processGlobalVars(Node node) {
        // Translate global variable declarations
        basicCode.append("' Global variables\n");
        // Example of how to process: Add corresponding code for global variables
        // basicCode.append("DIM varName AS INTEGER\n");
    }

    // Method to handle "ALGO" for algorithms
    private void processAlgorithm(Node node) {
        basicCode.append("' Algorithm section\n");
        // Translate algorithm-related code
    }

    // Method to handle "INSTRUC" for instructions
    private void processInstructions(Node node) {
        basicCode.append("' Instructions section\n");
    }

    // Method to handle "COMMAND" for commands
    private void processCommand(Node node) {
        // Example of processing a command (could be printing or other actions)
        basicCode.append("' Command processing\n");
        // Example: basicCode.append("PRINT \"Hello\"\n");
    }

    // Method to handle assignments
    private void processAssignment(Node node) {
        basicCode.append("' Assignment operation\n");
        // Example of assignment: variable = expression
        // basicCode.append("LET varName = value\n");
    }

    // Method to handle function calls
    private void processFunctionCall(Node node) {
        basicCode.append("' Function call\n");
        // Translate function call syntax
    }

    // Write the generated BASIC code to an output file
    private void writeToFile(String outputFilePath) {
        try (FileWriter writer = new FileWriter(outputFilePath)) {
            writer.write(basicCode.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Recursively search for a node by its unid attribute
    private Node findNodeByUNID(String unid) throws XPathExpressionException {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();

        String expression = String.format("//IN[UNID='%s']", unid);
        NodeList nodeList = (NodeList) xpath.evaluate(expression, doc, XPathConstants.NODESET);
        if(nodeList.getLength() == 0){
            expression = String.format("//LEAF[UNID='%s']", unid);
            nodeList = (NodeList) xpath.evaluate(expression, doc, XPathConstants.NODESET);
        }

        // Print the results
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            System.out.println("Node found with UNID: " + unid);
            return node;
        }

        return null; // Return null if no node with matching unid is found
    }
}
