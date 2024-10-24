package semantic;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import parser.ParseTree;
import parser.RootNode;
import parser.InnerNode;
import parser.LeafNode;
import parser.TreeNode;

public class SemanticAnalyzer {
    private List<TableEntry> symbolTable = new ArrayList<>();
    private int functionCounter = 1;
    private int variableCounter = 1;
    private int scopeCounter = 0;
    private ParseTree parseTree;
    private boolean insideSubfuncs = false;
    private int currentParentScope = 0; // Track current parent scope
    private Stack<Integer> scopeStack = new Stack<>(); // Stack to track nested scopes

    private boolean isInsideSubfuncs(TreeNode node) {
        TreeNode current = node;
        while (current != null) {
            if (current instanceof InnerNode) {
                InnerNode innerNode = (InnerNode) current;
                if (innerNode.getNonterminal().equals("SUBFUNCS")) {
                    return true;
                }
            }
            // Get parent node
            current = getParentNode(current);
        }
        return false;
    }

    private TreeNode getParentNode(TreeNode node) {
        for (InnerNode innerNode : parseTree.getInnerNodes()) {
            if (innerNode.getChildrenIds().contains(node.getUnid())) {
                return innerNode;
            }
        }
        return null;
    }

    public SemanticAnalyzer(ParseTree parseTree) {
        this.parseTree = parseTree;
        scopeStack.push(0);
    }

    public void analyze() {
        RootNode rootNode = parseTree.getRoot();
        traverse(rootNode);
        printSymbolTable();
    }



    // Traverse the syntax tree
    private void traverse(TreeNode node) {
        List<Integer> children = node.getChildrenIds();

        // Check if we're entering SUBFUNCS
        if (node instanceof InnerNode && ((InnerNode) node).getNonterminal().equals("SUBFUNCS")) {
            insideSubfuncs = true;
        }

        // Traverse children in reverse order
        for (int i = children.size() - 1; i >= 0; i--) {
            int childId = children.get(i);
            TreeNode childNode = getNodeByID(childId);

            if (childNode instanceof InnerNode) {
                InnerNode innerNode = (InnerNode) childNode;
                String symbol = innerNode.getNonterminal();

                System.out.println("Processing inner node: " + innerNode.getNonterminal() +
                        " (Inside SUBFUNCS: " + insideSubfuncs + ")");

                if (symbol.equals("GLOBVARS")) {
                    handleGlobalVariables(innerNode);
                } else if (symbol.equals("FUNCTIONS")) {
                    handleFunctions(innerNode);
                }
            } else if (childNode instanceof LeafNode) {
                LeafNode leafNode = (LeafNode) childNode;
                System.out.println("Processing leaf node: " + leafNode.getToken());
                handleLeafNode(leafNode);
            }

            traverse(childNode);
        }

        // Check if we're leaving SUBFUNCS
        if (node instanceof InnerNode && ((InnerNode) node).getNonterminal().equals("SUBFUNCS")) {
            insideSubfuncs = false;
        }
    }

    // Handle global variables (GLOBVARS)

    private TreeNode getNodeByID(int id) {
        for (InnerNode innerNode : parseTree.getInnerNodes()) {
            if (innerNode.getUnid() == id) {
                return innerNode;
            }
        }

        for (LeafNode leafNode : parseTree.getLeafNodes()) {
            if (leafNode.getUnid() == id) {
                return leafNode;
            }
        }

        return null;
    }

    private void handleGlobalVariables(InnerNode innerNode) {
        System.out.println("Processing global variables.");
        List<Integer> children = innerNode.getChildrenIds();

        if (children.size() == 0) {
            return;
        }

        LeafNode typeNode = null;
        LeafNode nameNode = null;
        for (int i = children.size() - 1; i >= 0; i--) {
            TreeNode childNode = getNodeByID(children.get(i));

            if (childNode instanceof InnerNode) {
                InnerNode innerChildNode = (InnerNode) childNode;
                if (innerChildNode.getNonterminal().equals("VTYP")) {
                    TreeNode grandChildNode = getNodeByID(innerChildNode.getChildrenIds().get(0));
                    LeafNode vTypNode = (LeafNode) grandChildNode;
                    typeNode = vTypNode;
                } else if (innerChildNode.getNonterminal().equals("VNAME")) {
                    TreeNode grandChildNode = getNodeByID(innerChildNode.getChildrenIds().get(0));
                    LeafNode vNameNode = (LeafNode) grandChildNode;
                    nameNode = vNameNode;
                }
            }
        }

        symbolTable.add(new TableEntry(nameNode.getToken().getValue(), typeNode.getToken().getValue(), scopeCounter,
                makeVariableName(), nameNode.getToken().getValue(), false, 0, false));
    }

    private void handleFunctions(InnerNode innerNode) {
        if (innerNode.getChildrenIds().size() == 0) {
            return;
        }

        boolean isSubfunction = isInsideSubfuncs(innerNode);
        System.out.println("Processing function declarations. Is Subfunction: " + isSubfunction);

        // Store the parent scope before incrementing scope counter
        int parentScope = scopeStack.peek();

        if (isSubfunction) {
            parentScope = scopeCounter;
        }

        scopeCounter++;
        scopeStack.push(scopeCounter); // Push new scope

        List<Integer> children = innerNode.getChildrenIds();

        for (int i = children.size() - 1; i >= 0; i--) {
            TreeNode childNode = getNodeByID(children.get(i));

            if (childNode instanceof InnerNode) {
                InnerNode innerChildNode = (InnerNode) childNode;
                if (innerChildNode.getNonterminal().equals("DECL")) {
                    List<Integer> grandChildren = innerChildNode.getChildrenIds();

                    for (int j = grandChildren.size() - 1; j >= 0; j--) {
                        TreeNode grandChildNode = getNodeByID(grandChildren.get(j));
                        if (grandChildNode instanceof InnerNode) {
                            InnerNode innerGrandChildNode = (InnerNode) grandChildNode;
                            if (innerGrandChildNode.getNonterminal().equals("HEADER")) {
                                handleHeader(innerGrandChildNode, isSubfunction, parentScope);
                            } else if (innerGrandChildNode.getNonterminal().equals("BODY")) {
                                handleBody(innerGrandChildNode);
                            }
                        }
                    }
                }
            }
        }

        // Pop the scope when done with this function
        scopeStack.pop();
    }

    private void handleHeader(InnerNode innerNode, boolean isSubfunction, int parentScope) {
        List<Integer> children = innerNode.getChildrenIds();

        LeafNode typeNode = null;
        LeafNode nameNode = null;
        for (int i = children.size() - 1; i >= 0; i--) {
            TreeNode childNode = getNodeByID(children.get(i));
            if (childNode instanceof InnerNode) {
                InnerNode innerChildNode = (InnerNode) childNode;
                if (innerChildNode.getNonterminal().equals("FTYP")) {
                    TreeNode grandChildNode = getNodeByID(innerChildNode.getChildrenIds().get(0));
                    LeafNode fTypNode = (LeafNode) grandChildNode;
                    typeNode = fTypNode;
                } else if (innerChildNode.getNonterminal().equals("FNAME")) {
                    TreeNode grandChildNode = getNodeByID(innerChildNode.getChildrenIds().get(0));
                    LeafNode fNameNode = (LeafNode) grandChildNode;
                    nameNode = fNameNode;
                }
            }
        }

        System.out.println("Adding function " + nameNode.getToken().getValue() +
                " to symbol table (Is Subfunction: " + isSubfunction +
                ", Parent Scope: " + parentScope + ")");

        symbolTable.add(new TableEntry(
                nameNode.getToken().getValue(),
                typeNode.getToken().getValue(),
                scopeCounter,
                makeFunctionName(),
                nameNode.getToken().getValue(),
                isSubfunction,
                parentScope, // Add parent scope ID
                isSubfunction
        ));
    }

    private void handleBody(InnerNode innerNode) {
        List<Integer> children = innerNode.getChildrenIds();

        for (int i = children.size() - 1; i >= 0; i--) {
            TreeNode childNode = getNodeByID(children.get(i));
            if (childNode instanceof InnerNode) {
                InnerNode innerChildNode = (InnerNode) childNode;
                if (innerChildNode.getNonterminal().equals("LOCVARS")) {
                    handleLocalVariables(innerChildNode);
                }
            }
        }
    }

    private void handleLocalVariables(InnerNode innerNode) {
        List<Integer> children = innerNode.getChildrenIds();

        LeafNode typeNode = null;
        LeafNode nameNode = null;
        for (int i = children.size() - 1; i >= 0; i--) {
            TreeNode childNode = getNodeByID(children.get(i));
            if (childNode instanceof InnerNode) {
                InnerNode innerChildNode = (InnerNode) childNode;
                if (innerChildNode.getNonterminal().equals("VTYP")) {
                    TreeNode grandChildNode = getNodeByID(innerChildNode.getChildrenIds().get(0));
                    LeafNode vTypNode = (LeafNode) grandChildNode;
                    typeNode = vTypNode;
                } else if (innerChildNode.getNonterminal().equals("VNAME")) {
                    TreeNode grandChildNode = getNodeByID(innerChildNode.getChildrenIds().get(0));
                    LeafNode vNameNode = (LeafNode) grandChildNode;
                    nameNode = vNameNode;
                }
            }

            if (typeNode != null && nameNode != null) {
                symbolTable.add(new TableEntry(
                        nameNode.getToken().getValue(),
                        typeNode.getToken().getValue(),
                        scopeCounter,
                        makeVariableName(),
                        nameNode.getToken().getValue(),
                        false, // variables aren't subfunctions
                        scopeStack.peek(), // current scope's parent
                        false
                ));
                typeNode = null;
                nameNode = null;
            }
        }

    }

    private void handleLeafNode(LeafNode leafNode) {
        if (leafNode.getToken().getValue().equals("main")) {
            symbolTable.add(new TableEntry("main", "main", scopeCounter, makeFunctionName(), "main", false, 0, true));
        }
    }

    private String makeFunctionName() {
        return "f" + functionCounter++;
    }

    private String makeVariableName() {
        return "v" + variableCounter++;
    }

    public List<TableEntry> getSymbolTable() {
        return symbolTable;
    }

    private void printSymbolTable() {
        System.out.println("\nSymbol Table:");
        System.out.println("=======================================================================");
        System.out.println("Name\tType\tScope\tInternal\tParent Scope\tIs Subfunction\t isFunction");
        System.out.println("-----------------------------------------------------------------------");
        for (TableEntry entry : symbolTable) {
            System.out.print(entry.getOriginalName() + "\t");
            System.out.print(entry.getType() + "\t");
            System.out.print(entry.getScopeId() + "\t");
            System.out.print(entry.getInternalName() + "\t");
            System.out.print("\t" + entry.getParentScopeId() + "\t\t");
            System.out
                    .print(entry.isSubfunction());
            System.out.println("\t " + entry.isFunction());
        }
        System.out.println("=======================================================================");
    }
}
