package rules;

import models.Card;
import models.GameState;

import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class Rules {

    public static boolean isRed(Card card) {
        return card.getSuit().equals("Diamonds") || card.getSuit().equals("Hearts");
    }

    public static boolean isBlack(Card card) {
        return card.getSuit().equals("Clubs") || card.getSuit().equals("Spades");
    }

    public static int getRankValue(String rank) {
        switch (rank) {
            case "Ace": return 1;
            case "2": return 2;
            case "3": return 3;
            case "4": return 4;
            case "5": return 5;
            case "6": return 6;
            case "7": return 7;
            case "8": return 8;
            case "9": return 9;
            case "10": return 10;
            case "Jack": return 11;
            case "Queen": return 12;
            case "King": return 13;
            default: return 0;
        }
    }

    public static boolean canMoveTableauToFreeCell(GameState state, int tableauPileIndex, int freeCellIndex) {
        if (tableauPileIndex < 0 || tableauPileIndex >= state.getTableauPiles().size() ||
            freeCellIndex < 0 || freeCellIndex >= state.getFreeCells().size()) {
            return false;
        }

        Stack<Card> tableauPile = state.getTableauPiles().get(tableauPileIndex);
        if (tableauPile.isEmpty()) {
            return false;
        }

        return state.getFreeCells().get(freeCellIndex) == null;
    }

    public static boolean canMoveFreeCellToTableau(GameState state, int freeCellIndex, int tableauPileIndex) {
        if (freeCellIndex < 0 || freeCellIndex >= state.getFreeCells().size() ||
            tableauPileIndex < 0 || tableauPileIndex >= state.getTableauPiles().size()) {
            return false;
        }

        Card cardToMove = state.getFreeCells().get(freeCellIndex);
        if (cardToMove == null) {
            return false;
        }

        Stack<Card> tableauPile = state.getTableauPiles().get(tableauPileIndex);

        if (tableauPile.isEmpty()) {
            return true;
        } else {
            Card topTableauCard = tableauPile.peek();
            boolean alternatingColor = (isRed(cardToMove) && isBlack(topTableauCard)) || (isBlack(cardToMove) && isRed(topTableauCard));
            boolean oneRankLower = getRankValue(cardToMove.getRank()) == getRankValue(topTableauCard.getRank()) - 1;

            return alternatingColor && oneRankLower;
        }
    }

    public static boolean canMoveTableauToHomeCell(GameState state, int tableauPileIndex) {
        if (tableauPileIndex < 0 || tableauPileIndex >= state.getTableauPiles().size()) {
            return false;
        }

        Stack<Card> tableauPile = state.getTableauPiles().get(tableauPileIndex);
        if (tableauPile.isEmpty()) {
            return false;
        }

        Card cardToMove = tableauPile.peek();
        return canMoveToHomeCell(state, cardToMove);
    }

    public static boolean canMoveFreeCellToHomeCell(GameState state, int freeCellIndex) {
        if (freeCellIndex < 0 || freeCellIndex >= state.getFreeCells().size()) {
            return false;
        }

        Card cardToMove = state.getFreeCells().get(freeCellIndex);
        if (cardToMove == null) {
            return false;
        }

        return canMoveToHomeCell(state, cardToMove);
    }

    private static boolean canMoveToHomeCell(GameState state, Card cardToMove) {
        List<Stack<Card>> homeCells = state.getHomeCells();

        for (Stack<Card> homeCellPile : homeCells) {
            if (homeCellPile.isEmpty()) {
                if (getRankValue(cardToMove.getRank()) == 1) {
                    return true;
                }
            } else {
                Card topHomeCard = homeCellPile.peek();
                if (cardToMove.getSuit().equals(topHomeCard.getSuit())) {
                    return getRankValue(cardToMove.getRank()) == getRankValue(topHomeCard.getRank()) + 1;
                }
            }
        }
        return false;
    }

    public static boolean canMoveTableauToTableau(GameState state, int fromPileIndex, int toPileIndex) {
        if (fromPileIndex < 0 || fromPileIndex >= state.getTableauPiles().size() || toPileIndex < 0 || toPileIndex >= state.getTableauPiles().size() || fromPileIndex == toPileIndex) {
            return false;
        }

        Stack<Card> fromPile = state.getTableauPiles().get(fromPileIndex);
        if (fromPile.isEmpty()) return false;

        Card cardToMove = fromPile.peek();

        Stack<Card> toPile = state.getTableauPiles().get(toPileIndex);

        if (toPile.isEmpty()) {
            return true;
        } else {
            Card topToPileCard = toPile.peek();
            boolean alternatingColor = (isRed(cardToMove) && isBlack(topToPileCard)) || (isBlack(cardToMove) && isRed(topToPileCard));
            boolean oneRankLower = getRankValue(cardToMove.getRank()) == getRankValue(topToPileCard.getRank()) - 1;
            return alternatingColor && oneRankLower;
        }
    }

    public static int getMaxMovableCards(GameState state) {
        int emptyFreeCells = state.getEmptyFreeCellsCount();
        int emptyTableauPiles = state.getEmptyTableauPilesCount();
        return (emptyFreeCells + 1) * (int) Math.pow(2, emptyTableauPiles);
    }

    public static boolean canMoveMultipleTableauCards(GameState state, int fromPileIndex, int toPileIndex, int numCards) {
        if (fromPileIndex < 0 || fromPileIndex >= state.getTableauPiles().size() ||
            toPileIndex < 0 || toPileIndex >= state.getTableauPiles().size() ||
            fromPileIndex == toPileIndex) {
            return false;
        }

        Stack<Card> fromPile = state.getTableauPiles().get(fromPileIndex);
        if (fromPile.size() < numCards || numCards <= 0) {
            return false;
        }
        if (numCards > getMaxMovableCards(state)) {
            return false;
        }

        Stack<Card> toPile = state.getTableauPiles().get(toPileIndex);
        Card destinationCard = toPile.isEmpty() ? null : toPile.peek();

        List<Card> cardsToMoveList = new ArrayList<>();
        for (int i = 0; i < numCards; i++) {
            cardsToMoveList.add(0, fromPile.pop());
        }

        boolean validSequence = true;
        for (int i = 0; i < numCards - 1; i++) {
            Card current = cardsToMoveList.get(i);
            Card next = cardsToMoveList.get(i + 1);
            if (!((isRed(current) && isBlack(next)) || (isBlack(current) && isRed(next))) ||
                getRankValue(current.getRank()) != getRankValue(next.getRank()) + 1) {
                validSequence = false;
                break;
            }
        }

        Card bottomOfSequenceCard = cardsToMoveList.get(0);
        if (validSequence) {
            if (destinationCard == null) {
            } else {
                boolean alternatingColor = (isRed(bottomOfSequenceCard) && isBlack(destinationCard)) || (isBlack(bottomOfSequenceCard) && isRed(destinationCard));
                boolean oneRankLower = getRankValue(bottomOfSequenceCard.getRank()) == getRankValue(destinationCard.getRank()) - 1;
                if (!(alternatingColor && oneRankLower)) {
                    validSequence = false;
                }
            }
        }

        for (Card card : cardsToMoveList) {
            fromPile.push(card);
        }

        return validSequence;
    }
}