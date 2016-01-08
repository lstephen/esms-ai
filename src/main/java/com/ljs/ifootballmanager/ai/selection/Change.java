package com.ljs.ifootballmanager.ai.selection;

import com.ljs.ifootballmanager.ai.formation.Formation;
import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import com.ljs.ifootballmanager.ai.player.Player;
import java.io.PrintWriter;

/**
 *
 * @author lstephen
 */
public interface Change {

  Integer getMinute();

  Formation apply(Formation f, Integer minute);

  Boolean isValid(ChangePlan cp);

  void print(PrintWriter w);

  void print(PrintWriter w, Function<Player, Integer> playerIdx);

  final class Meta {
    private Meta() { }

    public static Ordering<Change> byMinute() {
      return Ordering.natural().onResultOf(Change::getMinute);
    }
  }

}
