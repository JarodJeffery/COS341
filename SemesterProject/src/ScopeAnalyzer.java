import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;
import javax.xml.xpath.*;

public class ScopeAnalyzer {
    private Document doc;

    public ScopeAnalyzer(String xmlFile) throws Exception {
        // Load and parse the XML file
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        this.doc = builder.parse(new File(xmlFile));
    }

    public void analyze() throws Exception {
        // Initialize XPath to help navigate the XML
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();

        // Analyze global variables
        NodeList globalVars = analyzeGlobalScope(xpath);

        // Analyze the main algorithm
        analyzeMainAlgorithm(xpath, globalVars);

        // Then analyze each function's scope
        analyzeFunctionScopes(xpath, globalVars);
    }

    private NodeList analyzeGlobalScope(XPath xpath) throws XPathExpressionException {
        // Extract global variables
        NodeList globalVars = (NodeList) xpath.evaluate("//GLOBVARS/VNAME/TOKEN[@class='VNAME']", doc, XPathConstants.NODESET);
        System.out.println("Global Scope Variables:");
        for (int i = 0; i < globalVars.getLength(); i++) {
            Node var = globalVars.item(i);
            String varName = var.getAttributes().getNamedItem("word").getTextContent();
            System.out.println("- " + varName);
        }
        return globalVars;
    }

    private void analyzeMainAlgorithm(XPath xpath, NodeList globalVars) throws XPathExpressionException {
        // Analyze variable usage in the main algorithm
        Node mainAlgo = (Node) xpath.evaluate("//ALGO", doc, XPathConstants.NODE);
        System.out.println("Analyzing main algorithm for variable usage...");
        analyzeVariableUsage(mainAlgo, xpath, "main", globalVars, null, null);
    }

    private void analyzeFunctionScopes(XPath xpath, NodeList globalVars) throws XPathExpressionException {
        NodeList functions = (NodeList) xpath.evaluate("//FUNCTIONS/DECL", doc, XPathConstants.NODESET);

        for (int i = 0; i < functions.getLength(); i++) {
            Node function = functions.item(i);
            // Get the function name
            String functionName = xpath.evaluate("HEADER/TOKEN[@class='FNAME']/@word", function);
            System.out.println("Analyzing function: " + functionName);

            // Analyze function parameters
            NodeList parameters = (NodeList) xpath.evaluate("HEADER/VNAME/TOKEN[@class='VNAME']", function, XPathConstants.NODESET);
            System.out.println("Function Parameters in " + functionName + ":");
            for (int j = 0; j < parameters.getLength(); j++) {
                Node param = parameters.item(j);
                String paramName = param.getAttributes().getNamedItem("word").getTextContent();
                System.out.println("- " + paramName);
            }

            // Analyze local variables in this function
            NodeList localVars = (NodeList) xpath.evaluate("BODY/LOCALVARS/VNAME/TOKEN[@class='VNAME']", function, XPathConstants.NODESET);
            System.out.println("Local Scope Variables in " + functionName + ":");
            for (int j = 0; j < localVars.getLength(); j++) {
                Node var = localVars.item(j);
                String varName = var.getAttributes().getNamedItem("word").getTextContent();
                System.out.println("- " + varName);
            }

            // Analyze the algorithm (ALGO) block for variable usage
            analyzeVariableUsage(function, xpath, functionName, globalVars, localVars, parameters);
        }
    }

    private void analyzeVariableUsage(Node scopeNode, XPath xpath, String scopeName, NodeList globalVars, NodeList localVars, NodeList parameters) throws XPathExpressionException {
        // Collect all the variables used within the algorithm (ALGO) block
        NodeList usedVars = (NodeList) xpath.evaluate(".//VNAME/TOKEN[@class='VNAME']", scopeNode, XPathConstants.NODESET);

        for (int i = 0; i < usedVars.getLength(); i++) {
            Node usedVar = usedVars.item(i);
            String varName = usedVar.getAttributes().getNamedItem("word").getTextContent();

            // Check if the variable is declared in global, local, or parameter scope
            boolean isDeclared = isVariableDeclared(varName, globalVars) ||
                    (localVars != null && isVariableDeclared(varName, localVars)) ||
                    (parameters != null && isVariableDeclared(varName, parameters));

            if (!isDeclared) {
                throw new RuntimeException("Error: Variable " + varName + " is used out of scope in " + scopeName);
            }
        }
    }

    private boolean isVariableDeclared(String varName, NodeList declaredVars) {
        for (int i = 0; i < declaredVars.getLength(); i++) {
            String declaredVarName = declaredVars.item(i).getAttributes().getNamedItem("word").getTextContent();
            if (declaredVarName.equals(varName)) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        try {
            ScopeAnalyzer analyzer = new ScopeAnalyzer("Parser_out.xml");
            analyzer.analyze();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
