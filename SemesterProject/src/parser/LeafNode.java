package parser;
import lexer.Token;
public class LeafNode extends TreeNode {
    private Token token;

    public LeafNode(Token token) {
        super();
        this.token = token;
    }

    public Token getToken() {
        return token;
    }
}
