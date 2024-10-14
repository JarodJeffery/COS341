

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CodeGenerator {
    private Document xmlDocument; // The parsed XML document
    private StringBuilder javaCode; // StringBuilder to accumulate generated Java code

    // Constructor to initialize the code generator with the XML file
    public CodeGenerator(String xmlFilePath) throws Exception {
        loadXmlDocument(xmlFilePath);
        javaCode = new StringBuilder();
    }

    // Load the XML document
    private void loadXmlDocument(String xmlFilePath) throws Exception {
        File inputFile = new File(xmlFilePath);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        xmlDocument = dBuilder.parse(inputFile);
        xmlDocument.getDocumentElement().normalize();
    }

    // Main method to generate Java code
    public void generateJavaCode() throws Exception {
        javaCode.append("public class GeneratedProgram {\n");
        Element mainBlock = (Element) xmlDocument.getElementsByTagName("MainBlock").item(0);
        generateGlobalVariables(mainBlock);
        generateMainMethod();
        javaCode.append("}\n");
    }

    // Generate global variables
    private void generateGlobalVariables(Element mainBlock) throws Exception {
        Element globalsElement = (Element) mainBlock.getElementsByTagName("GlobalVariables").item(0);
        NodeList numTypes = globalsElement.getElementsByTagName("NumType");
        NodeList textTypes = globalsElement.getElementsByTagName("TextType");

        // Generate numeric variable declarations
        for (int i = 0; i < numTypes.getLength(); i++) {
            String varName = globalsElement.getElementsByTagName("VariableName").item(i).getTextContent();
            javaCode.append("    public static int ").append(varName).append(";\n");
        }

        // Generate text variable declarations
        for (int i = 0; i < textTypes.getLength(); i++) {
            String varName = globalsElement.getElementsByTagName("VariableName").item(i + numTypes.getLength()).getTextContent();
            javaCode.append("    public static String ").append(varName).append(";\n");
        }
    }

    // Generate the main method
    private void generateMainMethod() throws Exception {
        javaCode.append("    public static void main(String[] args) {\n");

        Element mainBlock = (Element) xmlDocument.getElementsByTagName("MainBlock").item(0);
        generateAlgorithm(mainBlock);

        javaCode.append("    }\n"); // Close main method
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
                    javaCode.append("        // Skip command\n");
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
    // Generate code for Print command
    private void generatePrintCommand(Element command) {
        javaCode.append("        System.out.println(");

        // Assuming you can either print a variable name or a string
        NodeList atomicNodes = command.getElementsByTagName("Atomic");
        for (int j = 0; j < atomicNodes.getLength(); j++) {
            Element atomic = (Element) atomicNodes.item(j);
            String type = atomic.getAttribute("type");

            if (type.equals("Variable")) {
                // Variable printout
                String varName = atomic.getTextContent(); // Get the variable name from the XML
                javaCode.append(varName);
            } else if (type.equals("StringLiteral")) {
                // String literal printout
                String stringValue = atomic.getTextContent();
                javaCode.append("\"").append(stringValue).append("\""); // Escape quotes for string literals
            } else if (type.equals("NumberLiteral")) {
                // Number literal printout
                String numberValue = atomic.getTextContent();
                javaCode.append(numberValue); // Directly append the number value
            }

            if (j < atomicNodes.getLength() - 1) {
                javaCode.append(" + "); // Concatenate if there are multiple atomic elements
            }
        }

        javaCode.append(");\n"); // Close the print statement
    }


    // Write generated Java code to a file
    public void writeJavaFile(String fileName) throws IOException {
        FileWriter writer = new FileWriter(fileName);
        writer.write(javaCode.toString());
        writer.close();
    }

    // Main method for testing the CodeGenerator
    public static void main(String[] args) {
        try {
            CodeGenerator generator = new CodeGenerator("parsed_program.xml");
            generator.generateJavaCode();
            generator.writeJavaFile("GeneratedProgram.java");
            System.out.println("Java code generated successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

