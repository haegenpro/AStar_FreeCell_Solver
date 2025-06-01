package models;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class GameState {
    // 8 Tableau Piles
    private List<Stack<Card>> tableauPiles;
    // 4 Free Cells
    private List<Card> freeCells;
    // 4 Home Cells (one for each suit, stores the next expected card for that suit)
    private List<Stack<Card>> homeCells; // Each stack holds cards of a specific suit, built up from Ace to King

    public GameState() {
        tableauPiles = new ArrayList<>(8);
        for (int i = 0; i < 8; i++) {
            tableauPiles.add(new Stack<>());
        }

        freeCells = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            freeCells.add(null);
        }

        homeCells = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            homeCells.add(new Stack<>());
        }
    }

    public List<Stack<Card>> getTableauPiles() {
        return tableauPiles;
    }

    public List<Card> getFreeCells() {
        return freeCells;
    }

    public List<Stack<Card>> getHomeCells() {
        return homeCells;
    }

    public GameState deepCopy() {
        GameState newGameState = new GameState();

        for (int i = 0; i < tableauPiles.size(); i++) {
            Stack<Card> originalPile = tableauPiles.get(i);
            Stack<Card> newPile = new Stack<>();
            List<Card> tempCards = new ArrayList<>(originalPile);
            for (Card card : tempCards) {
                newPile.push(new Card(card.getSuit(), card.getRank()));
            }
            newGameState.getTableauPiles().set(i, newPile);
        }

        for (int i = 0; i < freeCells.size(); i++) {
            Card originalCard = freeCells.get(i);
            if (originalCard != null) {
                newGameState.getFreeCells().set(i, new Card(originalCard.getSuit(), originalCard.getRank()));
            } else {
                newGameState.getFreeCells().set(i, null);
            }
        }

        for (int i = 0; i < homeCells.size(); i++) {
            Stack<Card> originalHomePile = homeCells.get(i);
            Stack<Card> newHomePile = new Stack<>();
            List<Card> tempCards = new ArrayList<>(originalHomePile);
            for (Card card : tempCards) {
                newHomePile.push(new Card(card.getSuit(), card.getRank()));
            }
            newGameState.getHomeCells().set(i, newHomePile);
        }

        return newGameState;
    }

    public int getEmptyFreeCellsCount() {
        int count = 0;
        for (Card card : freeCells) {
            if (card == null) {
                count++;
            }
        }
        return count;
    }

    public int getEmptyTableauPilesCount() {
        int count = 0;
        for (Stack<Card> pile : tableauPiles) {
            if (pile.isEmpty()) {
                count++;
            }
        }
        return count;
    }
}