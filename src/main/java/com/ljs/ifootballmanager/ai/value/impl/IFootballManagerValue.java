package com.ljs.ifootballmanager.ai.value.impl;

import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.value.Value;

/**
 *
 * @author lstephen
 */
public class IFootballManagerValue implements Value {

    private IFootballManagerValue() { }

    public Double getValue(Player p) {
        Double base = getPotential(p);

        return base * getAgingFactor(p);
    }

    private Double getAgingFactor(Player p) {
        Integer agingStartsAt = p.getOverall(Tactic.NORMAL).getRole() == Role.GK ? 35 : 32;
        return 1.0 - ((double) Math.pow(Math.max(0, p.getAge() - agingStartsAt), 2) * 2 / 100);
    }

    public static IFootballManagerValue create() {
        return new IFootballManagerValue();
    }

    private Double getPotential(Player p) {
        return IFootballManagerPotential
            .create()
            .atPotential(p)
            .getOverall(Tactic.NORMAL)
            .getRating();
    }

}
