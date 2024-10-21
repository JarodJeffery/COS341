import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;

public class CodeGenerator {
    private DocumentBuilderFactory factory;
    private DocumentBuilder builder;
    private Document document;
    private PrintWriter writer;

    public CodeGenerator(String xmlFilePath) {
        try {
            factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
            document = builder.parse(new File(xmlFilePath));
            document.getDocumentElement().normalize();
            writer = new PrintWriter(new FileWriter("bas_output.bas"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generateBASIC() {
        NodeList programNodes = document.getElementsByTagName("PROG");
        for (int i = 0; i < programNodes.getLength(); i++) {
            Element program = (Element) programNodes.item(i);
            handleGlobalVars(program);
            handleMainAlgorithm(program);
            handleFunctions(program);
        }
        writer.close();
    }

    private void handleGlobalVars(Element program) {
        NodeList globalVars = program.getElementsByTagName("GLOBVARS");
        if (globalVars.getLength() > 0) {
            NodeList varList = ((Element) globalVars.item(0)).getChildNodes();
            for (int j = 0; j < varList.getLength(); j++) {
                Node varNode = varList.item(j);
                if (varNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element varElement = (Element) varNode;
                    String varType = getElementText(varElement, "VTYP");
                    String varName = getElementText(varElement, "VNAME");
                    if (varType != null && varName != null) {
                        writer.println(varType.toUpperCase() + " " + varName + " = 0"); // Initialize global vars
                    }
                }
            }
            writer.println();
        }
    }

    private void handleMainAlgorithm(Element program) {
        NodeList algos = program.getElementsByTagName("ALGO");
        if (algos.getLength() > 0) {
            Element algo = (Element) algos.item(0);
            writer.println("START"); // BASIC starting point
            processInstructions(algo.getElementsByTagName("INSTRUC"));
            writer.println("END"); // End of BASIC program
        }
    }

    private void processInstructions(NodeList instructions) {
        for (int i = 0; i < instructions.getLength(); i++) {
            Node instrNode = instructions.item(i);
            if (instrNode.getNodeType() == Node.ELEMENT_NODE) {
                Element instrElement = (Element) instrNode;
                NodeList commands = instrElement.getElementsByTagName("COMMAND");
                for (int j = 0; j < commands.getLength(); j++) {
                    Element command = (Element) commands.item(j);
                    handleCommand(command);
                }
            }
        }
    }

    private void handleCommand(Element command) {
        if (command.getElementsByTagName("ASSIGN").getLength() > 0) {
            handleAssignment(command.getElementsByTagName("ASSIGN").item(0));
        } else if (command.getElementsByTagName("PRINT").getLength() > 0) {
            handlePrint(command.getElementsByTagName("PRINT").item(0));
        } else if (command.getElementsByTagName("BRANCH").getLength() > 0) {
            handleBranch(command.getElementsByTagName("BRANCH").item(0));
        } else if (command.getElementsByTagName("HALT").getLength() > 0) {
            writer.println("HALT");
        } else if (command.getElementsByTagName("SKIP").getLength() > 0) {
            writer.println("CONTINUE");
        }
    }

    private void handleAssignment(Node assignmentNode) {
        Element assignment = (Element) assignmentNode;
        String varName = getElementText(assignment, "VNAME");
        String value = getElementText(assignment, "ATOMIC"); // Should be a valid value or expression
        if (varName != null && value != null) {
            writer.println(varName + " = " + value);
        }
    }

    private void handlePrint(Node printNode) {
        Element print = (Element) printNode;
        String value = getElementText(print, "ATOMIC"); // Get the atomic value to print
        if (value != null) {
            writer.println("PRINT " + value);
        }
    }

    private void handleBranch(Node branchNode) {
        Element branch = (Element) branchNode;
        String condition = getElementText(branch, "COND");
        if (condition != null) {
            writer.println("IF " + condition + " THEN");
            // Assuming there are instructions within the branch
            processInstructions(branch.getChildNodes());
            writer.println("ELSE");
            // Assuming there are instructions for else
            processInstructions(branch.getChildNodes());
            writer.println("END IF");
        }
    }

    private void handleFunctions(Element program) {
        NodeList functions = program.getElementsByTagName("FUNCTIONS");
        for (int i = 0; i < functions.getLength(); i++) {
            Node functionNode = functions.item(i);
            if (functionNode.getNodeType() == Node.ELEMENT_NODE) {
                Element function = (Element) functionNode;
                processFunction(function);
            }
        }
    }

    private void processFunction(Element function) {
        String funcName = getElementText(function, "FNAME");
        writer.println("FUNCTION " + funcName + "()");
        // Handle function body and local variables
        writer.println("END FUNCTION");
    }

    private String getElementText(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            Node node = nodeList.item(0);
            if (node != null) {
                return node.getTextContent();
            }
        }
        return null; // Return null if the tag is not found
    }

    public static void main(String[] args) {
        String xmlFilePath = "parse_out.xml"; // Replace with your XML file path
        CodeGenerator codeGen = new CodeGenerator(xmlFilePath);
        codeGen.generateBASIC();
        System.out.println("BASIC program generated successfully.");
    }
}
