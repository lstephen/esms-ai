package com.ljs.ifootballmanager.ai.value.impl;

import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.value.Value;

/**
 *
 * @author lstephen
 */
public final class JaflValue implements Value {

    private JaflValue() { }

    public Integer getValue(Player p) {
        Integer base = getPotential(p);

        return (int) Math.round(base * getAgingFactor(p.getAge()));
    }

    private Double getAgingFactor(Integer age) {
        return 1.0 - ((double) Math.pow(Math.max(0, age - 29), 2) * 2 / 100);
    }

    public static JaflValue create() {
        return new JaflValue();
    }

    private Integer getPotential(Player p) {
        return JaflPotential
            .create()
            .atPotential(p)
            .getOverall(Tactic.NORMAL)
            .getRating();
    }

}
