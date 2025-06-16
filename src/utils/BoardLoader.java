package utils;

import models.Card;
import models.GameState;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Stack;

public class BoardLoader {

    public static GameState loadFromFile(String filename) throws IOException {
        GameState state = new GameState();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {            
            String line;
            for (int i = 0; i < 8; i++) {
                do {
                    line = reader.readLine();
                } while (line != null && (line.trim().startsWith("#") || line.trim().isEmpty()));
                
                if (line != null && !line.trim().equals("empty")) {
                    String[] cardStrings = line.trim().split(",");
                    Stack<Card> pile = state.getTableauPiles().get(i);
                    for (String cardStr : cardStrings) {
                        cardStr = cardStr.trim();
                        if (!cardStr.isEmpty() && !cardStr.equals("empty")) {
                            Card card = parseCard(cardStr);
                            if (card != null) {
                                pile.push(card);
                            }
                        }
                    }
                }
            }
            
            do {
                line = reader.readLine();
            } while (line != null && (line.trim().startsWith("#") || line.trim().isEmpty()));
            
            if (line != null) {
                String[] freeCellStrings = line.trim().split(",");
                for (int i = 0; i < Math.min(4, freeCellStrings.length); i++) {
                    String cardStr = freeCellStrings[i].trim();
                    if (!cardStr.isEmpty() && !cardStr.equals("empty")) {
                        Card card = parseCard(cardStr);
                        state.getFreeCells().set(i, card);
                    }
                }
            }
            
            for (int i = 0; i < 4; i++) {
                do {
                    line = reader.readLine();
                } 
                while (line != null && (line.trim().startsWith("#") || line.trim().isEmpty()));
                
                if (line != null && !line.trim().equals("empty")) {
                    String[] cardStrings = line.trim().split(",");
                    Stack<Card> homePile = state.getHomeCells().get(i);
                    for (String cardStr : cardStrings) {
                        cardStr = cardStr.trim();
                        if (!cardStr.isEmpty() && !cardStr.equals("empty")) {
                            Card card = parseCard(cardStr);
                            if (card != null) {
                                homePile.push(card);
                            }
                        }
                    }
                }
            }
        }
        return state;
    }
    
    private static Card parseCard(String cardStr) {
        try {
            String[] parts = cardStr.split(" of ");
            if (parts.length == 2) {
                String rank = parts[0].trim();
                String suit = parts[1].trim();
                return new Card(suit, rank);
            }
        } catch (Exception e) {
            System.err.println("Error parsing card: " + cardStr);
        }
        return null;
    }
    
    public static void saveToFile(GameState state, String filename) throws IOException {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(filename)) {
            for (int i = 0; i < 8; i++) {
                Stack<Card> pile = state.getTableauPiles().get(i);
                if (pile.isEmpty()) {
                    writer.println("empty");
                } 
                else {
                    StringBuilder sb = new StringBuilder();
                    for (int j = 0; j < pile.size(); j++) {
                        if (j > 0) sb.append(", ");
                        sb.append(pile.get(j).toString());
                    }
                    writer.println(sb.toString());
                }
            }
            
            StringBuilder freeCellsLine = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                if (i > 0) freeCellsLine.append(", ");
                Card card = state.getFreeCells().get(i);
                freeCellsLine.append(card == null ? "empty" : card.toString());
            }
            writer.println(freeCellsLine.toString());
            
            for (int i = 0; i < 4; i++) {
                Stack<Card> pile = state.getHomeCells().get(i);
                if (pile.isEmpty()) {
                    writer.println("empty");
                } 
                else {
                    StringBuilder sb = new StringBuilder();
                    for (int j = 0; j < pile.size(); j++) {
                        if (j > 0) sb.append(", ");
                        sb.append(pile.get(j).toString());
                    }
                    writer.println(sb.toString());
                }
            }
        }
    }
}
