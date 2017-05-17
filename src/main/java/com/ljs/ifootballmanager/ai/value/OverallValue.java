package com.ljs.ifootballmanager.ai.value;

import com.ljs.ifootballmanager.ai.Context;
import com.ljs.ifootballmanager.ai.player.Player;

/** @author lstephen */
public final class OverallValue {

  private final Context ctx;

  private OverallValue(Context ctx) {
    this.ctx = ctx;
  }

  public Double getValue(Player p) {
    Double now = NowValue.bestVsReplacement(ctx, p).getScore();
    Double future =
        NowValue.bestVsReplacement(ctx, ctx.getLeague().getPlayerPotential().atPotential(p))
            .getScore();

    Double ageValue = ctx.getLeague().getAgeValue().getValue(p);

    return Math.max(now, future) + ageValue;
  }

  public static OverallValue create(Context ctx) {
    return new OverallValue(ctx);
  }
}
