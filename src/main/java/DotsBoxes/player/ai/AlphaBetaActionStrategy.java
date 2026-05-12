package DotsBoxes.player.ai;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap ;

import javax.print.DocFlavor.INPUT_STREAM;

import DotsBoxes.board.Action;
import DotsBoxes.board.Board;
import DotsBoxes.observers.AlphaBetaPruningObserver;
import DotsBoxes.observers.NodeCounterObserver;
import DotsBoxes.player.ActionStrategy;

/**
 * Version etudiants.
 *
 * TODO:
 * - Implementer Minimax avec elagage Alpha-Beta.
 * - Utiliser les observateurs pour compter coupes et noeuds visites.
 * - Tenir compte de la regle de rejeu apres fermeture de boxe.
 */
public class AlphaBetaActionStrategy implements ActionStrategy {

    private final int maxDepth;
    private final AlphaBetaPruningObserver observer;
    private final NodeCounterObserver nodeCounter;
    //table de transposition pour memoriser chaque etas de board avec sa valeur
    // pour eviter  de faire recaler la valeur des board deja calculer
    private final Map<String, Integer> transpositionTable = new HashMap<>();

    public AlphaBetaActionStrategy(int maxDepth, AlphaBetaPruningObserver observer, NodeCounterObserver nodeCounter) {
        this.maxDepth = maxDepth;
        this.observer = observer;
        this.nodeCounter = nodeCounter;
    }

    @Override
    public Action selectAction(Board board, int playerId) {
        //transpositionTable.clear() ;
        List<Action> actions = board.getAvailableActions() ;
        Collections.shuffle(actions);
        //arreter s'il y a plus de coup à jouer
        if (actions.isEmpty()) {
            return null ;
        }



        // donner une valeur min de depart pour choisir le bon coup à jouer 
        // à partir de chercher le coup de la valeur max 
        int val = Integer.MIN_VALUE ; 
        int numAction = 0 ; // numero d'action à choisir à la fin

        for (int i = 0 ; i < actions.size() ; i++){
            Board boardCopy = new Board(board) ;
            int valCoupCourant ;
            boardCopy.apply(actions.get(i), playerId) ;

            if(board.getScore(playerId) < boardCopy.getScore(playerId)){
                
                valCoupCourant = alphaBetaAlgo(boardCopy, playerId, Integer.MIN_VALUE, Integer.MAX_VALUE, this.maxDepth, playerId) ;

            } else{
                if (playerId == 0){
                    valCoupCourant = alphaBetaAlgo(boardCopy, 1, Integer.MIN_VALUE, Integer.MAX_VALUE, this.maxDepth, playerId) ;
                } else{
                    valCoupCourant = alphaBetaAlgo(boardCopy, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, this.maxDepth, playerId) ;
                }
            }
            if (val < valCoupCourant){
                numAction = i ;
                val = valCoupCourant ;
            }
        }
        return actions.get(numAction) ;
    }

    // Algorithme alpha beta 

    public int alphaBetaAlgo(Board board, int playerId, int alpha, int beta, int depth, int maxPlayer){
        nodeCounter.increment();
        observer.incrementNodeCount();
        List<Action> actions = board.getAvailableActions() ;


        // genérer board avec le coup simuler et puis generer la key de ce board pour
        // vérifier est ce qu'il est deja calculer ou pas 
        /*String key = makeKey(board, playerId, depth, maxPlayer);

        if (transpositionTable.containsKey(key)) {
            return transpositionTable.get(key);
        }*/
        // si non continuer notre algo 


        // si on est à la profondeure demandée on s'arrete 
        if (depth <= 0 || actions.isEmpty() ) {
            // stocker la valeur de board dans la table avant de la retourner 
            int eval = Eval(board, maxPlayer, playerId);
            //transpositionTable.put(key, eval);
            return eval;
        }
        
        // cas Max
        if (playerId == maxPlayer){
            int val = Integer.MIN_VALUE ;
            for (int i = 0 ; i < actions.size() ; i++){
                Board boardCopy = new Board(board) ;
                int valCoupCourant ;
                boardCopy.apply(actions.get(i), playerId) ;
                
                if(board.getScore(playerId) < boardCopy.getScore(playerId)){
                    
                    valCoupCourant = alphaBetaAlgo(boardCopy, playerId, alpha, beta, depth-1, maxPlayer) ;

                } else{
                    if (playerId == 0){
                        valCoupCourant = alphaBetaAlgo(boardCopy, 1, alpha, beta, depth-1, maxPlayer) ;
                    } else{
                        valCoupCourant = alphaBetaAlgo(boardCopy, 0, alpha, beta, depth-1, maxPlayer) ;
                    }
                }
                if (val < valCoupCourant){
                    val = valCoupCourant ;
                }
                if (alpha < valCoupCourant) {
                    alpha = valCoupCourant ;
                }
                if (alpha >= beta) {
                    observer.incrementAlphaCut();
                    break;
                } 
            }

            // stocker avant d'envoyer
           // transpositionTable.put(key, val);
            return val;
            
        // cas Min
        } else {
             int val = Integer.MAX_VALUE ;
            for (int i = 0 ; i < actions.size() ; i++){
                Board boardCopy = new Board(board) ;
                int valCoupCourant ;
                boardCopy.apply(actions.get(i), playerId) ;

                
                if(board.getScore(playerId) < boardCopy.getScore(playerId)){
                    
                    valCoupCourant = alphaBetaAlgo(boardCopy, playerId, alpha, beta, depth-1, maxPlayer) ;

                } else{
                    if (playerId == 0){
                        valCoupCourant = alphaBetaAlgo(boardCopy, 1, alpha, beta, depth-1, maxPlayer) ;
                    } else{
                        valCoupCourant = alphaBetaAlgo(boardCopy, 0, alpha, beta, depth-1, maxPlayer) ;
                    }
                }
                if (val > valCoupCourant){
                    val = valCoupCourant ;
                }
                if (beta > valCoupCourant) {
                    beta = valCoupCourant ;
                } 
                if (alpha >= beta) {
                    observer.incrementBetaCut();
                    break ;
                }
            }
            // stocker avant de l'envoyer
            //transpositionTable.put(key, val);
            return val;
           
        }
    }


    // fonction Eval 
    private int Eval(Board board, int maxPlayer, int currentPlayer) {
        int opponentId = (maxPlayer == 0) ? 1 : 0;

        int scoreDiff = board.getScore(maxPlayer) - board.getScore(opponentId);
        int dangerousBoxes = countDangerousBoxes(board);

        if (currentPlayer == maxPlayer) {
            return 20 * scoreDiff + 20 * dangerousBoxes;
        } else {
            return 20 * scoreDiff - 20 * dangerousBoxes;
        }
    }

    //calcule des cases dangereuses dans board
    private int countDangerousBoxes(Board board) {
        int count = 0;

        for (int br = 0; br < board.getRows() - 1; br++) {
            for (int bc = 0; bc < board.getCols() - 1; bc++) {
                int sides = 0;

                if (board.isHEdgeSet(br, bc)) sides++;         // haut
                if (board.isHEdgeSet(br + 1, bc)) sides++;     // bas
                if (board.isVEdgeSet(br, bc)) sides++;         // gauche
                if (board.isVEdgeSet(br, bc + 1)) sides++;     // droite

                if (sides == 3) {
                    count++;
                }
            }
        }

        return count;
    }

    // fonction qui transforme une borad à un string pour l'utiliser comme un key de chaque borad
    // l'aidée c'est de stocker les valeurs des coup de chaque coup possible 
    // est qu'il est fait ou pas 
    private String makeKey(Board board, int playerId, int depth, int maxPlayer) {
        StringBuilder sb = new StringBuilder();

        sb.append(playerId).append('|').append(depth).append('|').append(maxPlayer).append('|');

        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols() - 1; c++) {
                sb.append(board.isHEdgeSet(r, c) ? '1' : '0');
            }
        }

        sb.append('|');

        for (int r = 0; r < board.getRows() - 1; r++) {
            for (int c = 0; c < board.getCols(); c++) {
                sb.append(board.isVEdgeSet(r, c) ? '1' : '0');
            }
        }

        return sb.toString();
    }

    @Override
    public String getName() {
        return "Alpha-Beta";
    }
}
