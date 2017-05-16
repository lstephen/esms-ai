package com.ljs.ifootballmanager.ai.value;

import com.google.common.collect.Ordering;
import com.ljs.ifootballmanager.ai.Context;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.player.Player;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

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
    return String.format(
        "%2s %s : %3d %3d : %3d",
        role,
        tactic.getCode(),
        Math.round(getAbility()),
        Math.round(getVsReplacement()),
        Math.round(getScore()));
  }

  public String formatShort() {
    return String.format("%2s%3d", role, Math.round(getVsReplacement()));
  }

  private static Collection<NowValue> all(Context ctx, Player p) {
    return Arrays.stream(Tactic.values())
        .flatMap(t -> all(ctx, p, t).stream())
        .collect(Collectors.toList());
  }

  private static Collection<NowValue> all(Context ctx, Player p, Tactic t) {
    return Arrays.stream(Role.values())
        .map(r -> new NowValue(ctx, p, r, t))
        .collect(Collectors.toList());
  }

  public static NowValue best(Context ctx, Player p) {
    return Ordering.natural().onResultOf(NowValue::getScore).max(all(ctx, p));
  }

  public static NowValue bestVsReplacement(Context ctx, Player p) {
    return Ordering.natural().onResultOf(NowValue::getVsReplacement).max(all(ctx, p));
  }

  public static NowValue bestVsReplacement(Context ctx, Player p, Tactic t) {
    return Ordering.natural().onResultOf(NowValue::getVsReplacement).max(all(ctx, p, t));
  }
}
