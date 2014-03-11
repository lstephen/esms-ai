package com.ljs.ifootballmanager.ai.value;

import com.ljs.ifootballmanager.ai.league.League;
import com.ljs.ifootballmanager.ai.player.Player;

/**
 *
 * @author lstephen
 */
public final class AcquisitionValue implements Value {

    private final League league;

    private AcquisitionValue(League league) {
        this.league = league;
    }

    public Double getValue(Player p) {
        Value value = league.getPlayerValue();

        Double ovr = value.getValue(p);
        Double vsRepl = ReplacementLevelHolder.get().getValueVsReplacement(p);

        Player atPotential = league.getPlayerPotential().atPotential(p);

        if (ovr < value.getValue(atPotential)) {
            vsRepl = Math.max(0, vsRepl);
            vsRepl = Math.max(vsRepl, ReplacementLevelHolder.get().getValueVsReplacement(atPotential));
        }

        return ovr + vsRepl;
    }

    public static AcquisitionValue create(League league) {
        return new AcquisitionValue(league);
    }

}
