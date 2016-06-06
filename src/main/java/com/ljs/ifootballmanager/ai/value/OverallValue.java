package com.ljs.ifootballmanager.ai.value;

import java.util.stream.Collectors;

import com.ljs.ifootballmanager.ai.Context;
import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.player.Player;

/**
 *
 * @author lstephen
 */
public final class OverallValue implements Value {

    private final Context ctx;

    private OverallValue(Context ctx) {
      this.ctx = ctx;
    }

    public Double getValue(Player p) {
      return getBest(p).getRating();
    }

    public RatingInRole getBest(Player p) {
      return RatingInRole
        .byRating()
        .max(ctx
          .getFirstXI()
          .getTactics()
          .map(t -> p.getOverall(t)).collect(Collectors.toList()));
    }

    public static OverallValue create(Context ctx) {
        return new OverallValue(ctx);
    }

}
