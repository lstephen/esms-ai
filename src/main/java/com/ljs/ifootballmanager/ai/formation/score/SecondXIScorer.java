package com.ljs.ifootballmanager.ai.formation.score;

import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.formation.Formation;
import com.ljs.ifootballmanager.ai.league.League;
import java.io.PrintWriter;

/**
 *
 * @author lstephen
 */
public class SecondXIScorer implements FormationScorer {

    private final FormationScorer now;

    private final FormationScorer future;

    private SecondXIScorer(FormationScorer now, FormationScorer future) {
        this.now = now;
        this.future = future;
    }

    public Double score(Formation f, Tactic t) {
        return now.score(f, t) + future.score(f, t);
    }

    public Double scoring(Formation f, Tactic t) {
        return now.scoring(f, t);
    }

    public Double defending(Formation f, Tactic t) {
        return now.defending(f, t);
    }

    public void print(Formation f, PrintWriter w) {
        DefaultScorer.get().print(f, w);
    }

    public static SecondXIScorer create(League league) {
        return new SecondXIScorer(
            DefaultScorer.get(),
            AtPotentialScorer.create(league.getPlayerPotential()));
    }

}
