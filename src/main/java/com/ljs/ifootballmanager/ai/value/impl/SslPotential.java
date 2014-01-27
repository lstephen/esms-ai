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
        p = p.withAbilityAdded(p.getSecondarySkill(), 1100 * yearsToDevelop);
        p = p.withAbilityAdded(p.getTertiarySkill(), 600 * yearsToDevelop);

        return p;
    }

    public static SslPotential create() {
        return new SslPotential();
    }

}
