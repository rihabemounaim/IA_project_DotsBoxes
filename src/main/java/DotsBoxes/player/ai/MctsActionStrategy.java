package DotsBoxes.player.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import DotsBoxes.board.Action;
import DotsBoxes.board.Board;
import DotsBoxes.player.ActionStrategy;

/* 
 Version etudiant.
 *
 * TODO:
 * - Implementer MCTS (selection, expansion, simulation, backpropagation).
 * - Definir la politique UCT.
 **/
public class MctsActionStrategy implements ActionStrategy {
     // Classe interne : un noeud de l'arbre
    private static class MctsNode {
        Board state;           // l'état du plateau à ce noeud
        Action actionFromParent; // le coup qui a mené ici (null pour la racine)
        MctsNode parent;       // le noeud parent (null pour la racine)
        List<MctsNode> children; // les noeuds enfants
        int visits;            // combien de fois ce noeud a été visité
        double reward;         // somme des récompenses (victoires)

        MctsNode(Board state, Action actionFromParent, MctsNode parent) {
            this.state = state;
            this.actionFromParent = actionFromParent;
            this.parent = parent;
            this.children = new ArrayList<>();
            this.visits = 0;
            this.reward = 0.0;
        }
    }
    // Attributs 
    private final int iterations;
    private final Random random;


    





    public MctsActionStrategy(int iterations) {    
       
    if (iterations <= 0) throw new IllegalArgumentException("iterations must be positive");
    this.iterations = iterations;
    this.random = new Random();
    }

    public MctsActionStrategy(int iterations, long seed) {
        this.iterations = iterations;
        this.random = new Random(seed);
    }

    @Override
    public Action selectAction(Board board, int playerId) {
        if (board.getAvailableActions().isEmpty()) return null;

        MctsNode root = new MctsNode(board, null, null);
        for (int i = 0; i < iterations; i++) {
           MctsNode selected = selection(root);
           MctsNode expanded = expansion(selected, playerId);
           double result = simulation(expanded, playerId);
           backpropagation(expanded, result);
              return bestAction(root);
}
        throw new UnsupportedOperationException("TODO etudiant: implementer MctsActionStrategy.selectAction");
    }
    // SELECTION
    private MctsNode selection(MctsNode node) {
    // Descend tant que le noeud a des enfants
    while (!node.children.isEmpty()) {
        node = bestUCT(node);
    }
    return node;
}

private MctsNode bestUCT(MctsNode parent) {
    double C = Math.sqrt(2); // constante d'exploration
    MctsNode best = null;
    double bestScore = Double.NEGATIVE_INFINITY;

    for (MctsNode child : parent.children) {
        double score;
        if (child.visits == 0) {
            score = Double.POSITIVE_INFINITY; // noeud jamais visité = priorité 
        } else {
            score = (child.reward / child.visits)
                  + C * Math.sqrt(Math.log(parent.visits) / child.visits);
        }
        if (score > bestScore) {
            bestScore = score;
            best = child;
        }
    }
    return best;
}

//EXPANSION
private MctsNode expansion(MctsNode node, int playerId) {
     // Si la partie est terminée à ce noeud on ne fait pas d'expansion
    if (node.state.isFinished()) return node;
    List<Action> actions = node.state.getAvailableActions();

    if (node.children.isEmpty()) {
        for (Action action : actions) {
            Board copy = new Board(node.state);  // copie du plateau
            copy.apply(action, playerId);         // applique le coup
            MctsNode child = new MctsNode(copy, action, node);
            node.children.add(child);
        }
    }
// Retourne un enfant au hasard parmi ceux jamais visites 
    List<MctsNode> unvisited = new ArrayList<>();
    for (MctsNode child : node.children) {
        if (child.visits == 0) unvisited.add(child);
    }

    if (!unvisited.isEmpty()) {
        return unvisited.get(random.nextInt(unvisited.size()));
    }

    // Tous visités  retourne le meilleur UCT
    return bestUCT(node);
}
// SIMULATION
private double simulation(MctsNode node, int playerId) {
    Board copy = new Board(node.state); // copie pour ne pas modifier l'arbre
    int currentPlayer = playerId;

    // Joue au hasard jusqu'à la fin
    while (!copy.isFinished()) {
        List<Action> actions = copy.getAvailableActions();
        Action randomAction = actions.get(random.nextInt(actions.size()));
        int closed = copy.apply(randomAction, currentPlayer);

        // Si on a fermé une boîte donc on rejoue, sinon c'est l'adversaire
        if (closed == 0) {
            currentPlayer = (currentPlayer == 0) ? 1 : 0;
        }
    }

    // Évalue le résultat final
    int myScore = copy.getScore(playerId);
    int opponentScore = copy.getScore(playerId == 0 ? 1 : 0);

    if (myScore > opponentScore) return 1.0;  // victoire
    if (myScore < opponentScore) return 0.0;  // défaite
    return 0.5;                                // égalité
}
// BACKPROPAGATION
private void backpropagation(MctsNode node, double result) {
    MctsNode current = node;

    // Remonte jusqu'à la racine
    while (current != null) {
        current.visits++;
        current.reward += result;
        current = current.parent;
    }
}
private Action bestAction(MctsNode root) {
    MctsNode best = null;
    int bestVisits = -1;

    // Le coup le plus visité = le meilleur
    for (MctsNode child : root.children) {
        if (child.visits > bestVisits) {
            bestVisits = child.visits;
            best = child;
        }
    }

    return best != null ? best.actionFromParent : null;
}
    @Override
    public String getName() {
        return "MCTS";
    }

}
