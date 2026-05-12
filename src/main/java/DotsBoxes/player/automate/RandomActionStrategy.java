package DotsBoxes.player.automate;

import java.util.List;
import java.util.Random;

import DotsBoxes.board.Action;
import DotsBoxes.board.Board;
import DotsBoxes.player.ActionStrategy;

/**
 * Version etudiants.
 *
 * TODO:
 * - Construire la liste des actions valides.
 * - Tirer une action aleatoirement.
 * - Retourner null si aucun coup n'est disponible.
 */
public class RandomActionStrategy implements ActionStrategy {

    @Override
    public Action selectAction(Board board, int playerId) {
        List<Action>  actions = board.getAvailableActions() ;
        if (actions.isEmpty()){
            return null ;
        }
        
         Random random = new Random();
        return actions.get(random.nextInt(actions.size()));
    }

    @Override
    public String getName() {
        return "Random";
    }
}
