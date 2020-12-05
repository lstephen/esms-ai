package com.ljs.ifootballmanager.ai.value.impl;

import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.value.Value;

/** @author lstephen */
public class EslValue implements Value {

  public static EslValue create() {
    return new EslValue();
  }

  public Double getValue(Player p) {
    Double base = p.getOverall().getRating();

    return base * getAgingFactor(p.getAge()) + getPeakYearsValue(p);
  }

  public Value getAgeValue() {
    return p -> Math.pow(Math.max(0, p.getAge() - 29), 2) * 2 / 10 + getPeakYearsValue(p);
  }

  private Double getAgingFactor(Integer age) {
    return 1.0 - (Math.pow(Math.max(0, age - 29), 2) * 2 / 100);
  }

  private Integer getPeakYearsValue(Player p) {
    return Math.min(29 - p.getAge(), 7);
  }
}
