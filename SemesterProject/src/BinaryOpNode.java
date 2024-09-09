public class BinaryOpNode extends ASTNode {
    private RecSPLLexer.TokenType operator;
    private ASTNode leftArgument;
    private ASTNode rightArgument;

    public BinaryOpNode(RecSPLLexer.TokenType operator, ASTNode leftArgument, ASTNode rightArgument) {
        this.operator = operator;
        this.leftArgument = leftArgument;
        this.rightArgument = rightArgument;
    }

    public RecSPLLexer.TokenType getOperator() {
        return operator;
    }

    public ASTNode getLeftArgument() {
        return leftArgument;
    }

    public ASTNode getRightArgument() {
        return rightArgument;
    }

    @Override
    public String toString() {
        return "BinaryOpNode(operator: " + operator + ", leftArgument: " + leftArgument + ", rightArgument: " + rightArgument + ")";
    }
}