public class UnaryOpNode extends ASTNode {
    private RecSPLLexer.TokenType operator;
    private ASTNode argument;

    public UnaryOpNode(RecSPLLexer.TokenType operator, ASTNode argument) {
        this.operator = operator;
        this.argument = argument;
    }

    public RecSPLLexer.TokenType getOperator() {
        return operator;
    }

    public ASTNode getArgument() {
        return argument;
    }

    @Override
    public String toString() {
        return "UnaryOpNode(operator: " + operator + ", argument: " + argument + ")";
    }
}