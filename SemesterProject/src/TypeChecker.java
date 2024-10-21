import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import java.util.*;
import java.io.File;

public class TypeChecker {
    private Document doc;
    private XPath xpath;
    private Map<String, String> globalVars = new HashMap<>();
    private Map<String, Map<String, String>> localVars = new HashMap<>();
    private Map<String, List<String>> functionParams = new HashMap<>();
    private Map<String, String> functionReturnTypes = new HashMap<>();
    private String currentFunction = null;
    private boolean inFunction = false;

    public TypeChecker(String xmlFile) throws Exception {
        // Parse the XML file
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        doc = builder.parse(new File(xmlFile));
        XPathFactory xPathFactory = XPathFactory.newInstance();
        xpath = xPathFactory.newXPath();
    }

    public void check() throws Exception {
        checkGlobalVars();
        checkFunctions();
        System.out.println("Type checking completed with no errors.");
    }

    // Step 1: Parse global variable declarations
    private void checkGlobalVars() throws Exception {
        NodeList globVars = (NodeList) xpath.evaluate("/PARSETREE/PROG/GLOBVARS/*", doc, XPathConstants.NODESET);
        for (int i = 0; i < globVars.getLength(); i++) {
            Node node = globVars.item(i);
            if (node.getNodeName().equals("VTYP")) {
                String type = node.getTextContent().trim();
                i++;
                Node varNode = globVars.item(i);
                String varName = varNode.getTextContent().trim();
                globalVars.put(varName, type);
            }
        }
    }

    // Step 2: Check functions
    private void checkFunctions() throws Exception {
        NodeList functions = (NodeList) xpath.evaluate("/PARSETREE/PROG/FUNCTIONS/DECL", doc, XPathConstants.NODESET);
        for (int i = 0; i < functions.getLength(); i++) {
            Node function = functions.item(i);
            checkFunction(function);
        }
    }

    private void checkFunction(Node function) throws Exception {
        // Step 2.1: Check function header (return type, parameters)
        String returnType = xpath.evaluate("HEADER/FTYP/TOKEN/@word", function);
        String functionName = xpath.evaluate("HEADER/TOKEN[@class='FNAME']/@word", function);
        List<String> paramTypes = new ArrayList<>();
        NodeList params = (NodeList) xpath.evaluate("HEADER/VNAME/TOKEN", function, XPathConstants.NODESET);
        for (int i = 0; i < params.getLength(); i++) {
            paramTypes.add(xpath.evaluate("TOKEN[@class='VNAME']/@word", params.item(i)));
        }
        functionParams.put(functionName, paramTypes);
        functionReturnTypes.put(functionName, returnType);

        // Step 2.2: Check local variables
        Map<String, String> locals = new HashMap<>();
        NodeList localVarsList = (NodeList) xpath.evaluate("BODY/LOCALVARS/*", function, XPathConstants.NODESET);
        for (int i = 0; i < localVarsList.getLength(); i++) {
            Node node = localVarsList.item(i);
            if (node.getNodeName().equals("VTYP")) {
                String type = node.getTextContent().trim();
                i++;
                Node varNode = localVarsList.item(i);
                String varName = varNode.getTextContent().trim();
                locals.put(varName, type);
            }
        }
        localVars.put(functionName, locals);

        // Step 2.3: Process function body
        currentFunction = functionName;
        inFunction = true;
        checkAlgo(xpath.evaluate("BODY/ALGO", function), locals);
        inFunction = false;
    }

    // Step 3: Check algorithm instructions
    private void checkAlgo(String algoPath, Map<String, String> locals) throws Exception {
        NodeList instructions = (NodeList) xpath.evaluate(algoPath + "/INSTRUC/*", doc, XPathConstants.NODESET);
        for (int i = 0; i < instructions.getLength(); i++) {
            Node instruction = instructions.item(i);
            checkInstruction(instruction, locals);
        }
    }

    private void checkInstruction(Node instruction, Map<String, String> locals) throws Exception {
        String nodeName = instruction.getNodeName();
        if (nodeName.equals("COMMAND")) {
            checkCommand(instruction, locals);
        }
        // Handle other instruction types
        else if (nodeName.equals("BRANCH")) {
            checkBranch(instruction, locals);
        } else if (nodeName.equals("RETURN")) {
            checkReturn(instruction, locals);
        }
    }

    // Check command types
    private void checkCommand(Node command, Map<String, String> locals) throws Exception {
        String commandType = xpath.evaluate("TOKEN/@class", command);
        if (commandType.equals("ASSIGN")) {
            checkAssignment(command, locals);
        } else if (commandType.equals("PRINT")) {
            checkPrint(command, locals);
        } else if (commandType.equals("CALL")) {
            checkCall(command, locals);
        }
    }

    // Check assignment for type compatibility
    private void checkAssignment(Node assign, Map<String, String> locals) throws Exception {
        String varName = xpath.evaluate("VNAME/TOKEN[@class='VNAME']/@word", assign);
        String varType = getVarType(varName, locals);

        String termType = checkTerm(xpath.evaluate("TERM", assign), locals);
        if (!varType.equals(termType)) {
            throw new Exception("Type error: Cannot assign " + termType + " to " + varType + " in variable " + varName);
        }
    }

    // Check print command for valid variable
    private void checkPrint(Node print, Map<String, String> locals) throws Exception {
        String varName = xpath.evaluate("ATOMIC/VNAME/TOKEN[@class='VNAME']/@word", print);
        String varType = getVarType(varName, locals);
        if (varType == null) {
            throw new Exception("Error: Undefined variable " + varName + " in print statement.");
        }
    }

    // Check CALL statement for function type compatibility
    private void checkCall(Node call, Map<String, String> locals) throws Exception {
        String functionName = xpath.evaluate("TOKEN[@class='FNAME']/@word", call);
        List<String> paramTypes = functionParams.get(functionName);
        if (paramTypes == null) {
            throw new Exception("Error: Undefined function " + functionName + ".");
        }

        NodeList args = (NodeList) xpath.evaluate("ATOMIC/VNAME/TOKEN[@class='VNAME']", call, XPathConstants.NODESET);
        if (args.getLength() != paramTypes.size()) {
            throw new Exception("Error: Function " + functionName + " expects " + paramTypes.size() + " arguments, but got " + args.getLength() + ".");
        }

        for (int i = 0; i < args.getLength(); i++) {
            String argName = args.item(i).getTextContent().trim();
            String argType = getVarType(argName, locals);
            String expectedType = paramTypes.get(i);
            if (!argType.equals(expectedType)) {
                throw new Exception("Type error: Argument " + argName + " expected to be " + expectedType + " but found " + argType + " in function call " + functionName);
            }
        }
    }

    // Check branching (if-then-else) for valid condition and blocks
    private void checkBranch(Node branch, Map<String, String> locals) throws Exception {
        // Check condition type
        String condType = checkCondition(xpath.evaluate("COND", branch), locals);
        if (!condType.equals("num")) { // Assuming 'num' type for conditions
            throw new Exception("Type error: Condition in if-else must be a number or boolean.");
        }

        // Check then block
        checkAlgo(xpath.evaluate("ALGO[1]", branch), locals);

        // Check else block
        checkAlgo(xpath.evaluate("ALGO[2]", branch), locals);
    }

    // Check condition in branch
    private String checkCondition(String condPath, Map<String, String> locals) throws Exception {
        return checkTerm(condPath + "/SIMPLE/ATOMIC/VNAME", locals);
    }

    // Check RETURN statement for correct return type
    private void checkReturn(Node ret, Map<String, String> locals) throws Exception {
        String returnType = functionReturnTypes.get(currentFunction);
        String returnValueType = checkTerm(xpath.evaluate("ATOMIC/VNAME/TOKEN[@class='VNAME']", ret), locals);

        if (!returnType.equals(returnValueType)) {
            throw new Exception("Type error: Return type " + returnValueType + " does not match function return type " + returnType + " in function " + currentFunction);
        }
    }

    private String checkTerm(String termPath, Map<String, String> locals) throws Exception {
        String varName = xpath.evaluate("VNAME/TOKEN[@class='VNAME']/@word", doc);
        return getVarType(varName, locals);
    }

    // Get variable type from local or global context
    private String getVarType(String varName, Map<String, String> locals) {
        if (locals.containsKey(varName)) {
            return locals.get(varName);
        }
        if (globalVars.containsKey(varName)) {
            return globalVars.get(varName);
        }
        return null; // Variable is not defined
    }
}
