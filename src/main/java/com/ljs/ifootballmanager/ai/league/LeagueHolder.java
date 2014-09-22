package com.ljs.ifootballmanager.ai.league;

/**
 *
 * @author lstephen
 */
public final class LeagueHolder {

    private static League league;

    private LeagueHolder() { }

    public static League get() {
        return league;
    }

    public static void set(League league) {
        LeagueHolder.league = league;
    }

}
