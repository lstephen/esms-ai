package com.ljs.ifootballmanager.ai.formation.score;

import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.formation.Formation;
import com.ljs.ifootballmanager.ai.player.Player;
import java.io.PrintWriter;

/**
 *
 * @author lstephen
 */
public final class VsFormation implements FormationScorer {

    private final Formation vs;

    private VsFormation(Formation vs) {
        this.vs = vs;
    }

    public Double score(Formation f, Tactic t) {
        Double a = scoring(f, t).doubleValue();
        Double d = defending(f, t).doubleValue();

        Double oppA = scoring(vs, t).doubleValue();
        Double oppD = defending(vs, t).doubleValue();

        Double aterm = a / (oppD + 1);
        Double dterm = oppA / (d + 1);

        Integer ageScore = 0;
        for (Player p : f.unsortedPlayers()) {
            ageScore += p.getAge();
        }

        Double pct = 1 + aterm - dterm;

        return ((a + d) * pct + DefaultScorer.get().gkQuality(f) + DefaultScorer.get().shotQuality(f, t)) / 2 - ((double) ageScore / 1000.0);
    }

    public Integer scoring(Formation f, Tactic t) {
        return DefaultScorer.get().scoring(f, t);
    }

    public Integer defending(Formation f, Tactic t) {
        return DefaultScorer.get().defending(f, t);
    }

    public void print(Formation f, PrintWriter w) { }

    public static VsFormation create(Formation f) {
        return new VsFormation(f);
    }

}
