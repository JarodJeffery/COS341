public class DeclarationNode extends ASTNode {
    public final String type;
    public final String name;

    public DeclarationNode(String type, String name) {
        this.type = type;
        this.name = name;
    }
}
