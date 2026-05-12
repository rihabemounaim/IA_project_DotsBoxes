package DotsBoxes.player.automate;

import DotsBoxes.board.Action;
import DotsBoxes.board.Board;
import DotsBoxes.player.ActionStrategy;

import java.util.List;
import java.util.Random;

/**
 * Stratégie gloutonne pour le jeu Dots and Boxes.
 *
 * Principe :
 * - Prioriser les coups qui permettent de fermer immédiatement une ou plusieurs cases
 * - Sinon, jouer un coup valide choisi aléatoirement
 *
 * Cette stratégie maximise le gain local (court terme),
 * mais ne prend pas en compte les conséquences futures.
 */
public class GloutonActionStrategy implements ActionStrategy {

    // Générateur aléatoire utilisé si aucun coup "intéressant" n'est trouvé
    private final Random random;

    /**
     * Constructeur par défaut : comportement aléatoire classique
     */
    public GloutonActionStrategy() {
        this.random = new Random();
    }

    /**
     * Constructeur avec seed : permet de reproduire les mêmes parties
     * (utile pour les tests et les comparaisons)
     */
    public GloutonActionStrategy(long seed) {
        this.random = new Random(seed);
    }

    /**
     * Sélectionne une action à jouer selon la stratégie gloutonne.
     *
     * @param board état actuel du jeu
     * @param playerId joueur courant
     * @return action choisie ou null si aucun coup possible
     */
    @Override
    public Action selectAction(Board board, int playerId) {

        // 1. Récupérer tous les coups valides disponibles
        List<Action> actions = board.getAvailableActions();

        // 2. Si aucun coup n'est possible, la partie est terminée
        if (actions.isEmpty()) {
            return null;
        }

        // 3. Parcourir les coups pour trouver un coup "intéressant"
        // (c'est-à-dire un coup qui ferme une ou plusieurs cases)
        for (Action action : actions) {

            // On travaille sur une copie pour ne pas modifier le vrai plateau
            Board copy = new Board(board);

            // On simule le coup sur la copie
            int closed = copy.apply(action, playerId);

            // Si ce coup ferme au moins une case, on le joue immédiatement
            if (closed > 0) {
                return action;
            }
        }

        // 4. Si aucun coup ne permet de fermer une case,
        // on choisit un coup valide au hasard
        return actions.get(random.nextInt(actions.size()));
    }

    /**
     * Nom de la stratégie (utilisé pour affichage et debug)
     */
    @Override
    public String getName() {
        return "Glouton";
    }
}