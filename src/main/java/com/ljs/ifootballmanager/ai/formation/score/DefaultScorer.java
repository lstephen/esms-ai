package com.ljs.ifootballmanager.ai.formation.score;

import com.google.common.collect.ImmutableMap;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.formation.Formation;
import com.ljs.ifootballmanager.ai.league.LeagueHolder;
import com.ljs.ifootballmanager.ai.math.Maths;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.rating.Rating;
import java.io.PrintWriter;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

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

        return (a + d) * pct + gkBonus(f, tactic) + shootingBonus(f, tactic);
    }

    public Double scoring(Formation f, Tactic tactic) {
        Double shooting = skillRating(f, tactic, Rating.SHOOTING);
        Double passing = skillRating(f, tactic, Rating.PASSING);

        return (passing + passing + shooting) / 3;
    }

    public Double defending(Formation f, Tactic tactic) {
        return skillRating(f, tactic, Rating.TACKLING);
    }

    public Double shootingBonus(Formation f, Tactic tactic) {
        Double a = scoring(f, tactic);
        Double d = defending(f, tactic);

        Double avg = (a + d) / 2;

        DescriptiveStatistics shots = buildShotQualityStatistics(f, tactic);

        Double base = (shots.getMean() + shots.getPercentile(50) + shots.getMax()) / 3.0;

        return avg < 1.0 ? base : (a/avg * base);
    }

    public Double gkBonus(Formation f, Tactic t) {
        Double a = scoring(f, t);
        Double d = defending(f, t);

        Double avg = (a + d) / 2;

        Double factor = Math.min(2.0, d < 1.0 ? 1.0 : (avg / d));

        return factor * gkQuality(f);
    }

    public Double shotQuality(Formation f, Tactic t) {
        Double score = 0.0;

        ImmutableMap<Player, Double> chances = shootingChances(f, t);

        for (Player p : f.unsortedPlayers()) {
            score += (chances.get(p) * p.getSkill(Rating.SHOOTING));
        }

        return score;
    }

    private ImmutableMap<Player, Double> shootingChances(Formation f, Tactic t) {

        Double total = 0.0;

        for (Player p : f.unsortedPlayers()) {
            if (f.findRole(p) == Role.GK) {
                continue;
            }
            total += p.getSkillRating(f.findRole(p), t, Rating.SHOOTING);
        }

        ImmutableMap.Builder<Player, Double> chances = ImmutableMap.builder();

        for(Player p : f.unsortedPlayers()) {
            if (f.findRole(p) == Role.GK) {
                chances.put(p, 0.0);
            } else {
                chances.put(p, p.getSkillRating(f.findRole(p), t, Rating.SHOOTING) / total);
            }
        }

        return chances.build();
    }

    private DescriptiveStatistics buildShotQualityStatistics(Formation f, Tactic t) {
        DescriptiveStatistics shots = new DescriptiveStatistics();

        ImmutableMap<Player, Double> chances = shootingChances(f, t);

        for (Player p : f.unsortedPlayers()) {
            for (int i = 0; i < chances.get(p) * 1000; i++) {
                shots.addValue(p.getSkill(Rating.SHOOTING));
            }
        }

        return shots;
    }


    private Double cornerShotQuality(Formation f) {
        Double total = 0.0;

        for (Player p : f.unsortedPlayers()) {
            if (f.findRole(p) == Role.GK) {
                continue;
            }
            total += p.getSkill(Rating.SHOOTING) + 10;
        }

        Double score = 0.0;
        for (Player p : f.unsortedPlayers()) {
            if (f.findRole(p) == Role.GK) {
                continue;
            }
            Double chance = (p.getSkill(Rating.SHOOTING) + 10) / total;

            score += chance * p.getSkill(Rating.SHOOTING);
        }

        return score;
    }

    public Double gkQuality(Formation f) {
        return f.players(Role.GK).iterator().next().getSkill(Rating.STOPPING);
    }

    private Double aggression(Formation f) {
        Double agg = 0.0;

        for (Player p : f.unsortedPlayers()) {
            agg += p.getAggression();
        }

        return agg / 11;
    }

    private Double skillRating(Formation f, Tactic t, Rating r) {
        Double score = 0.0;
        for (Player p : f.unsortedPlayers()) {
            if (LeagueHolder.get().getPlayerValidator().isAllowedInRole(p.getRatings(), f.findRole(p))) {
                score += p.getSkillRating(f.findRole(p), t, r);
            }
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

        w.format("%10s ", "SH Mean");
        for (Tactic t : tactics) {
            DescriptiveStatistics shots = buildShotQualityStatistics(f, t);
            w.format("%7d ", Maths.round(shots.getMean()));
        }
        w.println();

        w.format("%10s ", "SH Median");
        for (Tactic t : tactics) {
            DescriptiveStatistics shots = buildShotQualityStatistics(f, t);
            w.format("%7d ", Maths.round(shots.getPercentile(50)));
        }
        w.println();

        w.format("%10s ", "SH Max");
        for (Tactic t : tactics) {
            DescriptiveStatistics shots = buildShotQualityStatistics(f, t);
            w.format("%7d ", Maths.round(shots.getMax()));
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

        w.format("%10s ", "Agg");
        for (Tactic t : tactics) {
            w.format("%7d ", Maths.round(aggression(f)));
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
