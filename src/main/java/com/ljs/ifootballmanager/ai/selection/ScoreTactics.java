package com.ljs.ifootballmanager.ai.selection;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.formation.Formation;
import com.ljs.ifootballmanager.ai.report.Report;
import java.io.PrintWriter;
import java.util.Arrays;

/** @author lstephen */
public class ScoreTactics implements Report {

  private final Formation formation;
  private final ChangePlan changePlan;

  private ScoreTactics(Formation f, ChangePlan cp) {
    this.formation = f;
    this.changePlan = cp;
  }

  public void print(PrintWriter w) {
    printTactics(w, -2);
    printTactics(w, -1);

    printResetTactics(w);

    printTactics(w, 1);
    printTactics(w, 2);
  }

  public void printResetTactics(PrintWriter w) {
    TacticChange tc = Iterables.getFirst(changePlan.changes(TacticChange.class), null);

    Optional<Integer> tcMinute =
        tc == null ? Optional.<Integer>absent() : Optional.of(tc.getMinute());

    // Reset tactics
    if (tcMinute.isPresent()) {
      w.format(
          "TACTIC %s IF MIN <= %d SCORE = 0%n",
          formation.getTactic().getCode(), tcMinute.get() - 1);
    } else {
      w.format("TACTIC %s IF SCORE = 0%n", formation.getTactic().getCode());
    }
  }

  public void printTactics(PrintWriter w, final Integer score) {
    final Formation atEnd = changePlan.getFormationAt(90);

    Tactic best =
        Ordering.natural()
            .onResultOf(
                new Function<Tactic, Double>() {
                  public Double apply(Tactic t) {
                    return score(atEnd, t, score, 90);
                  }
                })
            .max(Arrays.asList(Tactic.values()));

    Integer from = 90;
    Formation f = changePlan.getFormationAt(from);

    while (from > 0 && score(f, f.getTactic(), score, from) <= score(f, best, score, from)) {
      from--;
      f = changePlan.getFormationAt(from);
    }

    from++;

    if (from > 1) {
      w.format("TACTIC %s IF MIN >= %d %s%n", best.getCode(), from, getScoreCondition(score));
    } else {
      w.format("TACTIC %s IF %s%n", best.getCode(), getScoreCondition(score));
    }
  }

  private String getScoreCondition(Integer score) {
    if (score == -2) {
      return "SCORE <= -2";
    } else if (score == 2) {
      return "SCORE >= 2";
    } else {
      return String.format("SCORE = %d", score);
    }
  }

  private Double score(Formation f, Tactic t, Integer score, Integer minute) {
    Double d = f.defending(t);
    Double a = f.scoring(t);
    Double s = f.score(t);
    Double m = (double) minute / 90;

    return m * Math.abs(Math.min(score, 0) * a + Math.max(score, 0) * d) + s;
  }

  public void printTacticsTable(PrintWriter w) {
    w.format("%2s: ", "M");

    for (Tactic t : Tactic.values()) {
      w.format("%21s ", t.getCode());
    }
    w.println();

    for (int i = 0; i < 90; i++) {
      Formation f = changePlan.getFormationAt(i);

      w.format("%2d:", i);
      for (Tactic t : Tactic.values()) {
        Double d = f.defending(t);
        Double a = f.scoring(t);
        Double s = f.score(t);
        Double m = (double) i / 90;
        w.format(
            "%3d/%3d||%3d||%3d/%3d ",
            Math.round(m * (d + d) + s),
            Math.round(m * d + s),
            Math.round(s),
            Math.round(m * a + s),
            Math.round(m * (a + a) + s));
      }

      w.println();
    }
  }

  public static ScoreTactics create(Formation f, ChangePlan cp) {
    return new ScoreTactics(f, cp);
  }
}
