package DotsBoxes.player.automate;

import java.util.List;

import DotsBoxes.board.Action;
import DotsBoxes.board.Board;
import DotsBoxes.player.ActionStrategy;

/**
 * Version etudiant.
 *
 * TODO:
 * - Parcourir le plateau.
 * - Retourner le premier segment libre.
 * - Retourner null si aucun coup n'est possible.
 */
public class FirstValidActionStrategy implements ActionStrategy {

    @Override
    public Action selectAction(Board board, int playerId) {
        List<Action> actions = board.getAvailableActions() ;
        if(actions.isEmpty()){
            return null ;
        }
        return actions.get(0) ;
    }

    @Override
    public String getName() {
        return "First valid";
    }
}
