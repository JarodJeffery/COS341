package semantic;

public class TableEntry {
    private String name;
    private String type;
    private int scopeId;
    private String internalName;
    private String originalName;
    private boolean isSubfunction;
    private boolean isFunction;
    private int parentScopeId; // New field for parent scope

    public TableEntry(String name, String type, int scopeId, String internalName,
                      String originalName, boolean isSubfunction, int parentScopeId,boolean isFunction) {
        this.name = name;
        this.type = type;
        this.scopeId = scopeId;
        this.internalName = internalName;
        this.originalName = originalName;
        this.isSubfunction = isSubfunction;
        this.parentScopeId = parentScopeId;
        this.isFunction = isFunction;
    }

    // Add getter for parent scope
    public int getParentScopeId() {
        return parentScopeId;
    }

    // Existing getters...
    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getScopeId() {
        return scopeId;
    }

    public String getInternalName() {
        return internalName;
    }

    public String getOriginalName() {
        return originalName;
    }

    public boolean isSubfunction() {
        return isSubfunction;
    }

    public boolean isFunction() {
        return isFunction;
    }
}
