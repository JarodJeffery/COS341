import java.util.List;

public class ProgramNode extends ASTNode{
    public final List<DeclarationNode> declarations;
    public final AlgoNode algo;

    public ProgramNode(List<DeclarationNode> declarations, AlgoNode algo) {
        this.declarations = declarations;
        this.algo = algo;
    }
}
