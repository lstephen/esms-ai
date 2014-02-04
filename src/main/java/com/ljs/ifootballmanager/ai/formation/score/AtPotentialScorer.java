package com.ljs.ifootballmanager.ai.formation.score;

import com.google.common.collect.Sets;
import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.formation.Formation;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.value.Potential;
import java.io.PrintWriter;
import java.util.Set;

/**
 *
 * @author lstephen
 */
public class AtPotentialScorer implements FormationScorer {

    private Potential potential;

    private AtPotentialScorer(Potential potential) {
        this.potential = potential;
    }

    public Double score(Formation f, Tactic t) {
        return DefaultScorer.get().score(atPotential(f), t);
    }

    public Double scoring(Formation f, Tactic t) {
        return DefaultScorer.get().scoring(atPotential(f), t);
    }

    public Double defending(Formation f, Tactic t) {
        return DefaultScorer.get().defending(atPotential(f), t);
    }

    public void print(Formation f, PrintWriter w) { }

    private Formation atPotential(Formation f) {
        Set<Player> updated = Sets.newHashSet();
        for (Player p : f.unsortedPlayers()) {
            updated.add(potential.atPotential(p));
        }
        return f.withUpdatedPlayers(updated);
    }

    public static AtPotentialScorer create(Potential potential) {
        return new AtPotentialScorer(potential);
    }

}
