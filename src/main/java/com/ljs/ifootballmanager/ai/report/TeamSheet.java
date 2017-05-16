package com.ljs.ifootballmanager.ai.report;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.ljs.ifootballmanager.ai.formation.Formation;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.selection.Bench;
import com.ljs.ifootballmanager.ai.selection.ChangePlan;
import java.io.PrintWriter;

/** @author lstephen */
public class TeamSheet implements Report {

  private final String team;

  private final Formation formation;

  private final ChangePlan changePlan;

  private final Bench bench;

  private TeamSheet(String team, Formation formation, ChangePlan cp, Bench bench) {
    this.team = team;
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
    w.println(team);
    w.println(formation.getTactic().getCode());
    w.println();

    formation.printPlayers(w);
    w.println();
    bench.printPlayers(w);
    w.println();
    w.format("PK: %s%n", formation.getPenaltyKicker().getName());
    w.println("AGG 20");
    w.println();
    changePlan.print(w, getPlayerIndex());
  }

  public static TeamSheet create(String team, Formation formation, ChangePlan cp, Bench bench) {
    return new TeamSheet(team, formation, cp, bench);
  }
}
