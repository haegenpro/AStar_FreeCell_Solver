package search;

import models.Card;
import models.GameState;
import rules.Rules;
import java.util.*;

public class Heuristic {

    private static final int REWARD_PER_CARD_IN_HOME = -25;

    private static final int REWARD_PER_SEQUENCE_CARD = -10;

    private static final int REWARD_PER_EMPTY_TABLEAU = -5;

    private static final int PENALTY_PER_USED_FREECELL = 2;

    private static final int PENALTY_PER_BLOCKED_CARD = 1;

    public int calculate(GameState state) {
        int cardsInHome = 0;

        for (Stack<Card> homePile : state.getHomeCells()) {
            cardsInHome += homePile.size();
        }

        int homeReward = cardsInHome * REWARD_PER_CARD_IN_HOME;
        int sequenceScore = calculateSequenceScore(state);
        int sequenceReward = sequenceScore * REWARD_PER_SEQUENCE_CARD;
        int emptyTableauReward = state.getEmptyTableauPilesCount() * REWARD_PER_EMPTY_TABLEAU;
        int freeCellPenalty = (4 - state.getEmptyFreeCellsCount()) * PENALTY_PER_USED_FREECELL;
        int blockedPenalty = calculateBlockedPenalty(state) * PENALTY_PER_BLOCKED_CARD;

        return homeReward + sequenceReward + emptyTableauReward + freeCellPenalty + blockedPenalty;
    }

    private int calculateSequenceScore(GameState state) {
        int totalCardsInOrderedSequences = 0;
        for (Stack<Card> tableauPile : state.getTableauPiles()) {
            if (tableauPile.size() <= 1) continue;

            List<Card> cards = new ArrayList<>(tableauPile);
            Collections.reverse(cards);

            int sequenceLength = 1;
            for (int i = 1; i < cards.size(); i++) {
                Card higherCard = cards.get(i - 1);
                Card lowerCard = cards.get(i);

                boolean isCorrectOrder = Rules.getRankValue(lowerCard.getRank()) == Rules.getRankValue(higherCard.getRank()) - 1;
                boolean isAlternatingColor = (Rules.isRed(lowerCard) != Rules.isRed(higherCard));

                if (isCorrectOrder && isAlternatingColor) {
                    sequenceLength++;
                } else {
                    break;
                }
            }
            
            if (sequenceLength > 1) {
                totalCardsInOrderedSequences += sequenceLength;
            }
        }
        return totalCardsInOrderedSequences;
    }

    private int calculateBlockedPenalty(GameState state) {
        int blockers = 0;
        Map<String, Integer> nextExpectedRank = getNextExpectedRanks(state);

        for (Stack<Card> tableauPile : state.getTableauPiles()) {
            if (tableauPile.isEmpty()) continue;
            
            for (int i = 0; i < tableauPile.size() - 1; i++) {
                Card cardUnderneath = tableauPile.get(i);
                Integer expectedRank = nextExpectedRank.get(cardUnderneath.getSuit());
                if (expectedRank != null && expectedRank > 0 && Rules.getRankValue(cardUnderneath.getRank()) == expectedRank) {
                    blockers += tableauPile.size() - (i + 1);
                    break;
                }
            }
        }
        return blockers;
    }

    private Map<String, Integer> getNextExpectedRanks(GameState state) {
        Map<String, Integer> nextExpectedRankMap = new HashMap<>();
        String[] suits = {"Clubs", "Diamonds", "Hearts", "Spades"};
        for (String suit : suits) {
            nextExpectedRankMap.put(suit, 1);
        }

        for (Stack<Card> homePile : state.getHomeCells()) {
            if (!homePile.isEmpty()) {
                Card topCard = homePile.peek();
                String suit = topCard.getSuit();
                int currentRank = Rules.getRankValue(topCard.getRank());
                nextExpectedRankMap.put(suit, currentRank < 13 ? currentRank + 1 : 0);
            }
        }
        return nextExpectedRankMap;
    }
}