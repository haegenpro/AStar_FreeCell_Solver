package search;

import models.GameState;

import java.util.Objects;

public class Node {
    private Node parent;
    private int depth;
    private int pathCost;
    private GameState state;
    private String action;

    public Node(Node parent, GameState state, String action, int depth, int pathCost) {
        this.parent = parent;
        this.depth = depth;
        this.pathCost = pathCost;
        this.state = state;
        this.action = action;
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

    public GameState getState() {
        return state;
    }

    public String getAction() {
        return action;
    }

    public int getFScore(Heuristic heuristic) {
        return this.pathCost + heuristic.calculate(this.state);
    }
    
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node other = (Node) o;
        return Objects.equals(this.state, other.state);
    }

    public int hashCode() {
        return Objects.hash(state.hashCode());
    }
}