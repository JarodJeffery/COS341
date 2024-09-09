import java.util.List;

public class AlgoNode extends ASTNode {
    public final List<CommandNode> commands;

    public AlgoNode(List<CommandNode> commands) {
        this.commands = commands;
    }
}
