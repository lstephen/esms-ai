package com.ljs.ifootballmanager.ai.value.impl;

import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.value.Value;

/** @author lstephen */
public class SslValue {

  private static final int PEAK_END = 30;
  private static final int PEAK_START = 22;

  private SslValue() {}

  public static SslValue create() {
    return new SslValue();
  }

  public Value getAgeValue() {
    return p -> (double) Math.max(0, Math.min(PEAK_END - p.getAge(), (PEAK_END - PEAK_START)));
  }
}
