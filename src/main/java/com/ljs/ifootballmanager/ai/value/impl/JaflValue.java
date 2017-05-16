package com.ljs.ifootballmanager.ai.value.impl;

import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.value.Value;

/** @author lstephen */
public final class JaflValue implements Value {

  private JaflValue() {}

  public Double getValue(Player p) {
    Double base = getPotential(p);

    return base * getAgingFactor(p.getAge()) + getPeakYearsValue(p);
  }

  private Double getAgingFactor(Integer age) {
    return 1.0 - ((double) Math.pow(Math.max(0, age - 29), 2) * 2 / 100);
  }

  private Integer getPeakYearsValue(Player p) {
    return Math.min(29 - p.getAge(), 7);
  }

  public static JaflValue create() {
    return new JaflValue();
  }

  private Double getPotential(Player p) {
    return JaflPotential.create().atPotential(p).getOverall().getRating();
  }
}
