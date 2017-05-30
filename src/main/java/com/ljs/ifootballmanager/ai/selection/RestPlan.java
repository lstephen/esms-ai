package com.ljs.ifootballmanager.ai.selection;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import com.ljs.ifootballmanager.ai.formation.Formation;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.player.Squad;
import com.ljs.ifootballmanager.ai.report.Report;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class RestPlan implements Report {

  private final Squad squad;

  private final Formation formation;

  private final Bench bench;

  private RestPlan(Squad s, Formation f, Bench b) {
    this.squad = s;
    this.formation = f;
    this.bench = b;
  }

  private Optional<Player> getPlayerToBeRested() {
    List<Player> ps = formation.players();
    Collections.shuffle(ps);

    Player lowestFitness =
        Ordering.natural()
            .onResultOf((Player p) -> squad.findPlayer(p.getName()).getFitness())
            .min(ps);

    return squad.findPlayer(lowestFitness.getName()).isFullFitness()
        ? Optional.empty()
        : Optional.of(lowestFitness);
  }

  private Player getSubstituteFor(Player p) {
    return bench.findSubstitute(formation.findRole(p));
  }

  public void print(PrintWriter w) {
    getPlayerToBeRested()
        .ifPresent(
            out -> {
              Player in = getSubstituteFor(out);
              w.format(
                  "SUB %s %s %s IF SCORE <= -3%n",
                  out.getName(), in.getName(), formation.findRole(out));
              w.format(
                  "SUB %s %s %s IF SCORE >= 3%n",
                  out.getName(), in.getName(), formation.findRole(out));
            });
  }

  public void print(PrintWriter w, Function<Player, Integer> playerIdx) {
    getPlayerToBeRested()
        .ifPresent(
            out -> {
              Player in = getSubstituteFor(out);
              w.format(
                  "SUB %s %s %s IF SCORE <= -3%n",
                  playerIdx.apply(out), playerIdx.apply(in), formation.findRole(out));
              w.format(
                  "SUB %s %s %s IF SCORE >= 3%n",
                  playerIdx.apply(out), playerIdx.apply(in), formation.findRole(out));
            });
  }

  public static RestPlan create(Squad s, Formation f, Bench b) {
    return new RestPlan(s, f, b);
  }
}
