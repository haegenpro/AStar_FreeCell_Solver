package search;

import models.GameState; // Import the GameState class

import java.util.Objects; // Used for Objects.hash and Objects.equals

public class Node {
    private Node parent;
    private int depth; // g-score: cost from start node to this node (number of moves)
    private int pathCost; // This can also represent g-score, making it more explicit. Let's stick to depth for now as cost is usually 1 per move.
    private GameState state; // The actual game board state
    private String action; // Description of the action taken to reach this state from parent

    // Constructor
    public Node(Node parent, GameState state, String action, int depth, int pathCost) {
        this.parent = parent;
        this.depth = depth;
        this.pathCost = pathCost;
        this.state = state; // Now specifically GameState
        this.action = action;
    }

    // Getters
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
        return state.equals(o) && o instanceof Node other &&
               this.depth == other.depth &&
               this.pathCost == other.pathCost &&
               Objects.equals(this.action, other.action) &&
               Objects.equals(this.parent, other.parent);
    }

    public int hashCode() {
        // Hash code should be based on the immutable aspects of the game state.
        // We need a good hash for GameState. For now, let's use its default hash or
        // a simple combined hash. A truly robust hash for GameState would involve
        // combining hashes of all cards in all piles.
        return Objects.hash(state.hashCode()); // Rely on GameState's hashCode
    }
}