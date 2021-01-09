package com.ljs.ifootballmanager.ai.value.impl;

import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.value.Value;

/** @author lstephen */
public class EslValue {

  private static final int PEAK_END = 30;
  private static final int PEAK_START = 22;

  public static EslValue create() {
    return new EslValue();
  }

  public Value getAgeValue() {
    return p -> (double) Math.max(0, Math.min(PEAK_END - p.getAge(), (PEAK_END - PEAK_START)));
  }
}
