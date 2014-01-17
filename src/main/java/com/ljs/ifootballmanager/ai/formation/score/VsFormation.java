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
public final class VsFormation implements FormationScorer {

    private final Formation vs;

    private VsFormation(Formation vs) {
        this.vs = vs;
    }

    public Double score(Formation f, Tactic t) {
        Double a = scoring(f, t);
        Double d = defending(f, t);

        Double oppA = vs.scoring();
        Double oppD = vs.defending();

        Double aterm = a / (oppD + 1);
        Double dterm = oppA / (d + 1);

        return aterm - dterm;
    }

    public Double scoring(Formation f, Tactic t) {
        Double shooting = skillRating(f, t, Rating.SHOOTING);
        Double passing = skillRating(f, t, Rating.PASSING);

        return (passing + passing + shooting) / 3;
    }

    public Double defending(Formation f, Tactic t) {
        return skillRating(f, t, Rating.TACKLING);
    }

    public Double gkQuality(Formation f) {
        return DefaultScorer.get().gkQuality(f);
    }
    
    public Double shotQuality(Formation f, Tactic t) {
        return DefaultScorer.get().shotQuality(f, t);
    }

    private Double skillRating(Formation f, Tactic t, Rating r) {
        Double score = 0.0;
        for (Player p : f.unsortedPlayers()) {
            score += p.getSkillRating(f.findRole(p), t, r, vs.getTactic());
        }
        return score;
    }

    public void print(Formation f, PrintWriter w) {
        DefaultScorer.get().print(f, w);
    }

    public static VsFormation create(Formation f) {
        return new VsFormation(f);
    }

}
