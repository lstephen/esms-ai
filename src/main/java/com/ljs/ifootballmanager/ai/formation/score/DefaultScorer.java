package com.ljs.ifootballmanager.ai.formation.score;

import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.formation.Formation;
import com.ljs.ifootballmanager.ai.math.Maths;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.rating.Rating;
import java.io.PrintWriter;

/**
 *
 * @author lstephen
 */
public final class DefaultScorer implements FormationScorer {

    private static final DefaultScorer INSTANCE = new DefaultScorer();

    private DefaultScorer() { }

    public Double score(Formation f, Tactic tactic) {

        Double a = scoring(f, tactic);
        Double d = defending(f, tactic);

        Double avg = (a + d) / 2;

        Double aterm = a / (avg + 1);
        Double dterm = avg / (d + 1);

        Double pct = 1 + aterm - dterm;

        return (a + d) * pct + gkQuality(f) + shotQuality(f, tactic);
    }

    public Double scoring(Formation f, Tactic tactic) {
        Double shooting = skillRating(f, tactic, Rating.SHOOTING);
        Double passing = skillRating(f, tactic, Rating.PASSING);

        return (passing + passing + shooting) / 3;
    }

    public Double defending(Formation f, Tactic tactic) {
        return skillRating(f, tactic, Rating.TACKLING);
    }

    public Double shotQuality(Formation f, Tactic t) {
        Double score = 0.0;

        Double shooting = skillRating(f, t, Rating.SHOOTING);

        for (Player p : f.unsortedPlayers()) {
            if (f.findRole(p) == Role.GK) {
                continue;
            }
            Double chance = p.getSkillRating(f.findRole(p), t, Rating.SHOOTING) / shooting;

            score += (chance * p.getSkill(Rating.SHOOTING));
        }

        return score;
    }

    public Double gkQuality(Formation f) {
        return f.players(Role.GK).iterator().next().getSkill(Rating.STOPPING);
    }

    public Double skillRating(Formation f, Tactic t, Rating r) {
        Double score = 0.0;
        for (Player p : f.unsortedPlayers()) {
            score += p.getSkillRating(f.findRole(p), t, r);
        }
        return score;
    }

    public void print(Formation f, PrintWriter w) {
        Tactic[] tactics = Tactic.values();

        w.format("%10s ", "");
        for (Tactic t : tactics) {
            w.format("%7s ", t.getCode());
        }
        w.println();

        for (Rating rt : Rating.values()) {
            w.format("%10s ", rt);
            for (Tactic t : tactics) {
                w.format("%7d ", Maths.round(skillRating(f, t, rt)));
            }
            w.println();
        }

        w.format("%10s ", "SH Qual");
        for (Tactic t : tactics) {
            w.format("%7d ", Maths.round(shotQuality(f, t)));
        }
        w.println();

        w.format("%10s ", "GK Qual");
        for (Tactic t : tactics) {
            w.format("%7d ", Maths.round(gkQuality(f)));
        }
        w.println();

        w.format("%10s ", "Scoring");
        for (Tactic t : tactics) {
            w.format("%7d ", Maths.round(scoring(f, t)));
        }
        w.println();

        w.format("%10s ", "Defending");
        for (Tactic t : tactics) {
            w.format("%7d ", Maths.round(defending(f, t)));
        }
        w.println();

        w.format("%10s ", "Overall");
        for (Tactic t : tactics) {
            w.format("%7d ", Maths.round(score(f, t)));
        }
        w.println();
    }

    public static DefaultScorer get() {
        return INSTANCE;
    }


}
