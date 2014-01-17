package com.ljs.ifootballmanager.ai.value;

import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.player.Player;

/**
 *
 * @author lstephen
 */
public final class OverallValue implements Value {

    private OverallValue() { }

    @Override
    public Double getValue(Player p) {
        return p.getOverall(Tactic.NORMAL).getRating();
    }

    public static OverallValue create() {
        return new OverallValue();
    }

}
