// Class for variable nodes (VNAME)
public class VariableNode extends ASTNode {
    private final String name;

    public VariableNode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "VariableNode(" + name + ")";
    }
}
