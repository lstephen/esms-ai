package com.ljs.ifootballmanager.ai.league;

import com.ljs.ifootballmanager.ai.Role;

/**
 *
 * @author lstephen
 */
public final class Pbemff implements League {

    private static final Pbemff INSTANCE = new Pbemff();

    private Pbemff() { }

    public String getTeam() {
        return "sfc";
    }

    public Integer getMinimum(Role r) {
        switch (r) {
            case GK:
                return 1;
            case DF:
                return 3;
            case DM:
            case MF:
            case AM:
            case FW:
                return 0;
        }

        throw new IllegalStateException();
    }

    public Integer getMaximum(Role r) {
        switch (r) {
            case GK: return 1;
            case DF: return 5;
            case DM: return 5;
            case MF: return 7;
            case AM: return 5;
            case FW: return 4;
        }

        throw new IllegalStateException();
    }

    public static Pbemff get() {
        return INSTANCE;
    }
}
