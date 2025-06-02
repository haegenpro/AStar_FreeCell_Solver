package models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

public class GameState {
    
    private List<Stack<Card>> tableauPiles;
    private List<Card> freeCells;
    private List<Stack<Card>> homeCells;

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

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameState other = (GameState) o;

        if (this.tableauPiles.size() != other.tableauPiles.size()) return false;
        for (int i = 0; i < this.tableauPiles.size(); i++) {
            if (!stackEquals(this.tableauPiles.get(i), other.tableauPiles.get(i))) {
                return false;
            }
        }

        if (this.freeCells.size() != other.freeCells.size()) return false;
        for (int i = 0; i < this.freeCells.size(); i++) {
            Card thisCard = this.freeCells.get(i);
            Card otherCard = other.freeCells.get(i);
            if (!Objects.equals(thisCard, otherCard)) {
                return false;
            }
        }

        if (this.homeCells.size() != other.homeCells.size()) return false;
        for (int i = 0; i < this.homeCells.size(); i++) {
            if (!stackEquals(this.homeCells.get(i), other.homeCells.get(i))) {
                return false;
            }
        }

        return true;
    }

    private boolean stackEquals(Stack<Card> s1, Stack<Card> s2) {
        if (s1.size() != s2.size()) return false;
        for (int i = 0; i < s1.size(); i++) {
            if (!Objects.equals(s1.get(i), s2.get(i))) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        return Objects.hash(
            deepListHashCode(tableauPiles),
            deepListHashCode(freeCells),
            deepListHashCode(homeCells)
        );
    }

    private int deepListHashCode(List<?> list) {
        int result = 1;
        for (Object element : list) {
            if (element instanceof Stack<?>) {
                @SuppressWarnings("unchecked")
                Stack<Card> stack = (Stack<Card>) element;
                result = 31 * result + deepStackHashCode(stack);
            } else {
                result = 31 * result + Objects.hashCode(element);
            }
        }
        return result;
    }

    private int deepStackHashCode(Stack<Card> stack) {
        int result = 1;
        for (Card card : stack) {
            result = 31 * result + Objects.hashCode(card);
        }
        return result;
    }
}