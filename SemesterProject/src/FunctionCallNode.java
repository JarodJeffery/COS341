public class FunctionCallNode extends ASTNode {
    private String functionName;
    private ASTNode arg1;
    private ASTNode arg2;
    private ASTNode arg3;

    public FunctionCallNode(ASTNode arg1, ASTNode arg2, ASTNode arg3) {
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.arg3 = arg3;
    }

    public String getFunctionName() {
        return functionName;
    }

    public ASTNode getArg1() {
        return arg1;
    }

    public ASTNode getArg2() {
        return arg2;
    }

    public ASTNode getArg3() {
        return arg3;
    }

    @Override
    public String toString() {
        return "FunctionCallNode(functionName: " + functionName + ", arg1: " + arg1 + ", arg2: " + arg2 + ", arg3: " + arg3 + ")";
    }
}