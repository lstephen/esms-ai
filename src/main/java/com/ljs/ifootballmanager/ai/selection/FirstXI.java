package com.ljs.ifootballmanager.ai.selection;

import com.google.common.collect.ImmutableList;
import com.ljs.ifootballmanager.ai.Context;
import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.formation.Formation;
import com.ljs.ifootballmanager.ai.formation.score.DefaultScorer;
import com.ljs.ifootballmanager.ai.player.Player;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class FirstXI {

  private final Context ctx;

  private final ImmutableList<Formation> formations;

  private FirstXI(Context ctx, ImmutableList<Formation> formations) {
    this.ctx = ctx;
    this.formations = formations;
  }

  public Formation best() {
    return formations.get(0);
  }

  public Stream<Formation> getFormations() {
    return formations.stream();
  }

  public Optional<Formation> getFormation(Tactic t) {
    return getFormations().filter(f -> f.getTactic().equals(t)).findAny();
  }

  public Stream<Tactic> getTactics() {
    return getFormations().map(Formation::getTactic);
  }

  public Set<Player> getPlayers() {
    return getFormations()
        .flatMap(f -> f.players().stream())
        .distinct()
        .filter(
            p ->
                getFormations().filter(f -> f.contains(p)).count()
                    >= (ctx.getLeague().isReserveEligible(p) ? 4 : 2))
        .collect(Collectors.toSet());
  }

  public static FirstXI select(Context ctx) {
    return new FirstXI(
        ctx, Formation.select(ctx.getLeague(), ctx.getSquad().players(), DefaultScorer.get()));
  }
}
