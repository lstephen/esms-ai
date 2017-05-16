package com.ljs.ifootballmanager.ai.value;

import com.google.common.collect.Sets;
import com.ljs.ifootballmanager.ai.player.Player;
import java.util.Set;

/** @author lstephen */
public final class Potentials {

  private Potentials() {}

  public static Iterable<Player> atPotential(Potential potential, Iterable<Player> ps) {
    Set<Player> result = Sets.newHashSet();

    for (Player p : ps) {
      result.add(potential.atPotential(p));
    }

    return result;
  }
}
