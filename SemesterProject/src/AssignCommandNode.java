public class AssignCommandNode extends CommandNode {
    public ASTNode expression;
    public AssignCommandNode(ASTNode variable, ASTNode expression, ASTNode term) {

        super("assign", variable);
        this.expression = expression;
    }
}
