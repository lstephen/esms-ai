package com.ljs.ifootballmanager.ai.formation.score;

import com.google.common.collect.ImmutableList;
import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.formation.Formation;
import com.ljs.ifootballmanager.ai.math.Maths;
import com.ljs.ifootballmanager.ai.rating.Rating;
import java.io.PrintWriter;

/**
 *
 * @author lstephen
 */
public final class AverageVsAllTacticsScorer implements FormationScorer {

    private static final AverageVsAllTacticsScorer INSTANCE
        = new AverageVsAllTacticsScorer();

    private static final ImmutableList<Tactic> tactics = ImmutableList.copyOf(Tactic.values());

    private AverageVsAllTacticsScorer() { }

    public Double score(Formation f, Tactic tactic) {
        Double score = 0.0;

        for (Tactic t : tactics) {
            score += VsTacticScorer.create(t).score(f, tactic);
        }

        return score / tactics.size();
    }

    public Double scoring(Formation f, Tactic tactic) {
        Double score = 0.0;

        for (Tactic t : tactics) {
            score += VsTacticScorer.create(t).scoring(f, tactic);
        }

        return score / tactics.size();
    }

    public Double defending(Formation f, Tactic tactic) {
        Double score = 0.0;

        for (Tactic t : tactics) {
            score += VsTacticScorer.create(t).defending(f, tactic);
        }

        return score / tactics.size();
    }

    public Double shootingBonus(Formation f, Tactic tactic) {
        return DefaultScorer.get().shootingBonus(f, tactic);
    }

    public Double gkBonus(Formation f, Tactic t) {
        return DefaultScorer.get().gkBonus(f , t);
    }

    public Double shotQuality(Formation f, Tactic t) {
        return DefaultScorer.get().shotQuality(f, t);
    }

    public Double gkQuality(Formation f) {
        return DefaultScorer.get().gkQuality(f);
    }

    private Double skillRating(Formation f, Tactic tactic, Rating r) {
        Double score = 0.0;

        for (Tactic t : tactics) {
            score += VsTacticScorer.create(t).skillRating(f, tactic, r);
        }

        return score / tactics.size();
    }

    public void print(Formation f, PrintWriter w) {
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

        w.format("%10s ", "SH Bonus");
        for (Tactic t : tactics) {
            w.format("%7d ", Maths.round(shootingBonus(f, t)));
        }
        w.println();

        w.format("%10s ", "GK Qual");
        for (Tactic t : tactics) {
            w.format("%7d ", Maths.round(gkQuality(f)));
        }
        w.println();

        w.format("%10s ", "GK Bonus");
        for (Tactic t : tactics) {
            w.format("%7d ", Maths.round(gkBonus(f, t)));
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

    public static AverageVsAllTacticsScorer get() {
        return INSTANCE;
    }


}
