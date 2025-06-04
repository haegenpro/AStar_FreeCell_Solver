import models.Card;
import models.GameState;
import search.AStar;
import utils.BoardLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class Main {

    public static void main(String[] args) {
        System.out.println("Starting FreeCell Solver...");
        System.out.println("Usage: java Main [board_file.txt]");
        System.out.println("Available boards: easy.txt, medium.txt, hard.txt, boss.txt, impossible.txt");
        System.out.println();

        GameState initialState = null;
        
        if (args.length > 0) {
            String filename = args[0];
            try {
                System.out.println("Loading game from file: " + filename);
                initialState = BoardLoader.loadFromFile("boards/" + filename);
            } catch (Exception e) {
                System.err.println("Error loading file: " + e.getMessage());
                System.out.println("Available board files: easy.txt, medium.txt, hard.txt, boss.txt, impossible.txt");
                return;
            }
        } else {
            System.out.println("Choose a test case:");
            System.out.println("1. Easy test case (from image)");
            System.out.println("2. Medium difficulty");
            System.out.println("3. Hard difficulty");
            System.out.println("4. Boss difficulty (very hard)");
            System.out.println("5. Impossible (unsolvable)");
            System.out.println("6. Random shuffled game");
            System.out.println("7. Programmatic easy test (nearly solved)");
            
            int choice = 1;
            
            switch (choice) {
                case 1:
                    try {
                        System.out.println("Loading easy test case from file...");
                        initialState = BoardLoader.loadFromFile("boards/easy.txt");
                    } catch (Exception e) {
                        System.err.println("Error loading easy.txt: " + e.getMessage());
                        return;
                    }
                    break;
                case 2:
                    try {
                        System.out.println("Loading medium difficulty...");
                        initialState = BoardLoader.loadFromFile("boards/medium.txt");
                    } catch (Exception e) {
                        System.err.println("Error loading medium.txt: " + e.getMessage());
                        return;
                    }
                    break;
                case 3:
                    try {
                        System.out.println("Loading hard difficulty...");
                        initialState = BoardLoader.loadFromFile("boards/hard.txt");
                    } catch (Exception e) {
                        System.err.println("Error loading hard.txt: " + e.getMessage());
                        return;
                    }
                    break;
                case 4:
                    try {
                        System.out.println("Loading boss difficulty...");
                        initialState = BoardLoader.loadFromFile("boards/boss.txt");
                    } catch (Exception e) {
                        System.err.println("Error loading boss.txt: " + e.getMessage());
                        return;
                    }
                    break;
                case 5:
                    try {
                        System.out.println("Loading impossible case...");
                        initialState = BoardLoader.loadFromFile("boards/impossible.txt");
                    } catch (Exception e) {
                        System.err.println("Error loading impossible.txt: " + e.getMessage());
                        return;
                    }
                    break;
                case 6:
                    System.out.println("Running random shuffled game...");
                    List<Card> deck = createShuffledDeck();
                    initialState = createInitialGameState(deck);
                    break;
                case 7:
                    System.out.println("Running programmatic easy test case (nearly solved)...");
                    initialState = createEasyTestCase();
                    break;
                default:
                    System.out.println("Invalid choice, using easy test case...");
                    try {
                        initialState = BoardLoader.loadFromFile("boards/easy.txt");
                    } catch (Exception e) {
                        System.err.println("Error loading easy.txt: " + e.getMessage());
                        return;
                    }
                    break;
            }
        }

        printGameState(initialState);

        AStar solver = new AStar();

        System.out.println("\nSearching for a solution...");
        List<String> solutionPath = solver.solve(initialState);

        if (solutionPath != null) {
            System.out.println("\nSolution Found! Number of moves: " + solutionPath.size());
            for (int i = 0; i < solutionPath.size(); i++) {
                System.out.println((i + 1) + ". " + solutionPath.get(i));
            }
        } else {
            System.out.println("\nNo solution found for this starting configuration.");
        }
    }

    private static GameState createEasyTestCase() {
        GameState state = new GameState();
        
        String[] suits = {"Clubs", "Diamonds", "Hearts", "Spades"};
        String[] ranks = {"Ace", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
        
        for (int i = 0; i < suits.length; i++) {
            Stack<Card> homePile = state.getHomeCells().get(i);
            for (String rank : ranks) {
                homePile.push(new Card(suits[i], rank));
            }
        }
        
        state.getTableauPiles().get(0).push(new Card("Hearts", "Queen"));
        state.getTableauPiles().get(0).push(new Card("Spades", "King"));
        
        state.getTableauPiles().get(1).push(new Card("Diamonds", "Queen"));
        state.getTableauPiles().get(1).push(new Card("Clubs", "King"));
        
        state.getTableauPiles().get(2).push(new Card("Spades", "Queen"));
        state.getTableauPiles().get(2).push(new Card("Hearts", "King"));
        
        state.getTableauPiles().get(3).push(new Card("Clubs", "Queen"));
        state.getTableauPiles().get(3).push(new Card("Diamonds", "King"));
        
        state.getFreeCells().set(0, new Card("Clubs", "Jack"));
        state.getFreeCells().set(1, new Card("Diamonds", "Jack"));
        state.getFreeCells().set(2, new Card("Hearts", "Jack"));
        state.getFreeCells().set(3, new Card("Spades", "Jack"));
        
        return state;
    }

    private static List<Card> createShuffledDeck() {
        List<Card> deck = new ArrayList<>();
        String[] suits = {"Clubs", "Diamonds", "Hearts", "Spades"};
        String[] ranks = {"Ace", "2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen", "King"};

        for (String suit : suits) {
            for (String rank : ranks) {
                deck.add(new Card(suit, rank));
            }
        }
        Collections.shuffle(deck);
        return deck;
    }

    private static GameState createInitialGameState(List<Card> deck) {
        GameState state = new GameState();
        int cardIndex = 0;

        for (int i = 0; i < 8; i++) {
            int cardsToDeal = (i < 4) ? 7 : 6;
            Stack<Card> currentPile = state.getTableauPiles().get(i);
            for (int j = 0; j < cardsToDeal; j++) {
                if (cardIndex < deck.size()) {
                    currentPile.push(deck.get(cardIndex++));
                }
            }
        }
        System.out.println("Initial Game State created.");
        return state;
    }

    private static void printGameState(GameState state) {
        System.out.println("\n--- Current Game State ---");
        
        for (int i = 0; i < state.getTableauPiles().size(); i++) {
            Stack<Card> pile = state.getTableauPiles().get(i);
            System.out.println("Tableau " + i + " (" + pile.size() + " cards): " + 
                (pile.isEmpty() ? "[empty]" : pile.toString()));
        }
        
        System.out.print("Free Cells: [");
        for (int i = 0; i < state.getFreeCells().size(); i++) {
            Card card = state.getFreeCells().get(i);
            System.out.print(card == null ? "empty" : card.toString());
            if (i < state.getFreeCells().size() - 1) System.out.print(", ");
        }
        System.out.println("]");
        
        System.out.print("Home Cells: ");
        String[] suits = {"Clubs", "Diamonds", "Hearts", "Spades"};
        for (int i = 0; i < state.getHomeCells().size(); i++) {
            Stack<Card> pile = state.getHomeCells().get(i);
            System.out.print(suits[i] + "(" + pile.size() + ")");
            if (!pile.isEmpty()) {
                System.out.print(":" + pile.peek().getRank());
            }
            if (i < state.getHomeCells().size() - 1) System.out.print(", ");
        }
        System.out.println();
        System.out.println("--------------------------");
    }
}