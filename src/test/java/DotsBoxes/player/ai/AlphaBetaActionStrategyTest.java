package DotsBoxes.player.ai;

import DotsBoxes.board.Action;
import DotsBoxes.board.Board;
import DotsBoxes.observers.AlphaBetaPruningObserver;
import DotsBoxes.observers.NodeCounterObserver;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AlphaBetaActionStrategyTest {

    @Test
    void selectActionShouldReturnNullWhenNoActionAvailable() {
        Board board = new Board(2, 2);
        board.apply(new Action(Action.Type.HORIZONTAL, 0, 0), 0);
        board.apply(new Action(Action.Type.HORIZONTAL, 1, 0), 1);
        board.apply(new Action(Action.Type.VERTICAL, 0, 0), 0);
        board.apply(new Action(Action.Type.VERTICAL, 0, 1), 1);

        AlphaBetaActionStrategy strategy = new AlphaBetaActionStrategy(3,
                new AlphaBetaPruningObserver(), new NodeCounterObserver());

        assertNull(strategy.selectAction(board, 0));
    }

    @Test
    void selectActionShouldChooseImmediateBoxClosingMoveAndIncrementNodeCounter() {
        Board board = new Board(2, 2);
        board.apply(new Action(Action.Type.HORIZONTAL, 0, 0), 0);
        board.apply(new Action(Action.Type.HORIZONTAL, 1, 0), 1);
        board.apply(new Action(Action.Type.VERTICAL, 0, 0), 0);

        AlphaBetaPruningObserver observer = new AlphaBetaPruningObserver();
        NodeCounterObserver nodeCounter = new NodeCounterObserver();
        AlphaBetaActionStrategy strategy = new AlphaBetaActionStrategy(2, observer, nodeCounter);

        Action best = strategy.selectAction(board, 1);

        assertAll(
                () -> assertEquals(new Action(Action.Type.VERTICAL, 0, 1), best),
                () -> assertTrue(nodeCounter.getCount() > 0),
                () -> assertTrue(observer.getAlphaCutCount() >= 0),
                () -> assertTrue(observer.getBetaCutCount() >= 0)
        );
    }

    @Test
    void getNameShouldReturnAlphaBeta() {
        AlphaBetaActionStrategy strategy = new AlphaBetaActionStrategy(1,
                new AlphaBetaPruningObserver(), new NodeCounterObserver());
        assertEquals("Alpha-Beta", strategy.getName());
    }
}
