package com.ljs.ifootballmanager.ai.report;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.ljs.ifootballmanager.ai.formation.Formation;
import com.ljs.ifootballmanager.ai.league.League;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.selection.Bench;
import com.ljs.ifootballmanager.ai.selection.ChangePlan;
import java.io.PrintWriter;

/**
 *
 * @author lstephen
 */
public class TeamSheet implements Report {

    private final League league;

    private final Formation formation;

    private final ChangePlan changePlan;

    private final Bench bench;

    private TeamSheet(League league, Formation formation, ChangePlan cp, Bench bench) {
        this.league = league;
        this.formation = formation;
        this.changePlan = cp;
        this.bench = bench;
    }

    private ImmutableList<Player> players() {
        return ImmutableList.copyOf(Iterables.concat(formation.players(), bench.players()));
    }

    private Function<Player, Integer> getPlayerIndex() {
        return new Function<Player, Integer>() {
            public Integer apply(Player p) {
                return players().indexOf(p) + 1;
            }
        };
    }

    public void print(PrintWriter w) {
        w.println(league.getTeam());
        w.println(formation.getTactic().getCode());
        w.println();

        formation.printPlayers(w);
        w.println();
        bench.printPlayers(w);
        w.println();
        w.format("PK: %s%n", formation.getPenaltyKicker().getName());
        w.println();
        bench.printInjuryTactics(w, getPlayerIndex());
        w.format("TACTIC %s IF SCORE = 0%n", formation.getTactic().getCode());
        w.format("TACTIC %s IF SCORE < 0%n", changePlan.getBestScoringTactic().getCode());
        w.format("TACTIC %s IF SCORE > 0%n", changePlan.getBestDefensiveTactic().getCode());
        changePlan.print(w, getPlayerIndex());

    }


    public static TeamSheet create(League league, Formation formation, ChangePlan cp, Bench bench) {
        return new TeamSheet(league, formation, cp, bench);
    }

}
