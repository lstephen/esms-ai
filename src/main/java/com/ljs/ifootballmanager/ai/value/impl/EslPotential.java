package com.ljs.ifootballmanager.ai.value.impl;

import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.player.SquadHolder;
import com.ljs.ifootballmanager.ai.value.Potential;

/**
 *
 * @author lstephen
 */
public class EslPotential implements Potential {

    private final Double pctOfSeason;

    private EslPotential(Double pctOfSeason) {
        this.pctOfSeason = pctOfSeason;
    }

    public Player atPotential(Player p) {
        Double yearsToDevelop = Math.max(0, 20 - pctOfSeason - p.getAge());

        p = p.withAbilityAdded(p.getPrimarySkill(), Math.round(800 * yearsToDevelop));
        p = p.withAbilityAdded(p.getSecondarySkill(), Math.round(450 * yearsToDevelop));

        if (p.getAge() == 20 || p.getAge() == 21) {
            Double factor = 22 - pctOfSeason - p.getAge();
            p = p.withAbilityAdded(p.getPrimarySkill(), Math.round(500 * factor));
            p = p.withAbilityAdded(p.getSecondarySkill(), Math.round(300 * factor));
        }

        return p;
    }

    public static EslPotential create() {
        return new EslPotential(SquadHolder.get().getGames() / 25.0);
    }

}
