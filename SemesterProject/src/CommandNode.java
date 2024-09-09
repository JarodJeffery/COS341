public class CommandNode extends ASTNode {
    public final String commandType;
    public final ASTNode argument;

    public CommandNode(String commandType, ASTNode argument) {
        this.commandType = commandType;
        this.argument = argument;
    }
}
