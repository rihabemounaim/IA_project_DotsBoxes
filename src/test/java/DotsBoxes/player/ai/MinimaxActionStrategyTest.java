package DotsBoxes.player.ai;

import DotsBoxes.board.Action;
import DotsBoxes.board.Board;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MinimaxActionStrategyTest {

    @Test
    void selectActionShouldReturnNullWhenNoActionAvailable() {
        Board board = new Board(2, 2);
        board.apply(new Action(Action.Type.HORIZONTAL, 0, 0), 0);
        board.apply(new Action(Action.Type.HORIZONTAL, 1, 0), 1);
        board.apply(new Action(Action.Type.VERTICAL, 0, 0), 0);
        board.apply(new Action(Action.Type.VERTICAL, 0, 1), 1);

        MinimaxActionStrategy strategy = new MinimaxActionStrategy(3);

        assertNull(strategy.selectAction(board, 0));
    }

    @Test
    void selectActionShouldChooseImmediateBoxClosingMove() {
        Board board = new Board(2, 2);
        board.apply(new Action(Action.Type.HORIZONTAL, 0, 0), 0);
        board.apply(new Action(Action.Type.HORIZONTAL, 1, 0), 1);
        board.apply(new Action(Action.Type.VERTICAL, 0, 0), 0);

        MinimaxActionStrategy strategy = new MinimaxActionStrategy(2);

        Action best = strategy.selectAction(board, 1);

        assertEquals(new Action(Action.Type.VERTICAL, 0, 1), best);
    }

    @Test
    void getNameShouldReturnMinimax() {
        assertEquals("Minimax", new MinimaxActionStrategy(1).getName());
    }

    @Test
    void testReplayRule_chainCapture() {
        Board board = new Board(2, 3);
        board.apply(new Action(Action.Type.HORIZONTAL, 0, 0), 1);
        board.apply(new Action(Action.Type.HORIZONTAL, 1, 0), 1);
        board.apply(new Action(Action.Type.VERTICAL,   0, 0), 1);
        board.apply(new Action(Action.Type.HORIZONTAL, 0, 1), 1);
        board.apply(new Action(Action.Type.HORIZONTAL, 1, 1), 1);
        board.apply(new Action(Action.Type.VERTICAL,   0, 2), 1);

        MinimaxActionStrategy ai = new MinimaxActionStrategy(4);
        Action best = ai.selectAction(board, 0);
        assertEquals(new Action(Action.Type.VERTICAL, 0, 1), best);

        Board copy = new Board(board);
        int closed = copy.apply(best, 0);
        assertEquals(2, closed);
    }

    @Test
    void testOptimalMove_avoidsGivingBoxToOpponent() {
        Board board = new Board(2, 3);
        board.apply(new Action(Action.Type.HORIZONTAL, 0, 0), 1);
        board.apply(new Action(Action.Type.VERTICAL,   0, 0), 1);

        MinimaxActionStrategy ai = new MinimaxActionStrategy(4);
        Action best = ai.selectAction(board, 0);

        assertNotEquals(new Action(Action.Type.HORIZONTAL, 1, 0), best);
        assertNotEquals(new Action(Action.Type.VERTICAL,   0, 1), best);
    }

    @Test
    void testDifferentDepths_alwaysReturnAction() {
        Board board = new Board(3, 3);
        assertNotNull(new MinimaxActionStrategy(1).selectAction(board, 0));
        assertNotNull(new MinimaxActionStrategy(4).selectAction(board, 0));
        assertNotNull(new MinimaxActionStrategy(6).selectAction(board, 0));
    }

}
