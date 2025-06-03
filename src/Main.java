import models.Card;
import models.GameState;
import search.AStar;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class Main {

    public static void main(String[] args) {
        System.out.println("Starting FreeCell Solver...");

        // 1. Create a standard 52-card deck
        List<Card> deck = createShuffledDeck();

        // 2. Initialize the GameState with the dealt cards
        GameState initialState = createInitialGameState(deck);

        // 3. Instantiate the A* solver
        AStar solver = new AStar();

        // 4. Run the solver
        System.out.println("\nSearching for a solution...");
        List<String> solutionPath = solver.solve(initialState);

        // 5. Display the solution
        if (solutionPath != null) {
            System.out.println("\nSolution Found! Number of moves: " + solutionPath.size());
            for (int i = 0; i < solutionPath.size(); i++) {
                System.out.println((i + 1) + ". " + solutionPath.get(i));
            }
        } else {
            System.out.println("\nNo solution found for this starting configuration.");
        }
    }

    /**
     * Creates a standard 52-card deck and shuffles it.
     * @return A shuffled list of Card objects.
     */
    private static List<Card> createShuffledDeck() {
        List<Card> deck = new ArrayList<>();
        String[] suits = {"Clubs", "Diamonds", "Hearts", "Spades"};
        String[] ranks = {"Ace", "2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen", "King"};

        for (String suit : suits) {
            for (String rank : ranks) {
                deck.add(new Card(suit, rank)); //
            }
        }
        Collections.shuffle(deck); // Shuffle the deck
        return deck;
    }

    /**
     * Creates the initial GameState by dealing cards into the tableau piles.
     * @param deck The shuffled deck of cards.
     * @return The initial GameState.
     */
    private static GameState createInitialGameState(List<Card> deck) {
        GameState state = new GameState(); //
        int cardIndex = 0;

        // FreeCell deals 7 cards to the first 4 tableau piles and 6 to the last 4.
        for (int i = 0; i < 8; i++) { // There are 8 tableau piles
            int cardsToDeal = (i < 4) ? 7 : 6;
            Stack<Card> currentPile = state.getTableauPiles().get(i); //
            for (int j = 0; j < cardsToDeal; j++) {
                if (cardIndex < deck.size()) {
                    currentPile.push(deck.get(cardIndex++));
                }
            }
        }
        System.out.println("Initial Game State created.");
        // You might want to print the initial state for debugging:
        // printGameState(state);
        return state;
    }

    // Optional: A helper method to print the current game state for debugging
    private static void printGameState(GameState state) {
        System.out.println("\n--- Current Game State ---");
        for (int i = 0; i < state.getTableauPiles().size(); i++) { //
            System.out.println("Tableau " + i + ": " + state.getTableauPiles().get(i)); //
        }
        System.out.println("Free Cells: " + state.getFreeCells()); //
        System.out.print("Home Cells: ");
        for (Stack<Card> pile : state.getHomeCells()) { //
            System.out.print(pile + " ");
        }
        System.out.println("\n--------------------------");
    }
}