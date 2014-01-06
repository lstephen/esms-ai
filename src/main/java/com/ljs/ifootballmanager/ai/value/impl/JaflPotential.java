package com.ljs.ifootballmanager.ai.value.impl;

import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.value.Potential;

/**
 *
 * @author lstephen
 */
public class JaflPotential implements Potential {

    private JaflPotential() { }

    public Player atPotential(Player p) {
        Integer yearsToDevelop = Math.max(0, 22 - p.getAge());

        p = p.withAbilityAdded(p.getPrimarySkill(), 1500 * yearsToDevelop);
        p = p.withAbilityAdded(p.getSecondarySkill(), 1100 * yearsToDevelop);
        p = p.withAbilityAdded(p.getTertiarySkill(), 600 * yearsToDevelop);

        return p;
    }

    public static JaflPotential create() {
        return new JaflPotential();
    }

}
