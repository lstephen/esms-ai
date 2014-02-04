package com.ljs.ifootballmanager.ai.formation.score;

import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.formation.Formation;
import com.ljs.ifootballmanager.ai.league.League;
import com.ljs.ifootballmanager.ai.player.Squad;
import java.io.PrintWriter;

/**
 *
 * @author lstephen
 */
public class YouthTeamScorer implements FormationScorer {

    private final FormationScorer now;

    private final FormationScorer future;

    private YouthTeamScorer(FormationScorer now, FormationScorer future) {
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

    public static YouthTeamScorer create(League league, Squad squad) {
        return new YouthTeamScorer(
            league.getYouthSkillsCap().isPresent()
                ? CappedScorer.create(squad, league.getYouthSkillsCap().get())
                : DefaultScorer.get(),
            AtPotentialScorer.create(league.getPlayerPotential()));
    }

}
