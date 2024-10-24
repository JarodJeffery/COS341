package parser;

import java.util.List;

public class GrammarRule {
    private String lhs;
    private List<String> rhs;

    public GrammarRule(String lhs, List<String> rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public String getLhs() {
        return lhs;
    }

    public List<String> getRhs() {
        return rhs;
    }
}


