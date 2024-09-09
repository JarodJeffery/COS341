// Class for print commands (print ATOMIC)
public class PrintCommandNode extends CommandNode {
    private final ASTNode atomic;

    public PrintCommandNode(ASTNode atomic) {
        super("print");
        this.atomic = atomic;
    }

    public ASTNode getAtomic() {
        return atomic;
    }

    @Override
    public String toString() {
        return "PrintCommandNode(atomic: " + atomic + ")";
    }
}
