package search;

public class Node {
    private Node parent;
    private int depth;
    private int pathCost;
    private Object state;

    public Node(Node parent, int depth, int pathCost, Object state) {
        this.parent = parent;
        this.depth = depth;
        this.pathCost = pathCost;
        this.state = state;
    }

    public Node getParent() {
        return parent;
    }

    public int getDepth() {
        return depth;
    }

    public int getPathCost() {
        return pathCost;
    }

    public Object getState() {
        return state;
    }
}
