package search;

import models.Card; //
import models.GameState; //

import java.util.List;
import java.util.Stack;

public class Heuristic {

    /**
     * Calculates the heuristic value for a given game state.
     * This heuristic counts the number of cards that are currently not in their home cells.
     * It is admissible because each card not in a home cell must take at least one move
     * to eventually reach a home cell.
     *
     * @param state The current game state.
     * @return The estimated minimum number of moves required to place all cards into their home cells.
     */
    public int calculate(GameState state) {
        int cardsInHomeCells = 0;

        // Sum the number of cards in all four home cell piles
        for (Stack<Card> homePile : state.getHomeCells()) {
            cardsInHomeCells += homePile.size();
        }

        // A standard FreeCell deck has 52 cards.
        // The heuristic value is the total number of cards minus the cards already in home cells.
        return 52 - cardsInHomeCells;
    }
}