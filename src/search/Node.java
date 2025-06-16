package search;

import models.GameState;
import java.util.Objects;

public class Node {
    private Node parent;
    private int depth;
    private int pathCost;
    private GameState state;
    private SolutionStep step; 

    public Node(Node parent, GameState state, SolutionStep step, int depth, int pathCost) {
        this.parent = parent;
        this.depth = depth;
        this.pathCost = pathCost;
        this.state = state;
        this.step = step;
    }

    public SolutionStep getStep() {
        return step;
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
    
    public int getFScore(Heuristic heuristic) {
        return this.pathCost + heuristic.calculate(this.state);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node other = (Node) o;
        return Objects.equals(this.state, other.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state);
    }
}