package search;

import models.Card;
import models.GameState;
import rules.Rules;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Collections;
import java.util.Stack;

public class AStar {

    private Heuristic heuristic;

    public AStar() {
        this.heuristic = new Heuristic();
    }

    public List<SolutionStep> solve(GameState initialState) {

        PriorityQueue<Node> openList = new PriorityQueue<>(Comparator.comparingInt(n -> heuristic.calculate(n.getState())));
        HashSet<GameState> closedList = new HashSet<>();
        HashSet<GameState> openStates = new HashSet<>();

        Node initialNode = new Node(null, initialState, null, 0, 0);
        openList.add(initialNode);
        openStates.add(initialState);

        long startTime = System.currentTimeMillis();
        int visitedNodes = 0;
        int maxNodes = 1000000;
        long maxTime = 120000;

        while (!openList.isEmpty() && visitedNodes < maxNodes) {
            if (System.currentTimeMillis() - startTime > maxTime) {
                System.out.println("Search timeout reached (" + maxTime/1000 + " seconds)");
                break;
            }

            Node currentNode = openList.poll();
            openStates.remove(currentNode.getState());
            visitedNodes++;

            if (visitedNodes % 1000 == 0) {
                System.out.println("Visited nodes: " + visitedNodes + ", Open list size: " + openList.size() + ", Current depth: " + currentNode.getDepth() + ", F-score: " + currentNode.getFScore(heuristic) + ", H-score: " + heuristic.calculate(currentNode.getState()));
            }

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

            List<Node> allSuccessors = new ArrayList<>();
            allSuccessors.addAll(getHomeCellMoves(currentNode));
            allSuccessors.addAll(getOtherMoves(currentNode));

            for (Node successor : allSuccessors) {
                GameState successorState = successor.getState();
                if (!closedList.contains(successorState) && !openStates.contains(successorState)) {
                    openList.add(successor);
                    openStates.add(successorState);
                }
            }
        }
        System.out.println("No solution found.");
        System.out.println("Nodes visited: " + visitedNodes);
        System.out.println("Time taken: " + (System.currentTimeMillis() - startTime) + " ms");
        return null;
    }

    private boolean isGoalState(GameState state) {
        int cardsInHomeCells = 0;
        for (Stack<Card> homePile : state.getHomeCells()) {
            cardsInHomeCells += homePile.size();
        }
        return cardsInHomeCells == 52;
    }

    private List<Node> getHomeCellMoves(Node currentNode) {
        List<Node> successors = new ArrayList<>();
        GameState currentState = currentNode.getState();

        for (int fromPile = 0; fromPile < 8; fromPile++) {
            if (!currentState.getTableauPiles().get(fromPile).isEmpty() && Rules.canMoveTableauToHomeCell(currentState, fromPile)) {
                GameState newState = currentState.deepCopy();
                Card cardToMove = newState.getTableauPiles().get(fromPile).pop();
                addCardToHomeCell(newState, cardToMove);
                
                List<Card> autoMovedCards = performAutocompleteMoves(newState);
                
                String notation = String.format("%dh", fromPile + 1);
                String description = String.format("Move %s from Tableau %d to Home", cardToMove, fromPile + 1);
                SolutionStep step = new SolutionStep(notation, description, autoMovedCards);
                
                successors.add(new Node(currentNode, newState, step, currentNode.getDepth() + 1, currentNode.getPathCost() + 1));
            }
        }

        for (int fromFreeCell = 0; fromFreeCell < 4; fromFreeCell++) {
            if (currentState.getFreeCells().get(fromFreeCell) != null) {
                if (Rules.canMoveFreeCellToHomeCell(currentState, fromFreeCell)) {
                    GameState newState = currentState.deepCopy();
                    Card cardToMove = newState.getFreeCells().set(fromFreeCell, null);
                    addCardToHomeCell(newState, cardToMove);
                    List<Card> autoMovedCards = performAutocompleteMoves(newState);

                    String notation = String.format("%dh", fromFreeCell + 1);
                    String description = String.format("Move %s from FreeCell %c to Home", cardToMove, getFreeCellChar(fromFreeCell));
                    SolutionStep step = new SolutionStep(notation, description, autoMovedCards);
                    
                    successors.add(new Node(currentNode, newState, step, currentNode.getDepth() + 1, currentNode.getPathCost() + 1));
                }
            }
        }
        return successors;
    }

    private List<Node> getOtherMoves(Node currentNode) {
        List<Node> successors = new ArrayList<>();
        GameState currentState = currentNode.getState();
        int currentDepth = currentNode.getDepth();
        int currentPathCost = currentNode.getPathCost();

        for (int fromFreeCell = 0; fromFreeCell < 4; fromFreeCell++) {
            if (currentState.getFreeCells().get(fromFreeCell) != null) {
                for (int toPile = 0; toPile < 8; toPile++) {
                    if (Rules.canMoveFreeCellToTableau(currentState, fromFreeCell, toPile)) {
                        GameState newState = currentState.deepCopy();
                        Card cardToMove = newState.getFreeCells().set(fromFreeCell, null);
                        newState.getTableauPiles().get(toPile).push(cardToMove);
                        List<Card> autoMovedCards = performAutocompleteMoves(newState);

                        String notation = String.format("%c%d", getFreeCellChar(fromFreeCell), toPile + 1);
                        String description = String.format("Move %s from FreeCell %c to Tableau %d", cardToMove, getFreeCellChar(fromFreeCell), toPile + 1);
                        SolutionStep step = new SolutionStep(notation, description, autoMovedCards);
                        
                        successors.add(new Node(currentNode, newState, step, currentNode.getDepth() + 1, currentNode.getPathCost() + 1));
                    }
                }
            }
        }

        for (int fromPile = 0; fromPile < 8; fromPile++) {
            if (!currentState.getTableauPiles().get(fromPile).isEmpty()) {
                for (int toPile = 0; toPile < 8; toPile++) {
                    if (fromPile == toPile) continue;

                    if (Rules.canMoveTableauToTableau(currentState, fromPile, toPile)) {
                        GameState newState = currentState.deepCopy();
                        Card cardToMove = newState.getTableauPiles().get(fromPile).pop();
                        newState.getTableauPiles().get(toPile).push(cardToMove);
                        List<Card> autoMovedCards = performAutocompleteMoves(newState);

                        String notation = String.format("%d%d", fromPile + 1, toPile + 1);
                        String description = String.format("Move %s from Tableau %d to Tableau %d", cardToMove, fromPile + 1, toPile + 1);
                        SolutionStep step = new SolutionStep(notation, description, autoMovedCards);
                        
                        successors.add(new Node(currentNode, newState, step, currentNode.getDepth() + 1, currentNode.getPathCost() + 1));
                    }

                    int actualMaxSequenceToMove = Rules.getMaxMovableCards(currentState);
                    Stack<Card> sourcePile = currentState.getTableauPiles().get(fromPile);
                    for (int numCards = Math.min(actualMaxSequenceToMove, sourcePile.size()); numCards >= 2; numCards--) {
                        if (Rules.canMoveMultipleTableauCards(currentState, fromPile, toPile, numCards)) {
                            GameState newState = currentState.deepCopy();
                            Stack<Card> newSourcePile = newState.getTableauPiles().get(fromPile);
                            Stack<Card> newDestPile = newState.getTableauPiles().get(toPile);

                            List<Card> cardsToMove = new ArrayList<>();
                            for (int i = 0; i < numCards; i++) {
                                cardsToMove.add(0, newSourcePile.pop());
                            }
                            Card cardToMove = cardsToMove.get(0);
                            Card lastCard = cardsToMove.get(cardsToMove.size() - 1);
                            for (Card card : cardsToMove) {
                                newDestPile.push(card);
                            }
                            List<Card> autoMovedCards = performAutocompleteMoves(newState);

                            String notation = String.format("%d%d", fromPile + 1, toPile + 1);
                            String description = String.format("Move %s-%s from Tableau %d to Tableau %d", cardToMove, lastCard, fromPile + 1, toPile + 1);
                            SolutionStep step = new SolutionStep(notation, description, autoMovedCards);
                            
                            successors.add(new Node(currentNode, newState, step, currentNode.getDepth() + 1, currentNode.getPathCost() + 1));
                            break;
                        }
                    }
                }
            }
        }

        for (int fromPile = 0; fromPile < 8; fromPile++) {
            if (!currentState.getTableauPiles().get(fromPile).isEmpty()) {
                for (int toFreeCell = 0; toFreeCell < 4; toFreeCell++) {
                    if (Rules.canMoveTableauToFreeCell(currentState, fromPile, toFreeCell)) {
                        GameState newState = currentState.deepCopy();
                        Card cardToMove = newState.getTableauPiles().get(fromPile).pop();
                        newState.getFreeCells().set(toFreeCell, cardToMove);

                        List<Card> autoMovedCards = performAutocompleteMoves(newState);

                        String notation = String.format("%d%c", fromPile + 1, getFreeCellChar(toFreeCell));
                        String description = String.format("Move %s from Tableau %d to FreeCell %c", cardToMove, fromPile + 1, getFreeCellChar(toFreeCell));
                        SolutionStep step = new SolutionStep(notation, description, autoMovedCards);
                        
                        successors.add(new Node(currentNode, newState, step, currentNode.getDepth() + 1, currentNode.getPathCost() + 1));
                        break;
                    }
                }
            }
        }
        return successors;
    }

    private List<Card> performAutocompleteMoves(GameState state) {
        List<Card> movedCards = new ArrayList<>();
        boolean moveMadeInThisPassTotal;
        final String[] suitOrder = {"Clubs", "Diamonds", "Hearts", "Spades"}; 

        do {
            moveMadeInThisPassTotal = false;
            for (int i = 0; i < state.getTableauPiles().size(); i++) {
                while (!state.getTableauPiles().get(i).isEmpty()) {
                    Card card = state.getTableauPiles().get(i).peek();
                    if (Rules.canMoveTableauToHomeCell(state, i) && isSafeToAutocomplete(state, card, suitOrder)) {
                        state.getTableauPiles().get(i).pop();
                        addCardToHomeCell(state, card);
                        movedCards.add(card);
                        moveMadeInThisPassTotal = true;
                    } else {
                        break; 
                    }
                }
            }
            for (int i = 0; i < state.getFreeCells().size(); i++) {
                Card card = state.getFreeCells().get(i);
                if (card != null && Rules.canMoveFreeCellToHomeCell(state, i) && isSafeToAutocomplete(state, card, suitOrder)) {
                    state.getFreeCells().set(i, null);
                    addCardToHomeCell(state, card);
                    movedCards.add(card);
                    moveMadeInThisPassTotal = true;
                }
            }
        } while (moveMadeInThisPassTotal);
        return movedCards;
    }
    
    private boolean isSafeToAutocomplete(GameState state, Card cardToMove, String[] suitOrder) {
        int rankVal = Rules.getRankValue(cardToMove.getRank());

        if (rankVal <= 2) return true;

        int requiredRankOnOpposite = rankVal - 2;
        boolean cardIsRed = Rules.isRed(cardToMove);
        Map<String, Integer> currentHomeRanks = new HashMap<>();
        for (int i = 0; i < suitOrder.length; i++) {
            if (state.getHomeCells().get(i).isEmpty()) {
                currentHomeRanks.put(suitOrder[i], 0);
            } 
            else {
                currentHomeRanks.put(suitOrder[i], Rules.getRankValue(state.getHomeCells().get(i).peek().getRank()));
            }
        }

        for (String suit : suitOrder) {
            boolean suitToCheckIsRed = Rules.isRed(new Card(suit, "Ace"));
            if (cardIsRed != suitToCheckIsRed) {
                if (currentHomeRanks.get(suit) < requiredRankOnOpposite) {
                    return false;
                }
            }
        }
        return true;
    }

    private void addCardToHomeCell(GameState state, Card card) {
        for (Stack<Card> homePile : state.getHomeCells()) {
            if (homePile.isEmpty()) {
                if (card.getRank().equals("Ace")) {
                    homePile.push(card);
                    return;
                }
            } 
            else {
                if (homePile.peek().getSuit().equals(card.getSuit())) {
                    homePile.push(card);
                    return;
                }
            }
        }
    }

    private List<SolutionStep> reconstructPath(Node goalNode) {
        List<SolutionStep> path = new ArrayList<>();
        Node currentNode = goalNode;
        while (currentNode != null && currentNode.getStep() != null) {
            path.add(currentNode.getStep());
            currentNode = currentNode.getParent();
        }
        Collections.reverse(path);
        return path;
    }

    private char getFreeCellChar(int index) {
        return (char) ('a' + index);
    }
}