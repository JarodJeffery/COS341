// Class for input assignment commands (VNAME < input)
public class InputAssignCommandNode extends CommandNode {
    private final ASTNode variable;

    public InputAssignCommandNode(ASTNode variable) {
        super("input-assign");
        this.variable = variable;
    }

    public ASTNode getVariable() {
        return variable;
    }

    @Override
    public String toString() {
        return "InputAssignCommandNode(variable: " + variable + ")";
    }
}
