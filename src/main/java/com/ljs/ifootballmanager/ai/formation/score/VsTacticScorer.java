package com.ljs.ifootballmanager.ai.formation.score;

import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.formation.Formation;
import com.ljs.ifootballmanager.ai.math.Maths;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.rating.Rating;
import java.io.PrintWriter;

/** @author lstephen */
public final class VsTacticScorer implements FormationScorer {

  private final Tactic vs;

  private VsTacticScorer(Tactic vs) {
    this.vs = vs;
  }

  public double score(Formation f, Tactic tactic) {

    double a = scoring(f, tactic);
    double d = defending(f, tactic);

    double avg = (a + d) / 2;

    double aterm = a / (avg + 1);
    double dterm = avg / (d + 1);

    double pct = 1 + aterm - dterm;

    return (a + d) * pct + gkBonus(f, tactic) + shootingBonus(f, tactic);
  }

  public double scoring(Formation f, Tactic tactic) {
    double shooting = skillRating(f, tactic, Rating.SHOOTING);
    double passing = skillRating(f, tactic, Rating.PASSING);

    return (passing + passing + shooting) / 3;
  }

  public double defending(Formation f, Tactic tactic) {
    return skillRating(f, tactic, Rating.TACKLING);
  }

  public double shootingBonus(Formation f, Tactic tactic) {
    double a = scoring(f, tactic);
    double d = defending(f, tactic);

    double avg = (a + d) / 2;

    double base = (9 * shotQuality(f, tactic) + cornerShotQuality(f)) / 10;

    return a / avg * base;
  }

  public double gkBonus(Formation f, Tactic t) {
    double a = scoring(f, t);
    double d = defending(f, t);

    double avg = (a + d) / 2;

    return avg / d * gkQuality(f);
  }

  public double shotQuality(Formation f, Tactic t) {
    double score = 0.0;

    double shooting = skillRating(f, t, Rating.SHOOTING);

    for (Player p : f.unsortedPlayers()) {
      if (f.findRole(p) == Role.GK) {
        continue;
      }
      double chance = p.getSkillRating(f.findRole(p), t, Rating.SHOOTING, vs) / shooting;

      score += (chance * p.getSkill(Rating.SHOOTING));
    }

    return score;
  }

  private double cornerShotQuality(Formation f) {
    double total = 0.0;

    for (Player p : f.unsortedPlayers()) {
      if (f.findRole(p) == Role.GK) {
        continue;
      }
      total += p.getSkill(Rating.SHOOTING) + 10;
    }

    double score = 0.0;
    for (Player p : f.unsortedPlayers()) {
      if (f.findRole(p) == Role.GK) {
        continue;
      }
      double chance = (p.getSkill(Rating.SHOOTING) + 10) / total;

      score += chance * p.getSkill(Rating.SHOOTING);
    }

    return score;
  }

  public double gkQuality(Formation f) {
    return f.players(Role.GK).iterator().next().getSkill(Rating.STOPPING);
  }

  private double teamAggression(Formation f) {
    double sum = aggression(f) * 11;

    return 0.75 * sum + 0.25 * 10 * sum;
  }

  private double aggression(Formation f) {
    double agg = 0.0;

    for (Player p : f.unsortedPlayers()) {
      agg += p.getAggression();
    }

    return agg / 11;
  }

  public double skillRating(Formation f, Tactic t, Rating r) {
    double score = 0.0;
    for (Player p : f.unsortedPlayers()) {
      score += p.getSkillRating(f.findRole(p), t, r, vs);
    }
    return score;
  }

  private double outfieldPlayerSkillTotal(Formation f, Rating r) {
    double score = 0.0;
    for (Player p : f.unsortedPlayers()) {
      if (f.findRole(p) == Role.GK) {
        continue;
      }
      score += p.getSkill(r);
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

    w.format("%10s ", "C SH Qual");
    for (Tactic t : tactics) {
      w.format("%7d ", Maths.round(cornerShotQuality(f)));
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

  public static VsTacticScorer create(Tactic vs) {
    return new VsTacticScorer(vs);
  }
}
