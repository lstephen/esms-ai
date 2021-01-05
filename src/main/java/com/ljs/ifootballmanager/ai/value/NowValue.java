package com.ljs.ifootballmanager.ai.value;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Ordering;
import com.ljs.ifootballmanager.ai.Context;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.rating.Rating;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.function.Function;
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

  public Context getContext() {
    return ctx;
  }

  public Player getPlayer() {
    return player;
  }

  public Role getRole() {
    return role;
  }

  public Tactic getTactic() {
    return tactic;
  }

  private Double getAbility() {
    return player.evaluate(role, tactic).getRating();
  }

  public Double getVsReplacement() {
    var rl = ctx.getReplacementLevel();

    var shooting =
        player.getSkill(Rating.SHOOTING) - rl.getReplacementLevel(Rating.SHOOTING, tactic);
    var keeping =
        player.getSkill(Rating.STOPPING) - rl.getReplacementLevel(Rating.STOPPING, tactic);

    return getAbility()
        - ctx.getReplacementLevel().getReplacementLevel(role, tactic)
        + Math.max(shooting, 0)
        + Math.max(keeping, 0);
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
    return byScore().max(all(ctx, p));
  }

  public static NowValue bestVsReplacement(Context ctx, Player p, Tactic t) {
    return byVsReplacement().compound(byScore()).max(all(ctx, p, t));
  }

  public static NowValue bestByWeighted(Context ctx, Player p) {
    var values = allBestByTactic(ctx, p);
    var weightings = ctx.getFirstXI().getTacticWeightings();

    var tactic =
        Arrays.stream(Tactic.values())
            .max(Comparator.comparing(t -> values.get(t).getScore() * weightings.get(t)))
            .orElseThrow(IllegalStateException::new);

    return values.get(tactic);
  }

  public static ImmutableMap<Tactic, NowValue> allBestByTactic(Context ctx, Player p) {
    return Arrays.stream(Tactic.values())
        .map(t -> byScore().max(all(ctx, p, t)))
        .collect(ImmutableMap.toImmutableMap(NowValue::getTactic, Function.identity()));
  }

  public static Double weightedScore(Context ctx, Player p) {
    var values = NowValue.allBestByTactic(ctx, p);
    var weightings = ctx.getFirstXI().getTacticWeightings();

    return Arrays.stream(Tactic.values())
            .mapToDouble(t -> values.get(t).getScore() * weightings.get(t))
            .sum()
        / weightings.values().stream().mapToLong(v -> v).sum();
  }

  private static Ordering<NowValue> byScore() {
    return Ordering.natural().onResultOf(NowValue::getScore);
  }

  private static Ordering<NowValue> byVsReplacement() {
    return Ordering.natural().onResultOf(NowValue::getVsReplacement);
  }
}
