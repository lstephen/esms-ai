package com.ljs.ifootballmanager.ai.formation.score;

import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.formation.Formation;
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

        Double a = scoring(f, tactic).doubleValue();
        Double d = defending(f, tactic).doubleValue();

        Double avg = (a + d) / 2.0;

        Double aterm = a / (avg + 1);
        Double dterm = avg / (d + 1);

        Integer ageScore = 0;
        for (Player p : f.unsortedPlayers()) {
            ageScore += p.getAge();
        }

        Double pct = 1 + aterm - dterm;

        return ((a + d) * pct + gkQuality(f) + shotQuality(f, tactic)) / 2 - ((double) ageScore / 1000.0);
    }

    public Integer scoring(Formation f, Tactic tactic) {
        Integer shooting = skillRating(f, tactic, Rating.SHOOTING);
        Integer passing = skillRating(f, tactic, Rating.PASSING);

        return (passing + passing + shooting) / 3;
    }

    public Integer defending(Formation f, Tactic tactic) {
        return skillRating(f, tactic, Rating.TACKLING);
    }

    public Integer shotQuality(Formation f, Tactic t) {
        Long score = 0L;

        Integer shooting = skillRating(f, t, Rating.SHOOTING);

        for (Player p : f.unsortedPlayers()) {
            if (f.findRole(p) == Role.GK) {
                continue;
            }
            Double chance = (double) p.getSkillRating(f.findRole(p), t, Rating.SHOOTING) / shooting;
            score += Math.round(chance * p.getSkill(Rating.SHOOTING) / 10);
        }

        return (int) Math.round((double) score) / 10;
    }

    public Integer gkQuality(Formation f) {
        return f.players(Role.GK).iterator().next().getSkill(Rating.STOPPING) / 10;
    }

    private Integer skillRating(Formation f, Tactic t, Rating r) {
        Integer score = 0;
        for (Player p : f.unsortedPlayers()) {
            score += p.getSkillRating(f.findRole(p), t, r);
        }
        return (int) Math.round((double) score / 10.0);
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
                w.format("%7d ", skillRating(f, t, rt));
            }
            w.println();
        }

        w.format("%10s ", "SH Qual");
        for (Tactic t : tactics) {
            w.format("%7d ", shotQuality(f, t));
        }
        w.println();

        w.format("%10s ", "GK Qual");
        for (Tactic t : tactics) {
            w.format("%7d ", gkQuality(f));
        }
        w.println();

        w.format("%10s ", "Scoring");
        for (Tactic t : tactics) {
            w.format("%7d ", scoring(f, t));
        }
        w.println();

        w.format("%10s ", "Defending");
        for (Tactic t : tactics) {
            w.format("%7d ", defending(f, t));
        }
        w.println();

        w.format("%10s ", "Overall");
        for (Tactic t : tactics) {
            w.format("%7d ", score(f, t).intValue());
        }
        w.println();
    }

    public static DefaultScorer get() {
        return INSTANCE;
    }


}
