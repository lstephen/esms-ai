package com.ljs.ifootballmanager.ai.player;

/**
 *
 * @author lstephen
 */
public final class SquadHolder {

    private static Squad squad;

    private SquadHolder() { }

    public static Squad get() {
        return squad;
    }

    public static void set(Squad squad) {
        SquadHolder.squad = squad;
    }

}
