package com.ljs.ifootballmanager.ai.report;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.ljs.ifootballmanager.ai.Context;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.WithContext;
import com.ljs.ifootballmanager.ai.math.Maths;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.rating.Rating;
import com.ljs.ifootballmanager.ai.value.NowValue;
import com.ljs.ifootballmanager.ai.value.OverallValue;
import com.ljs.ifootballmanager.ai.value.ReplacementLevel;
import com.ljs.ifootballmanager.ai.value.ReplacementLevelHolder;
import com.ljs.ifootballmanager.ai.value.Value;
import java.io.PrintWriter;

/** @author lstephen */
public class SquadReport implements Report, WithContext {

  private final Context ctx;

  private final Tactic tactic;

  private final Iterable<Player> squad;

  private Ordering<Player> ordering;

  private Value value;

  private SquadReport(Context ctx, Tactic tactic, Iterable<Player> squad) {
    this.ctx = ctx;
    this.tactic = tactic;
    this.squad = squad;

    ordering = Player.byOverall(tactic).reverse().compound(Player.byTieBreak());
    value = getLeague().getPlayerValue();
  }

  public Context getContext() {
    return ctx;
  }

  private Double getValue(Player p) {
    return OverallValue.create(ctx).getValue(p);
  }

  public SquadReport sortByValue() {
    ordering =
        Ordering.natural()
            .reverse()
            .onResultOf(
                new Function<Player, Long>() {
                  public Long apply(Player p) {
                    return Math.round(getValue(p));
                  }
                })
            .compound(Player.byTieBreak());
    return this;
  }

  public SquadReport sortByValue(Value value) {
    this.value = value;
    ordering = Player.byValue(value).reverse();
    return this;
  }

  public void print(PrintWriter w) {
    Role[] roles = Role.values();
    Tactic[] tactics = Tactic.values();

    ReplacementLevel repl = ReplacementLevelHolder.get();

    w.format("%27s | %20s | %3s %3s      || %3s ||", "", "", "vAg", "Abs", "OVR");

    getFirstXI().getTactics().forEach(t -> w.format("%3s   ", t.getCode()));

    w.println();

    for (Player p : ordering.immutableSortedCopy(squad)) {
      w.format("%-15s ", p.getName());

      String skills =
          String.format(
              "%2d/%2d/%2d",
              Math.round(p.getSkill(Rating.TACKLING)),
              Math.round(p.getSkill(Rating.PASSING)),
              Math.round(p.getSkill(Rating.SHOOTING)));

      if (p.getPrimarySkill() == Rating.STOPPING) {
        skills = String.format("   %d   ", Math.round(p.getSkill(Rating.STOPPING)));
      }

      w.format("%2d %8s ", p.getAge(), skills);

      NowValue now = NowValue.bestVsReplacement(ctx, p);

      w.format("| %20s ", now.format());
      w.format("| %3d ", Maths.round(OverallValue.getVsAge(now)));
      w.format(" %1.1f", OverallValue.getAbilities(now));

      Double ovr = value.getValue(p);
      Double vsRepl = repl.getValueVsReplacement(p);

      w.format(
          " %3d || %3d || ",
          Maths.round(getLeague().getAgeValue().getValue(p)), Maths.round(getValue(p)));

      getFirstXI()
          .getTactics()
          .forEach(
              t -> {
                w.format("%5s ", NowValue.bestVsReplacement(ctx, p, t).formatShort());
              });

      w.format(
          "%s%1s%1s ",
          p.getRosterStatus(),
          Iterables.contains(getLeague().getForcedPlay(), p.getName()) ? "F" : "",
          getLeague().isReserveEligible(p) ? "r" : "");

      w.format("%s", p.getComment());

      w.println();
    }
  }

  public static SquadReport create(Context ctx, Tactic tactic, Iterable<Player> squad) {
    return new SquadReport(ctx, tactic, squad);
  }
}
