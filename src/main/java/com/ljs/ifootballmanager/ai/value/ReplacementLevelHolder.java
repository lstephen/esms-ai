package com.ljs.ifootballmanager.ai.value;

/** @author lstephen */
public final class ReplacementLevelHolder {

  private static ReplacementLevel rl;

  private ReplacementLevelHolder() {}

  public static ReplacementLevel get() {
    return rl;
  }

  public static void set(ReplacementLevel rl) {
    ReplacementLevelHolder.rl = rl;
  }
}
