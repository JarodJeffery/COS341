package parser;

public class RootNode extends TreeNode {
    private String startSymbol;

    public RootNode(String startSymbol) {
        super();
        this.startSymbol = startSymbol;
    }

    public String getStartSymbol() {
        return startSymbol;
    }
}
