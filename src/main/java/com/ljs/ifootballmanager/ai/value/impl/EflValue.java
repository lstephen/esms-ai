package com.ljs.ifootballmanager.ai.value.impl;

import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.value.Value;

/** @author lstephen */
public class EflValue {

  private Integer getYearsLeft(Player p) {
    return getRetirementAge(p) - p.getAge();
  }

  private Integer getRetirementAge(Player p) {
    switch (p.getPrimarySkill()) {
      case STOPPING:
        return 38;
      case TACKLING:
        return 37;
      case PASSING:
      case SHOOTING:
        return 36;
      default:
        throw new IllegalArgumentException(p.getPrimarySkill().toString());
    }
  }

  public Value getAgeValue() {
    return p -> (double) getYearsLeft(p) / 2;
  }

  public static EflValue create() {
    return new EflValue();
  }
}
