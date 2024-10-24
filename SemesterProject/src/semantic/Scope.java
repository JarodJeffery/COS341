package semantic;

import java.util.*;

public class Scope {
    private String name;
    private Scope parent;
    private List<Scope> children;
    private List<TableEntry> variables;
    private int scopeId;

    public Scope(String name, Scope parent, int scopeId) {
        this.name = name;
        this.parent = parent;
        this.children = new ArrayList<>();
        this.variables = new ArrayList<>();
        this.scopeId = scopeId;
        if (parent != null) {
            parent.addChild(this);
        }
    }

    public void addChild(Scope child) {
        children.add(child);
    }

    public String getName() {
        return name;
    }

    public Scope getParent() {
        return parent;
    }

    public List<Scope> getChildren() {
        return children;
    }

    public List<TableEntry> getVariables() {
        return variables;
    }

    public int getScopeId() {
        return scopeId;
    }

    public void addVariable(TableEntry variable) {
        variables.add(variable);
    }

    public boolean hasVariable(String name) {
        return variables.stream().anyMatch(v -> v.getName().equals(name));
    }

    public TableEntry findVariable(String name) {
        for (TableEntry var : variables) {
            if (var.getName().equals(name)) {
                return var;
            }
        }
        return null;
    }

    public boolean isDescendantOf(Scope potentialAncestor) {
        Scope current = this.parent;
        while (current != null) {
            if (current == potentialAncestor) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }
}



