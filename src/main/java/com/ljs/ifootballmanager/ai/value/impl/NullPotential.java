package com.ljs.ifootballmanager.ai.value.impl;

import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.value.Potential;

/**
 *
 * @author lstephen
 */
public class NullPotential implements Potential {

    private NullPotential() { }

    public Player atPotential(Player p) {
        return p;
    }

    public static NullPotential create() {
        return new NullPotential();
    }

}
