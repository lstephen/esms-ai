package com.ljs.ifootballmanager.ai.league;

/**
 *
 * @author lstephen
 */
public final class LeagueHolder {

    private static League league;

    private LeagueHolder() { }

    @Deprecated
    public static League get() {
        return league;
    }

    @Deprecated
    public static void set(League league) {
        LeagueHolder.league = league;
    }

}
