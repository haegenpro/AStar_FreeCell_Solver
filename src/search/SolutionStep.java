package search;

import models.Card;
import java.util.List;

public class SolutionStep {
    public final String primaryMoveNotation;
    public final String primaryMoveDescription;
    public final List<Card> automatedMoves;

    public SolutionStep(String primaryMoveNotation, String primaryMoveDescription, List<Card> automatedMoves) {
        this.primaryMoveNotation = primaryMoveNotation;
        this.primaryMoveDescription = primaryMoveDescription;
        this.automatedMoves = automatedMoves;
    }
}