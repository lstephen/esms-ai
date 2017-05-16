package com.ljs.ifootballmanager.ai;

/** @author lstephen */
public enum Tactic {
  NORMAL('N'),
  DEFENSIVE('D'),
  ATTACKING('A'),
  COUNTER_ATTACK('C'),
  LONG_BALL('L'),
  PASSING('P'),
  EUROPEAN('E');

  private final Character code;

  Tactic(Character code) {
    this.code = code;
  }

  public Character getCode() {
    return code;
  }
}
