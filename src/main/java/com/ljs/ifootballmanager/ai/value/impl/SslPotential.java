package com.ljs.ifootballmanager.ai.value.impl;

import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.player.SquadHolder;
import com.ljs.ifootballmanager.ai.value.Potential;

/** @author lstephen */
public class SslPotential implements Potential {

  private final Double pctOfSeason;

  private SslPotential(Double pctOfSeason) {
    this.pctOfSeason = pctOfSeason;
  }

  public Player atPotential(Player p) {
    Double yearsToDevelop = Math.max(0, 20 - pctOfSeason - p.getAge());

    p = p.withAbilityAdded(p.getPrimarySkill(), Math.round(1500 * yearsToDevelop));
    p = p.withAbilityAdded(p.getSecondarySkill(), Math.round(900 * yearsToDevelop));
    p = p.withAbilityAdded(p.getTertiarySkill(), Math.round(600 * yearsToDevelop));

    if (p.getAge() == 20 || p.getAge() == 21) {
      Double factor = 22 - pctOfSeason - p.getAge();
      p = p.withAbilityAdded(p.getPrimarySkill(), Math.round(750 * factor));
    }

    return p;
  }

  public static SslPotential create() {

    return new SslPotential(SquadHolder.get().getGames() / 25.0);
  }
}
