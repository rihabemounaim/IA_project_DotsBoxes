package DotsBoxes;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Classe utilitaire pour gérer le score d'un joueur au cours d'un tournoi.
 * <p>
 * Accumule les points selon un système de notation classique :
 * <ul>
 *   <li>3 points pour une victoire</li>
 *   <li>1 point pour un match nul</li>
 *   <li>0 point pour une défaite</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Enregistre aussi le nombre total de victoires, nulls et défaites
 * pour des statistiques complètes.
 * </p>
 * 
 * @version 1.0
 * @author L3 UPS
 */
class Score {
    
    /** Points totaux accumulés. */
      final AtomicInteger points = new AtomicInteger(0);
    
    /** Nombre total de victoires. */
    final AtomicInteger wins = new AtomicInteger(0);
    
    /** Nombre total de matchs nuls. */
    final AtomicInteger draws = new AtomicInteger(0);
    
    /** Nombre total de défaites. */
    final AtomicInteger losses = new AtomicInteger(0);

    /**
     * Enregistre une victoire (ajoute 3 points et incrémente le compteur).
     */
    void win() { 
        points.addAndGet(3); 
        wins.incrementAndGet(); 
    }
    
    /**
     * Enregistre un match nul (ajoute 1 point et incrémente le compteur).
     */
    void draw() { 
        points.incrementAndGet(); 
        draws.incrementAndGet(); 
    }
    
    /**
     * Enregistre une défaite (incrémente le compteur des pertes).
     */
    void lose() { 
        losses.incrementAndGet(); 
    }

    int getPoints() { return points.get(); }
    int getWins() { return wins.get(); }
    int getDraws() { return draws.get(); }
    int getLosses() { return losses.get(); }
}