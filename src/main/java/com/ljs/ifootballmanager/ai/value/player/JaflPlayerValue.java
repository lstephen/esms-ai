package com.ljs.ifootballmanager.ai.value.player;

import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.value.PlayerValue;

/**
 *
 * @author lstephen
 */
public final class JaflPlayerValue implements PlayerValue {

    private JaflPlayerValue() { }

    public Integer getValue(Player p) {
        Integer base = p.getOverall(Tactic.NORMAL).getRating();

        return (int) Math.round(base * getAgingFactor(p.getAge()));
    }

    private Double getAgingFactor(Integer age) {
        return 1.0 - Math.pow(Math.max(0, age - 30), 2) * 2;
    }

    public static JaflPlayerValue create() {
        return new JaflPlayerValue();
    }

}
