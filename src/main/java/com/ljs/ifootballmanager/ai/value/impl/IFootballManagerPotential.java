package com.ljs.ifootballmanager.ai.value.impl;

import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.rating.Rating;
import com.ljs.ifootballmanager.ai.value.Potential;

/**
 *
 * @author lstephen
 */
public class IFootballManagerPotential implements Potential {

    private IFootballManagerPotential() { }

    public Player atPotential(Player p) {
        Player atPotential = p;
        if (p.getPrimarySkill() == Rating.STOPPING) {
            if (p.getAge() < 23) {
                atPotential = p.withSkillAdded(Rating.STOPPING, 1);
            }
        } else {
            if (p.getAge() < 19) {
                atPotential = atPotential.withSkillAdded(p.getSecondarySkill(), 1);
            }
            if (p.getAge() < 20) {
                atPotential = atPotential
                    .withSkillAdded(p.getPrimarySkill(), 1)
                    .withSkillAdded(p.getTertiarySkill(), 1);
            }
        }
        return atPotential;
    }

    public static IFootballManagerPotential create() {
        return new IFootballManagerPotential();
    }

}
