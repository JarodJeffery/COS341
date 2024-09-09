// Class for assignment commands (VNAME = TERM)
public class AssignCommandNode extends CommandNode {
    private final ASTNode variable;
    private final ASTNode value;

    public AssignCommandNode(ASTNode variable, ASTNode value) {
        super("assign");
        this.variable = variable;
        this.value = value;
    }

    public ASTNode getVariable() {
        return variable;
    }

    public ASTNode getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "AssignCommandNode(variable: " + variable + ", value: " + value + ")";
    }
}
