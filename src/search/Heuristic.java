package search;

import models.Card; //
import models.GameState;

import java.util.List;
import java.util.Stack;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.ArrayList;

public class Heuristic {

    private static final int WEIGHT_HOME_CELLS = 100;

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

    public int calculate(GameState state) {
        int cardsInHomeCells = 0;
        for (Stack<Card> homePile : state.getHomeCells()) { //
            cardsInHomeCells += homePile.size();
        }

        int totalBlockers = calculateBlockers(state);

        return (52 - cardsInHomeCells) * WEIGHT_HOME_CELLS + totalBlockers;
    }

    private int calculateBlockers(GameState state) {
        int blockers = 0;
        String[] suits = {"Clubs", "Diamonds", "Hearts", "Spades"};
        Map<String, Integer> nextExpectedRankForSuit = new HashMap<>();

        for (String suit : suits) {
            nextExpectedRankForSuit.put(suit, 1);
        }

        for (Stack<Card> homePile : state.getHomeCells()) {
            if (!homePile.isEmpty()) {
                Card topHomeCard = homePile.peek();
                String suit = topHomeCard.getSuit();
                int currentRankValue = getRankValue(topHomeCard.getRank());
                if (currentRankValue < 13) {
                    nextExpectedRankForSuit.put(suit, currentRankValue + 1);
                } else {
                    nextExpectedRankForSuit.put(suit, 0);
                }
            }
        }

        for (Stack<Card> tableauPile : state.getTableauPiles()) {
            if (tableauPile.isEmpty()) continue;

            List<Card> cardsInPile = new ArrayList<>(tableauPile);
            Collections.reverse(cardsInPile);

            for (int i = 0; i < cardsInPile.size(); i++) {
                Card currentCard = cardsInPile.get(i); //
                String currentCardSuit = currentCard.getSuit(); //
                int currentCardRank = getRankValue(currentCard.getRank()); //

                Integer expectedRank = nextExpectedRankForSuit.get(currentCardSuit);

                if (expectedRank == null || expectedRank == 0) continue;

                if (currentCardRank == expectedRank) {
                    blockers += (cardsInPile.size() - 1 - i);
                }
            }
        }
        return blockers;
    }
}