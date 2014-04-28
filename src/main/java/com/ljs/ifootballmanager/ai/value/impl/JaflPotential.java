package com.ljs.ifootballmanager.ai.value.impl;

import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.player.SquadHolder;
import com.ljs.ifootballmanager.ai.value.Potential;

/**
 *
 * @author lstephen
 */
public class JaflPotential implements Potential {

    private final Double pctOfSeason;

    private JaflPotential(Double pctOfSeason) {
        this.pctOfSeason = pctOfSeason;
    }

    public Player atPotential(Player p) {
        Double yearsToDevelop = Math.max(0, 22 - pctOfSeason - p.getAge());

        p = p.withAbilityAdded(p.getPrimarySkill(), Math.round(1500 * yearsToDevelop));
        p = p.withAbilityAdded(p.getSecondarySkill(), Math.round(1100 * yearsToDevelop));
        p = p.withAbilityAdded(p.getTertiarySkill(), Math.round(600 * yearsToDevelop));

        return p;
    }

    public static JaflPotential create() {
        return new JaflPotential(SquadHolder.get().getGames() / 25.0);
    }

}
