package search;

import models.Card;
import models.GameState;

import java.util.List;
import java.util.Stack;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.ArrayList;

public class Heuristic {
    private static final int WEIGHT_CARDS_NOT_IN_HOME = 10;
    private static final int WEIGHT_BLOCKED_CARDS = 1;
    private static final int WEIGHT_FREECELL_PENALTY = 2;
    private static final int WEIGHT_EMPTY_TABLEAU_BONUS = 20;
    private static final int WEIGHT_SEQUENCE_BONUS = 10;

    private static int getRankValue(String rank) {
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

    private static boolean isRed(Card card) {
        return card.getSuit().equals("Diamonds") || card.getSuit().equals("Hearts");
    }

    private static boolean isBlack(Card card) {
        return card.getSuit().equals("Clubs") || card.getSuit().equals("Spades");
    }

    public int calculate(GameState state) {
        int cardsInHomeCells = 0;
        for (Stack<Card> homePile : state.getHomeCells()) {
            cardsInHomeCells += homePile.size();
        }
        int cardsNotInHomeComponent = (52 - cardsInHomeCells) * WEIGHT_CARDS_NOT_IN_HOME;

        int blockedCardsComponent = calculateBlockedCards(state) * WEIGHT_BLOCKED_CARDS;

        int freeCellPenaltyComponent = (4 - state.getEmptyFreeCellsCount()) * WEIGHT_FREECELL_PENALTY;

        int emptyTableauBonusComponent = state.getEmptyTableauPilesCount() * WEIGHT_EMPTY_TABLEAU_BONUS;
        
        int sequenceBonusValue = calculateSequenceBonus(state);
        int sequenceBonusEffect = sequenceBonusValue * WEIGHT_SEQUENCE_BONUS;

        return cardsNotInHomeComponent +
            blockedCardsComponent +
            freeCellPenaltyComponent -
            emptyTableauBonusComponent -
            sequenceBonusEffect;
    }

    private int calculateBlockedCards(GameState state) {
        int blockers = 0;
        Map<String, Integer> nextExpectedRank = getNextExpectedRanks(state);

        for (Stack<Card> tableauPile : state.getTableauPiles()) {
            if (tableauPile.isEmpty()) continue;

            List<Card> cardsInPile = new ArrayList<>(tableauPile);
            Collections.reverse(cardsInPile);

            for (int i = 0; i < cardsInPile.size(); i++) {
                Card currentCard = cardsInPile.get(i);
                String suit = currentCard.getSuit();
                int rank = getRankValue(currentCard.getRank());

                Integer expectedRank = nextExpectedRank.get(suit);
                if (expectedRank != null && expectedRank != 0 && rank == expectedRank) {
                    blockers += (cardsInPile.size() - 1 - i);
                }
            }
        }

        for (Card card : state.getFreeCells()) {
            if (card != null) {
                String suit = card.getSuit();
                int rank = getRankValue(card.getRank());
                Integer expectedRank = nextExpectedRank.get(suit);
                if (expectedRank != null && expectedRank != 0 && rank == expectedRank) {
                    blockers += 1;
                }
            }
        }

        return blockers;
    }

    private Map<String, Integer> getNextExpectedRanks(GameState state) {
        String[] suits = {"Clubs", "Diamonds", "Hearts", "Spades"};
        Map<String, Integer> nextExpectedRank = new HashMap<>();

        for (String suit : suits) {
            nextExpectedRank.put(suit, 1);
        }

        for (Stack<Card> homePile : state.getHomeCells()) {
            if (!homePile.isEmpty()) {
                Card topCard = homePile.peek();
                String suit = topCard.getSuit();
                int currentRank = getRankValue(topCard.getRank());
                if (currentRank < 13) {
                    nextExpectedRank.put(suit, currentRank + 1);
                } else {
                    nextExpectedRank.put(suit, 0);
                }
            }
        }

        return nextExpectedRank;
    }

    private int calculateSequenceBonus(GameState state) {
        int bonus = 0;

        for (Stack<Card> tableauPile : state.getTableauPiles()) {
            if (tableauPile.size() <= 1) continue;

            List<Card> cards = new ArrayList<>(tableauPile);
            Collections.reverse(cards);
            int sequenceLength = 1;
            for (int i = 1; i < cards.size(); i++) {
                Card current = cards.get(i);
                Card previous = cards.get(i - 1);

                boolean correctColorAlternation = (isRed(current) && isBlack(previous)) || (isBlack(current) && isRed(previous));
                boolean correctRankOrder = getRankValue(current.getRank()) == getRankValue(previous.getRank()) - 1;

                if (correctColorAlternation && correctRankOrder) {
                    sequenceLength++;
                } else {
                    break;
                }
            }

            if (sequenceLength > 1) {
                bonus += sequenceLength;
            }
        }

        return bonus;
    }
}