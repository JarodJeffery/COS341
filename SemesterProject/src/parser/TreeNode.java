package parser;

import java.util.ArrayList;
import java.util.List;

public abstract class TreeNode {
    private static int nextId = 1;
    protected int unid;
    protected int parentId;
    protected List<Integer> childrenIds;

    public TreeNode() {
        this.unid = nextId++;
        this.childrenIds = new ArrayList<>();
    }

    public int getUnid() {
        return unid;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public List<Integer> getChildrenIds() {
        return childrenIds;
    }

    public void addChildId(int childId) {
        childrenIds.add(childId);
    }
}
