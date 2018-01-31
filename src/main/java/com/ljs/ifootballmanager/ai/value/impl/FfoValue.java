package com.ljs.ifootballmanager.ai.value.impl;

import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.value.Value;

/** @author lstephen */
public class FfoValue implements Value {

  private FfoValue() {}

  public Double getValue(Player p) {
    Double base = getPotential(p);

    return base * getAgingFactor(p.getAge());
  }

  public Value getAgeValue() {
    return p -> Math.pow(Math.max(0, p.getAge() - 29), 2) * 2 / 10 + getPeakYearsValue(p);
  }

  private Double getAgingFactor(Integer age) {
    return 1.0 - ((double) Math.pow(Math.max(0, age - 29), 2) * 2 / 100);
  }

  private Integer getPeakYearsValue(Player p) {
    return Math.min(29 - p.getAge(), 7);
  }

  public static EslValue create() {
    return new EslValue();
  }

  private Double getPotential(Player p) {
    return EslPotential.create().atPotential(p).getOverall().getRating();
  }
}
