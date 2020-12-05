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
    NowValue now = NowValue.bestVsReplacement(ctx, p);

    Double ageValue = ctx.getLeague().getAgeValue().getValue(p);

    return now.getScore() + OverallValue.getAbilities(now) + OverallValue.getVsAge(now) + ageValue;
    // return Math.max(now, future) + ageValue;
  }

  public static double getVsAge(NowValue now) {
    return now.getScore()
        - now.getContext().getSkillByAge().getAverageForComparison(now.getPlayer().getAge());
  }

  public static double getAbilities(NowValue now) {
    return Evaluator.create(now.getPlayer().getAbilities())
            .evaluate(now.getRole(), now.getTactic())
            .getRating()
        / 1000.0;
  }

  public static OverallValue create(Context ctx) {
    return new OverallValue(ctx);
  }
}
