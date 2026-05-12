package DotsBoxes.player.ai;

import DotsBoxes.board.Action;
import DotsBoxes.board.Board;
import DotsBoxes.player.ActionStrategy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Strategie experte robuste pour Dots and Boxes.
 *
 * Philosophie :
 * 1. priorite absolue aux captures ;
 * 2. sinon priorite aux coups safe ;
 * 3. ordre de coups expert ;
 * 4. recherche Negamax + Alpha-Beta sans time budget artificiel ;
 * 5. heuristique orientee score + securite + structure locale.
 *
 * Objectif :
 * etre plus fort en pratique qu'une simple heuristique,
 * sans toucher a AlphaBetaActionStrategy.
 */
public class ExpertActionStrategy implements ActionStrategy {

    private static final int INF = 1_000_000_000;

    private final int maxDepth;

    public ExpertActionStrategy(int depth) {
        if (depth <= 0) {
            throw new IllegalArgumentException("La profondeur doit etre strictement positive.");
        }
        this.maxDepth = depth;
    }

    @Override
    public String getName() {
        return "Expert";
    }

    @Override
    public Action selectAction(Board board, int playerId) {
        if (board == null) {
            throw new IllegalArgumentException("board ne peut pas etre null");
        }
        if (board.isFinished()) {
            return null;
        }

        List<Action> validMoves = board.getAvailableActions();
        if (validMoves.isEmpty()) {
            return null;
        }

        List<Action> captureMoves = new ArrayList<>();
        List<Action> safeMoves = new ArrayList<>();
        List<Action> riskyMoves = new ArrayList<>();

        for (Action move : validMoves) {
            int closed = boxesClosedBy(board, move);
            if (closed > 0) {
                captureMoves.add(move);
            } else if (isSafe(board, move)) {
                safeMoves.add(move);
            } else {
                riskyMoves.add(move);
            }
        }

        // 1) Captures : toujours prioritaires.
        // On ne prend pas juste "la plus jolie" : on calcule.
        if (!captureMoves.isEmpty()) {
            List<Action> orderedCaptures = orderMoves(board, captureMoves);
            return searchBestMove(board, playerId, orderedCaptures, maxDepth);
        }

        // 2) S'il existe des coups safe, on cherche parmi eux.
        if (!safeMoves.isEmpty()) {
            List<Action> orderedSafe = orderMoves(board, safeMoves);
            return searchBestMove(board, playerId, orderedSafe, maxDepth);
        }

        // 3) Sinon, tous les coups sont dangereux : on cherche sur tous.
        List<Action> orderedAll = orderMoves(board, validMoves);
        return searchBestMove(board, playerId, orderedAll, maxDepth);
    }

    /* ============================================================
       RECHERCHE
       ============================================================ */

    private Action searchBestMove(Board board, int playerId, List<Action> rootMoves, int depth) {
        List<Action> ordered = orderMoves(board, rootMoves);

        Action bestMove = ordered.get(0);
        int bestValue = -INF;
        int alpha = -INF;
        int beta = INF;

        for (Action move : ordered) {
            Board child = new Board(board);
            int closed = child.apply(move, playerId);

            int value;
            if (closed > 0) {
                value = negamax(child, playerId, depth - 1, alpha, beta, playerId);
            } else {
                value = -negamax(child, 1 - playerId, depth - 1, -beta, -alpha, playerId);
            }

            if (value > bestValue) {
                bestValue = value;
                bestMove = move;
            }

            alpha = Math.max(alpha, value);
        }

        return bestMove;
    }

    private int negamax(Board board,
                        int playerToMove,
                        int depth,
                        int alpha,
                        int beta,
                        int rootPlayer) {

        if (board.isFinished()) {
            return finalScoreDiff(board, rootPlayer);
        }

        List<Action> moves = board.getAvailableActions();
        if (moves.isEmpty()) {
            return finalScoreDiff(board, rootPlayer);
        }

        if (depth <= 0) {
            boolean hasCapture = false;
            for (Action move : moves) {
                if (boxesClosedBy(board, move) > 0) {
                    hasCapture = true;
                    break;
                }
            }

            if (hasCapture) {
                return quiescenceCaptures(board, playerToMove, alpha, beta, rootPlayer);
            }
            return heuristic(board, rootPlayer);
        }

        List<Action> ordered = orderMoves(board, moves);
        int best = -INF;

        for (Action move : ordered) {
            Board child = new Board(board);
            int closed = child.apply(move, playerToMove);

            int val;
            if (closed > 0) {
                val = negamax(child, playerToMove, depth - 1, alpha, beta, rootPlayer);
            } else {
                val = -negamax(child, 1 - playerToMove, depth - 1, -beta, -alpha, rootPlayer);
            }

            best = Math.max(best, val);
            alpha = Math.max(alpha, val);

            if (alpha >= beta) {
                break;
            }
        }

        return best;
    }

    private int quiescenceCaptures(Board board,
                                   int playerToMove,
                                   int alpha,
                                   int beta,
                                   int rootPlayer) {

        int standPat = heuristic(board, rootPlayer);
        if (standPat >= beta) {
            return beta;
        }
        if (alpha < standPat) {
            alpha = standPat;
        }

        List<Action> captureMoves = new ArrayList<>();
        for (Action move : board.getAvailableActions()) {
            if (boxesClosedBy(board, move) > 0) {
                captureMoves.add(move);
            }
        }

        if (captureMoves.isEmpty()) {
            return standPat;
        }

        captureMoves = orderMoves(board, captureMoves);

        for (Action move : captureMoves) {
            Board child = new Board(board);
            int closed = child.apply(move, playerToMove);

            int score;
            if (closed > 0) {
                score = quiescenceCaptures(child, playerToMove, alpha, beta, rootPlayer);
            } else {
                score = -quiescenceCaptures(child, 1 - playerToMove, -beta, -alpha, rootPlayer);
            }

            if (score >= beta) {
                return beta;
            }
            if (score > alpha) {
                alpha = score;
            }
        }

        return alpha;
    }

    /* ============================================================
       HEURISTIQUE
       ============================================================ */

    private int heuristic(Board board, int playerId) {
        int opponent = 1 - playerId;

        int scoreDiff = board.getScore(playerId) - board.getScore(opponent);

        int capturePotential = 0;
        int safeCount = 0;
        int riskyCount = 0;
        int thirdSideCount = 0;
        int threeSidedBoxes = 0;
        int twoSidedBoxes = 0;

        List<Action> actions = board.getAvailableActions();
        for (Action move : actions) {
            int closed = boxesClosedBy(board, move);
            if (closed > 0) {
                capturePotential += closed;
            } else if (isSafe(board, move)) {
                safeCount++;
            } else {
                riskyCount++;
            }

            thirdSideCount += countCreatedThirdSides(board, move);
        }

        for (int r = 0; r < board.getRows() - 1; r++) {
            for (int c = 0; c < board.getCols() - 1; c++) {
                if (board.getBoxOwner(r, c) != -1) {
                    continue;
                }

                int openSides = countOpenSides(board, r, c);
                if (openSides == 1) {
                    threeSidedBoxes++;
                } else if (openSides == 2) {
                    twoSidedBoxes++;
                }
            }
        }

        return 150 * scoreDiff
                + 25 * capturePotential
                + 12 * safeCount
                - 10 * riskyCount
                - 18 * thirdSideCount
                - 35 * threeSidedBoxes
                - 6 * twoSidedBoxes;
    }

    private int finalScoreDiff(Board board, int rootPlayer) {
        return 10_000 * (board.getScore(rootPlayer) - board.getScore(1 - rootPlayer));
    }

    /* ============================================================
       ORDRE DES COUPS
       ============================================================ */

    private List<Action> orderMoves(Board board, List<Action> moves) {
        List<Action> ordered = new ArrayList<>(moves);
        ordered.sort(Comparator.comparingInt((Action m) -> movePriority(board, m)).reversed());
        return ordered;
    }

    private int movePriority(Board board, Action move) {
        int captures = boxesClosedBy(board, move);
        if (captures > 0) {
            // captures d'abord
            return 10_000 + 1000 * captures - 30 * opponentImmediateCapturesAfter(board, move);
        }

        if (isSafe(board, move)) {
            // safe move central > safe move excentre
            return 5_000 + centralityBonus(board, move);
        }

        // parmi les coups dangereux, minimiser les 3e cotes creees
        int createdThirdSides = countCreatedThirdSides(board, move);
        return 1_000 - 300 * createdThirdSides + centralityBonus(board, move);
    }

    private int centralityBonus(Board board, Action move) {
        int midR = board.getRows() / 2;
        int midC = board.getCols() / 2;
        int dist = Math.abs(move.getRow() - midR) + Math.abs(move.getCol() - midC);
        return -dist;
    }

    /* ============================================================
       ANALYSE LOCALE
       ============================================================ */

    private boolean isSafe(Board board, Action move) {
        return boxesClosedBy(board, move) == 0 && !wouldCreateThirdSide(board, move);
    }

    private int boxesClosedBy(Board board, Action move) {
        int count = 0;
        for (Cell box : adjacentBoxes(board, move)) {
            if (board.getBoxOwner(box.r, box.c) == -1
                    && countOpenSides(board, box.r, box.c) == 1) {
                count++;
            }
        }
        return count;
    }

    private boolean wouldCreateThirdSide(Board board, Action move) {
        if (boxesClosedBy(board, move) > 0) {
            return false;
        }

        for (Cell box : adjacentBoxes(board, move)) {
            if (board.getBoxOwner(box.r, box.c) != -1) {
                continue;
            }

            int openSides = countOpenSides(board, box.r, box.c);
            if (openSides == 2 && moveBordersBox(move, box.r, box.c)) {
                return true;
            }
        }
        return false;
    }

    private int countCreatedThirdSides(Board board, Action move) {
        if (boxesClosedBy(board, move) > 0) {
            return 0;
        }

        int count = 0;
        for (Cell box : adjacentBoxes(board, move)) {
            if (board.getBoxOwner(box.r, box.c) != -1) {
                continue;
            }

            int openSides = countOpenSides(board, box.r, box.c);
            if (openSides == 2 && moveBordersBox(move, box.r, box.c)) {
                count++;
            }
        }
        return count;
    }

    private int opponentImmediateCapturesAfter(Board board, Action move) {
        Board child = new Board(board);
        child.apply(move, 0); // valeur arbitraire ici : seuls les segments comptent pour cette mesure

        int total = 0;
        for (Action next : child.getAvailableActions()) {
            total += boxesClosedBy(child, next);
        }
        return total;
    }

    private int countOpenSides(Board board, int boxR, int boxC) {
        int open = 0;
        if (!board.isHEdgeSet(boxR, boxC)) open++;
        if (!board.isHEdgeSet(boxR + 1, boxC)) open++;
        if (!board.isVEdgeSet(boxR, boxC)) open++;
        if (!board.isVEdgeSet(boxR, boxC + 1)) open++;
        return open;
    }

    private List<Cell> adjacentBoxes(Board board, Action move) {
        List<Cell> result = new ArrayList<>(2);
        int r = move.getRow();
        int c = move.getCol();

        if (move.getType() == Action.Type.HORIZONTAL) {
            if (r > 0) {
                result.add(new Cell(r - 1, c));
            }
            if (r < board.getRows() - 1) {
                result.add(new Cell(r, c));
            }
        } else {
            if (c > 0) {
                result.add(new Cell(r, c - 1));
            }
            if (c < board.getCols() - 1) {
                result.add(new Cell(r, c));
            }
        }

        return result;
    }

    private boolean moveBordersBox(Action move, int boxR, int boxC) {
        int r = move.getRow();
        int c = move.getCol();

        if (move.getType() == Action.Type.HORIZONTAL) {
            return (r == boxR && c == boxC) || (r == boxR + 1 && c == boxC);
        } else {
            return (r == boxR && c == boxC) || (r == boxR && c == boxC + 1);
        }
    }

    /* ============================================================
       STRUCTURE INTERNE
       ============================================================ */

    private static class Cell {
        final int r;
        final int c;

        Cell(int r, int c) {
            this.r = r;
            this.c = c;
        }
    }
}