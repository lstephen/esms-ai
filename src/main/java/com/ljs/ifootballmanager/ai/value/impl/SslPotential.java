package com.ljs.ifootballmanager.ai.value.impl;

import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.value.Potential;

/**
 *
 * @author lstephen
 */
public class SslPotential implements Potential {

    private SslPotential() { }

    public Player atPotential(Player p) {
        Integer yearsToDevelop = Math.max(0, 20 - p.getAge());

        p = p.withAbilityAdded(p.getPrimarySkill(), 1500 * yearsToDevelop);
        p = p.withAbilityAdded(p.getSecondarySkill(), 900 * yearsToDevelop);
        p = p.withAbilityAdded(p.getTertiarySkill(), 600 * yearsToDevelop);

        if (p.getAge() == 20 || p.getAge() == 21) {
            Integer factor = 22 - p.getAge();
            p = p.withAbilityAdded(p.getPrimarySkill(), 800 * factor);
        }

        return p;
    }

    public static SslPotential create() {
        return new SslPotential();
    }

}
