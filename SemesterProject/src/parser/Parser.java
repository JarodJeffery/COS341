package parser;

import lexer.Token;
import lexer.Token.TokenType;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Parser {
    private List<Token> tokens; // The list of tokens
    private Stack<Integer> stateStack = new Stack<>();
    private Stack<String> symbolStack = new Stack<>();

    private ParseTree parseTree;
    private Stack<TreeNode> nodeStack;

    private ParsingTable parsingTable;

    public Parser() {
        this.tokens = new ArrayList<>();
        this.stateStack.push(0); // Initial state
        this.parsingTable = ParserInitializer.initializeParsingTable();

        this.parseTree = new ParseTree();
        this.nodeStack = new Stack<>();

        // Create root node with your grammar's start symbol
        RootNode root = new RootNode("PROG'"); // Replace with your start symbol
        parseTree.setRoot(root);
    }

    public void readTokensFromXML(String xmlPath) {
        try {
            File file = new File(xmlPath);

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);

            doc.getDocumentElement().normalize();

            NodeList tokenNodes = doc.getElementsByTagName("TOK");
            System.out.println("======================");
            System.out.println("\nNumber of tokens: " + tokenNodes.getLength());

            for (int i = 0; i < tokenNodes.getLength(); i++) {
                Node tokenNode = tokenNodes.item(i);
                if (tokenNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) tokenNode;

                    String tokenClass = element.getElementsByTagName("CLASS").item(0).getTextContent();
                    String tokenValue = element.getElementsByTagName("WORD").item(0).getTextContent();

                    TokenType tokenType;
                    try {
                        tokenType = TokenType.valueOf(tokenClass.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        // Handle case where the token class in XML doesn't match the enum values
                        System.err.println("Unknown token class: " + tokenClass);
                        continue;
                    }

                    Token token = new Token(tokenType, tokenValue);
                    tokens.add(token); // Add the token to the list in the parser
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ParseTree getParseTree() {
        return parseTree;
    }

    public void parseTokens() throws IOException {
        System.out.println("Starting Parsing...");
        int tokenIndex = 0;

        while (tokenIndex < tokens.size()) {
            Token currentToken = tokens.get(tokenIndex);
            int currentState = stateStack.peek();

            // Get the action from the parsing table
            String action = getActionForToken(currentState, currentToken);
            System.out
                    .println("\nCurrent State: " + currentState + ", Current Token: " + currentToken + ", Action: " + action);

            if (action == null) {
                System.err.println("Syntax error at token: " + currentToken);
                return;
            }

            if (action.startsWith("s")) { // Shift action
                int nextState = Integer.parseInt(action.substring(1)); // Get the state to shift to
                System.out.println("Action: Shift to state " + nextState + " with token '" + currentToken.getValue() + "'");

                LeafNode leafNode = new LeafNode(currentToken);
                parseTree.addLeafNode(leafNode);
                nodeStack.push(leafNode);

                // Push the current token's type (terminal) onto the symbol stack
                symbolStack.push(currentToken.getType().name());
                stateStack.push(nextState); // Push the new state onto the state stack
                tokenIndex++; // Move to the next token
            } else if (action.startsWith("r")) { // Reduce action
                int ruleNumber = Integer.parseInt(action.substring(1)); // Get the rule number for reduction
                System.out.println("Action: Reduce using rule " + ruleNumber);

                reduceAction(ruleNumber); // Call the reduce function

                // Optionally, you can print the current state and stack contents after each
                // reduction
                printStack();
            } else if (action.equals("acc")) { // Accept action
                System.out.println("Parsing successful! Input fully parsed.");
                if (!nodeStack.isEmpty()) {
                    TreeNode lastNode = nodeStack.peek();
                    parseTree.getRoot().addChildId(lastNode.getUnid());
                    lastNode.setParentId(parseTree.getRoot().getUnid());
                }

                try {
                    parseTree.writeToXML("parser_output/parse_tree.xml");
                    System.out.println("Parse tree written to parser_output/parse_tree.xml");
                } catch (Exception e) {
                    System.err.println("Error writing parse tree to XML: " + e.getMessage());
                    e.printStackTrace();
                }
                return;
            }
        }
    }

    private void printStack() {
        System.out.println("\n--- Stack Status ---");
        System.out.println("State Stack: " + stateStack);
        System.out.println("Symbol Stack: " + symbolStack);
        System.out.println("--------------------\n");
    }

    public void reduceAction(int ruleNumber) {
        GrammarRule rule = ParserInitializer.initializeGrammarRules().get(ruleNumber);
        String lhs = rule.getLhs(); // Get LHS of the reduction rule
        int rhsSize = rule.getRhs().size(); // Get RHS size

        System.out.println("Reduce action: " + lhs + " -> " + rule.getRhs());

        // Create new inner node for this reduction
        InnerNode newNode = new InnerNode(lhs);
        parseTree.addInnerNode(newNode);

        // Pop the number of symbols and states from the stack according to the RHS size
        List<TreeNode> children = new ArrayList<>();
        for (int i = 0; i < rhsSize; i++) {
            if (!nodeStack.isEmpty()) {
                TreeNode child = nodeStack.pop();
                child.setParentId(newNode.getUnid());
                newNode.addChildId(child.getUnid());
                children.add(0, child);
            }
        }

        for (int i = 0; i < rhsSize; i++) {
            symbolStack.pop(); // Pop the symbols
            stateStack.pop(); // Pop the states
        }

        // Push the LHS onto the symbol stack
        symbolStack.push(lhs);

        nodeStack.push(newNode);

        // Find the new state using the GOTO table
        int currentState = stateStack.peek();
        Integer nextState = parsingTable.getGoto(currentState, lhs);

        if (nextState == null) {
            System.err.println("Syntax error: GOTO not found for state " + currentState + " and LHS " + lhs);
            return;
        }

        // Push the new state onto the state stack
        stateStack.push(nextState);
    }

    public String getActionForToken(int currentState, Token token) {
        // Reserved keywords and terminal symbols should use their value directly
        if (token.getType() == TokenType.INPUT_OPERATOR) {
            return parsingTable.getAction(currentState, "<");
        }
        if (token.getType() == TokenType.STRING) {
            return parsingTable.getAction(currentState, "T");
        }
        if (token.getType() == TokenType.NUMBER) {
            return parsingTable.getAction(currentState, "N");
        }
        if (token.getType() == TokenType.PROLOG) {
            return parsingTable.getAction(currentState, "{");
        }
        if (token.getType() == TokenType.EPILOG) {
            return parsingTable.getAction(currentState, "}");
        }
        if (token.getType() == TokenType.MAIN || token.getType() == TokenType.BEGIN || token.getType() == TokenType.END ||
                token.getType() == TokenType.PRINT || token.getType() == TokenType.RETURN || token.getType() == TokenType.N
                || token.getType() == TokenType.COMMA || token.getType() == TokenType.SEMICOLON
                || token.getType() == TokenType.ASSIGNMENT || token.getType() == TokenType.INPUT
                || token.getType() == TokenType.BINOP || token.getType() == TokenType.LPAREN
                || token.getType() == TokenType.RPAREN || token.getType() == TokenType.EOF || token.getType() == TokenType.T
                || token.getType() == TokenType.SKIP || token.getType() == TokenType.HALT || token.getType() == TokenType.IF
                || token.getType() == TokenType.ELSE || token.getType() == TokenType.THEN) {
            return parsingTable.getAction(currentState, token.getValue());
        } else {
            // Use token type for general types like variables, numbers, etc.
            return parsingTable.getAction(currentState, token.getType().name());
        }
    }

}
