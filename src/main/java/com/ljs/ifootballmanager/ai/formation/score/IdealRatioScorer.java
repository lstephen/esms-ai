package com.ljs.ifootballmanager.ai.formation.score;

import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.formation.Formation;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.rating.Rating;
import java.io.PrintWriter;

/**
 *
 * @author lstephen
 */
public class IdealRatioScorer implements FormationScorer {

    private Double scoring;

    private Double defending;

    private Tactic vs;

    private IdealRatioScorer(Double scoring, Double defending, Tactic vs) { }

    public Double score(Formation f, Tactic tactic) {

        Double a = scoring(f, tactic);
        Double d = defending(f, tactic);

        Double avg = (a + d) / 2;

        Double aterm = a / (avg + 1);
        Double dterm = avg / (d + 1);

        Double total = scoring + defending;

        Double aRatio = defending / total;
        Double dRatio = scoring / total;

        Double pct = 1 + aRatio * aterm - dRatio * dterm;

        System.out.println(String.format("s:%.3d d:%.3d p:%.3d", a, d, pct));

        return (a + d) * pct + gkQuality(f) + shotQuality(f, tactic);
    }

    public Double scoring(Formation f, Tactic tactic) {
        return DefaultScorer.get().score(f, tactic);
    }

    public Double defending(Formation f, Tactic tactic) {
        return DefaultScorer.get().defending(f, tactic);
    }

    public Double shotQuality(Formation f, Tactic t) {
        return DefaultScorer.get().shotQuality(f, t);
    }

    public Double gkQuality(Formation f) {
        return DefaultScorer.get().gkQuality(f);
    }

    private Double skillRating(Formation f, Tactic t, Rating r) {
        Double score = 0.0;
        for (Player p : f.unsortedPlayers()) {
            score += p.getSkillRating(f.findRole(p), t, r, vs);
        }
        return score;
    }

    public void print(Formation f, PrintWriter w) {
        DefaultScorer.get().print(f, w);
    }

    private static IdealRatioScorer create(Double scoring, Double defending, Tactic vs) {
        return new IdealRatioScorer(scoring, defending, vs);
    }

    public static IdealRatioScorer create(Formation f, Formation vs) {
        return create(f.scoring(), f.defending(), vs.getTactic());
    }

}
