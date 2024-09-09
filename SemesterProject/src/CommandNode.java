// Base class for commands in the AST
public class CommandNode extends ASTNode {
    private final String commandType;

    public CommandNode(String commandType) {
        this.commandType = commandType;
    }

    public String getCommandType() {
        return commandType;
    }

    @Override
    public String toString() {
        return "CommandNode(" + commandType + ")";
    }
}
