package com.ljs.ifootballmanager.ai.value;

import com.google.common.collect.Ordering;
import com.ljs.ifootballmanager.ai.Context;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.player.Player;
import java.util.Collection;
import java.util.ArrayList;

public final class NowValue {

  private Context ctx;

  private Player player;

  private Role role;

  private Tactic tactic;

  private NowValue(Context ctx, Player p, Role r, Tactic t) {
    this.ctx = ctx;
    this.player = p;
    this.role = r;
    this.tactic = t;
  }

  private Double getAbility() {
    return player.evaluate(role, tactic).getRating();
  }

  private Double getVsReplacement() {
    return getAbility() - ctx.getReplacementLevel().getReplacementLevel(role, tactic);
  }

  public Double getScore() {
    return getAbility() + getVsReplacement();
  }

  public String format() {
    return String.format("%2s %s : %3d %3d : %3d",
        role,
        tactic.getCode(),
        Math.round(getAbility()),
        Math.round(getVsReplacement()),
        Math.round(getScore()));
  }

  private static Iterable<NowValue> all(Context ctx, Player p) {
    Collection<NowValue> vs = new ArrayList<>();

    for (Role r : Role.values()) {
      for (Tactic t : Tactic.values()) {
        vs.add(new NowValue(ctx, p, r, t));
      }
    }

    return vs;
  }


  public static NowValue best(Context ctx, Player p) {
    return Ordering.natural().onResultOf(NowValue::getScore).max(all(ctx, p));
  }

  public static NowValue bestVsReplacement(Context ctx, Player p) {
    return Ordering.natural().onResultOf(NowValue::getVsReplacement).max(all(ctx, p));
  }
}

