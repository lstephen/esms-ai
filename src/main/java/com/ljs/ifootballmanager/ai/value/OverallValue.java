package com.ljs.ifootballmanager.ai.value;

import com.ljs.ifootballmanager.ai.Context;
import com.ljs.ifootballmanager.ai.player.Player;

/** @author lstephen */
public final class OverallValue implements Value {

  private final Context ctx;

  private OverallValue(Context ctx) {
    this.ctx = ctx;
  }

  public Double getValue(Player p) {
    var score = NowValue.weightedScore(ctx, p);

    Double ageValue = ctx.getLeague().getAgeValue().getValue(p);

    return score + OverallValue.getAbilities(ctx, p) + OverallValue.getVsAge(ctx, p) + ageValue;
  }

  public static double getVsAge(Context ctx, Player p) {
    return NowValue.weightedScore(ctx, p) - ctx.getSkillByAge().getAverageForComparison(p.getAge());
  }

  public static double getAbilities(Context ctx, Player p) {
    NowValue now = NowValue.bestByWeighted(ctx, p);

    return Evaluator.create(now.getPlayer().getAbilities())
            .evaluate(now.getRole(), now.getTactic())
            .getRating()
        / 1000.0;
  }

  public static OverallValue create(Context ctx) {
    return new OverallValue(ctx);
  }
}
