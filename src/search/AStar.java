package search;

import models.Card; //
import models.GameState; //
import rules.Rules; //

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Collections;
import java.util.Stack;

public class AStar {

    private Heuristic heuristic; //

    public AStar() {
        this.heuristic = new Heuristic(); //
    }

    public List<String> solve(GameState initialState) {
        PriorityQueue<Node> openList = new PriorityQueue<>(new Comparator<Node>() {
            @Override
            public int compare(Node n1, Node n2) {
                return Integer.compare(n1.getFScore(heuristic), n2.getFScore(heuristic)); //
            }
        });

        HashSet<GameState> closedList = new HashSet<>();

        Node initialNode = new Node(null, initialState, "START", 0, 0); //
        openList.add(initialNode);

        long startTime = System.currentTimeMillis();
        int visitedNodes = 0;

        while (!openList.isEmpty()) {
            Node currentNode = openList.poll();
            visitedNodes++;

            if (closedList.contains(currentNode.getState())) {
                continue;
            }

            closedList.add(currentNode.getState());

            if (isGoalState(currentNode.getState())) {
                System.out.println("Solution Found!");
                System.out.println("Nodes visited: " + visitedNodes);
                System.out.println("Time taken: " + (System.currentTimeMillis() - startTime) + " ms");
                return reconstructPath(currentNode);
            }

            for (Node successor : getSuccessors(currentNode)) {
                if (!closedList.contains(successor.getState())) {
                    openList.add(successor);
                }
            }
        }

        System.out.println("No solution found.");
        System.out.println("Nodes visited: " + visitedNodes);
        System.out.println("Time taken: " + (System.currentTimeMillis() - startTime) + " ms");
        return null; // No solution found
    }

    private boolean isGoalState(GameState state) {
        int cardsInHomeCells = 0;
        for (Stack<Card> homePile : state.getHomeCells()) { //
            cardsInHomeCells += homePile.size();
        }
        return cardsInHomeCells == 52; // All 52 cards must be in home cells
    }

    private List<Node> getSuccessors(Node currentNode) {
        List<Node> successors = new ArrayList<>();
        GameState currentState = currentNode.getState();
        int currentDepth = currentNode.getDepth(); //
        int currentPathCost = currentNode.getPathCost(); //

        // --- Explore all possible moves ---

        // 1. Tableau to Free Cell
        for (int fromPile = 0; fromPile < 8; fromPile++) {
            if (!currentState.getTableauPiles().get(fromPile).isEmpty()) { //
                for (int toFreeCell = 0; toFreeCell < 4; toFreeCell++) {
                    if (Rules.canMoveTableauToFreeCell(currentState, fromPile, toFreeCell)) { //
                        GameState newState = currentState.deepCopy(); // Create a new state for the move
                        Card cardToMove = newState.getTableauPiles().get(fromPile).pop(); //
                        newState.getFreeCells().set(toFreeCell, cardToMove); //
                        String action = String.format("Move %s from Tableau %d to FreeCell %d", cardToMove, fromPile, toFreeCell);
                        successors.add(new Node(currentNode, newState, action, currentDepth + 1, currentPathCost + 1)); //
                    }
                }
            }
        }

        // 2. Free Cell to Tableau
        for (int fromFreeCell = 0; fromFreeCell < 4; fromFreeCell++) {
            if (currentState.getFreeCells().get(fromFreeCell) != null) { //
                for (int toPile = 0; toPile < 8; toPile++) {
                    if (Rules.canMoveFreeCellToTableau(currentState, fromFreeCell, toPile)) { //
                        GameState newState = currentState.deepCopy(); //
                        Card cardToMove = newState.getFreeCells().set(fromFreeCell, null); // Remove from free cell
                        newState.getTableauPiles().get(toPile).push(cardToMove); // Add to tableau pile
                        String action = String.format("Move %s from FreeCell %d to Tableau %d", cardToMove, fromFreeCell, toPile);
                        successors.add(new Node(currentNode, newState, action, currentDepth + 1, currentPathCost + 1)); //
                    }
                }
            }
        }

        // 3. Tableau to Home Cell
        for (int fromPile = 0; fromPile < 8; fromPile++) {
            if (!currentState.getTableauPiles().get(fromPile).isEmpty()) { //
                if (Rules.canMoveTableauToHomeCell(currentState, fromPile)) { //
                    GameState newState = currentState.deepCopy(); //
                    Card cardToMove = newState.getTableauPiles().get(fromPile).pop(); //
                    addCardToHomeCell(newState, cardToMove); // Helper to add to correct home cell
                    String action = String.format("Move %s from Tableau %d to HomeCell", cardToMove, fromPile);
                    successors.add(new Node(currentNode, newState, action, currentDepth + 1, currentPathCost + 1)); //
                }
            }
        }

        // 4. Free Cell to Home Cell
        for (int fromFreeCell = 0; fromFreeCell < 4; fromFreeCell++) {
            if (currentState.getFreeCells().get(fromFreeCell) != null) { //
                if (Rules.canMoveFreeCellToHomeCell(currentState, fromFreeCell)) { //
                    GameState newState = currentState.deepCopy(); //
                    Card cardToMove = newState.getFreeCells().set(fromFreeCell, null); // Remove from free cell
                    addCardToHomeCell(newState, cardToMove); // Helper to add to correct home cell
                    String action = String.format("Move %s from FreeCell %d to HomeCell", cardToMove, fromFreeCell);
                    successors.add(new Node(currentNode, newState, action, currentDepth + 1, currentPathCost + 1)); //
                }
            }
        }

        // 5. Tableau to Tableau (single card & multiple cards)
        for (int fromPile = 0; fromPile < 8; fromPile++) {
            if (!currentState.getTableauPiles().get(fromPile).isEmpty()) { //
                for (int toPile = 0; toPile < 8; toPile++) {
                    if (fromPile == toPile) continue;

                    // Try moving a single card first
                    if (Rules.canMoveTableauToTableau(currentState, fromPile, toPile)) { //
                        GameState newState = currentState.deepCopy(); //
                        Card cardToMove = newState.getTableauPiles().get(fromPile).pop(); //
                        newState.getTableauPiles().get(toPile).push(cardToMove); //
                        String action = String.format("Move %s from Tableau %d to Tableau %d (single)", cardToMove, fromPile, toPile);
                        successors.add(new Node(currentNode, newState, action, currentDepth + 1, currentPathCost + 1)); //
                    }

                    // Try moving multiple cards (supermove)
                    int maxMovable = Rules.getMaxMovableCards(currentState); //
                    Stack<Card> sourcePile = currentState.getTableauPiles().get(fromPile); //
                    // Iterate from maxMovable down to 2 (single card handled above)
                    for (int numCards = Math.min(maxMovable, sourcePile.size()); numCards >= 2; numCards--) {
                        if (Rules.canMoveMultipleTableauCards(currentState, fromPile, toPile, numCards)) { //
                            GameState newState = currentState.deepCopy(); //
                            Stack<Card> newSourcePile = newState.getTableauPiles().get(fromPile); //
                            Stack<Card> newDestPile = newState.getTableauPiles().get(toPile); //

                            List<Card> cardsToMove = new ArrayList<>();
                            for (int i = 0; i < numCards; i++) {
                                cardsToMove.add(0, newSourcePile.pop()); // Pop and add to front to maintain order
                            }
                            for (Card card : cardsToMove) {
                                newDestPile.push(card);
                            }
                            String action = String.format("Move %d cards from Tableau %d to Tableau %d (supermove)", numCards, fromPile, toPile);
                            successors.add(new Node(currentNode, newState, action, currentDepth + 1, currentPathCost + 1)); //
                        }
                    }
                }
            }
        }

        return successors;
    }

    private void addCardToHomeCell(GameState state, Card card) {
        List<Stack<Card>> homeCells = state.getHomeCells(); //
        // Simple approach: find the correct home cell.
        // A better approach would be to know the suit order of home cells or map them.
        for (Stack<Card> homePile : homeCells) {
            if (homePile.isEmpty()) {
                // If empty, this must be an Ace. Place it.
                // Assuming home cell structure ensures only correct suit Aces start here.
                // Or that we need to check the suit of the ace.
                // For a more robust solution, homeCells could be a Map<String, Stack<Card>> (Suit to Stack).
                // For now, if it's an Ace and an empty pile, it's assumed to be the correct one.
                if (card.getRank().equals("Ace")) { //
                     homePile.push(card);
                     return;
                }
            } else {
                // If not empty, check suit match
                if (homePile.peek().getSuit().equals(card.getSuit())) { //
                    homePile.push(card);
                    return;
                }
            }
        }
        // This case should ideally not be reached if Rules.canMoveToHomeCell was true.
        System.err.println("Error: Card " + card + " could not be placed in any home cell.");
    }

    private List<String> reconstructPath(Node goalNode) {
        List<String> path = new ArrayList<>();
        Node currentNode = goalNode;
        while (currentNode != null && currentNode.getAction() != null) { //
            path.add(currentNode.getAction()); //
            currentNode = currentNode.getParent(); //
        }
        Collections.reverse(path);
        return path;
    }
}