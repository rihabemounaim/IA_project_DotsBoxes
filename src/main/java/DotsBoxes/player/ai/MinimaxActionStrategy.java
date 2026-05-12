package DotsBoxes.player.ai;

import DotsBoxes.board.Action;
import DotsBoxes.board.Board;
import DotsBoxes.player.ActionStrategy;
import java.util.List;

public class MinimaxActionStrategy implements ActionStrategy {

    private final int maxDepth;

    public MinimaxActionStrategy(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    @Override
    public Action selectAction(Board board, int playerId) {

        List<Action> actions = board.getAvailableActions();
        Action meilleurAction=null;
        int meilleurScore= -1000000000;

        for(Action action : actions) {
            Board copy = new Board(board);
            int closed = copy.apply(action, playerId);
            boolean replay = closed > 0;

            int currentTour = replay ? playerId : opponent(playerId);

            int score = minimax(copy, maxDepth - 1, currentTour, playerId, replay); //   a regler pour optimiser l'intelligence
            if (score > meilleurScore) {
                meilleurScore = score;
                meilleurAction = action;
            }

        }
        return meilleurAction;
    }

    private int minimax(Board board, int depth, int currentId, int playerId, boolean maximizing) {

        if (depth == 0 || board.isFinished()) {
            return eval(board, playerId);
        }
        List<Action> actions = board.getAvailableActions();
        if (maximizing) {

            int value = -100000000;
            for (Action action : actions) {
                Board copy = new Board(board);
                int closed = copy.apply(action, currentId); //pour savoir si on a close des box
                boolean replay = closed > 0;      //si oui replay est vrai on rejoue

                int currentTour = replay ? currentId : opponent(currentId);

                int score = minimax(copy, depth - 1, currentTour, playerId, replay); //applique l'ppel recursif pronfondeur -
                value = Math.max(value, score);

            }
            return value;
        } else {
            int value = 100000000;
            for (Action action : actions) {
                Board copy = new Board(board);
                int closed = copy.apply(action, currentId);
                boolean replay = closed > 0;

                int currentTour = replay ? currentId : opponent(currentId);
                
                int score = minimax(copy, depth - 1, currentTour, playerId, !replay);
                value = Math.min(value, score);

            }
            return value;
        }
    }

    private int opponent(int playerId) {
        if (playerId == 0) return 1;
        else return 0;
    }

    private int eval(Board board, int aiId) {     //a regler pour augmenter l'efficacite de l'ia
        int score = 0;
        int opponent = opponent(aiId);
        int param=100;

        score+= (board.getScore(aiId) - board.getScore(opponent))*param;
        for (int r = 0; r < board.getRows() - 1; r++) {
            for (int c = 0; c < board.getCols() - 1; c++) {
                if (board.getBoxOwner(r, c) != -1) continue;
                if (countSides(board, r, c) == 2) score -= 20;
            }
        }

        return score;
    }

    private int countSides(Board board, int r, int c) {
        int count = 0;
        if (board.isHEdgeSet(r,     c)) count++; // top edge
        if (board.isHEdgeSet(r + 1, c)) count++; // bottom edge
        if (board.isVEdgeSet(r,     c)) count++; // left edge
        if (board.isVEdgeSet(r,  c + 1)) count++; // right edge
        return count;
    }

    @Override
    public String getName() {
        return "Minimax";
    }
}