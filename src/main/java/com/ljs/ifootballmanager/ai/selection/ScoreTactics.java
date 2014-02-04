package com.ljs.ifootballmanager.ai.selection;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.formation.Formation;
import com.ljs.ifootballmanager.ai.report.Report;
import java.io.PrintWriter;

/**
 *
 * @author lstephen
 */
public class ScoreTactics implements Report {

    private final Formation formation;
    private final ChangePlan changePlan;

    private ScoreTactics(Formation f, ChangePlan cp) {
        this.formation = f;
        this.changePlan = cp;
    }

    public void print(PrintWriter w) {
        TacticChange tc = Iterables.getFirst(changePlan.changes(TacticChange.class), null);

        Optional<Integer> tcMinute = tc == null
            ? Optional.<Integer>absent()
            : Optional.of(tc.getMinute());

        Tactic scoring = changePlan.getBestScoringTactic();
        Tactic defending = changePlan.getBestDefensiveTactic();

        if (scoring != formation.getTactic() || defending != formation.getTactic() || tcMinute.isPresent()) {
            if (tcMinute.isPresent()) {
                w.format("TACTIC %s IF MIN <= %d SCORE = 0%n", formation.getTactic().getCode(), tcMinute.get() - 1);
            } else {
                w.format("TACTIC %s IF SCORE = 0%n", formation.getTactic().getCode());
            }
        }


        if (scoring != formation.getTactic() || tcMinute.isPresent()) {
            w.format("TACTIC %s IF SCORE = -1%n", scoring.getCode());
        }

        if (defending != formation.getTactic() || tcMinute.isPresent()) {
            w.format("TACTIC %s IF SCORE = 1%n", defending.getCode());
        }
    }



    public static ScoreTactics create(Formation f, ChangePlan cp) {
        return new ScoreTactics(f, cp);
    }

}
