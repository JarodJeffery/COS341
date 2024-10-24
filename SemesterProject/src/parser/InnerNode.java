package parser;

public class InnerNode extends TreeNode {
    private String nonterminal;

    public InnerNode(String nonterminal) {
        super();
        this.nonterminal = nonterminal;
    }

    public String getNonterminal() {
        return nonterminal;
    }
}
