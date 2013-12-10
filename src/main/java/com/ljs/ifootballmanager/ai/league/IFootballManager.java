package com.ljs.ifootballmanager.ai.league;

import com.ljs.ifootballmanager.ai.Role;

/**
 *
 * @author lstephen
 */
public class IFootballManager implements League {

    private static final IFootballManager INSTANCE = new IFootballManager();

    private IFootballManager() { }

    public String getTeam() {
        return "liv";
    }

    public Integer getMinimum(Role r) {
        switch (r) {
            case GK: return 1;
            case DF: return 3;
            case DM: return 0;
            case MF: return 2;
            case AM: return 0;
            case FW: return 0;
        }

        throw new IllegalStateException();
    }

    public Integer getMaximum(Role r) {
        switch (r) {
            case GK: return 1;
            case DF: return 6;
            case DM: return 3;
            case MF: return 5;
            case AM: return 3;
            case FW: return 5;
        }

        throw new IllegalStateException();
    }

    public static IFootballManager get() {
        return INSTANCE;
    }

}
