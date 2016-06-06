package com.ljs.ifootballmanager.ai.player;

/**
 *
 * @author lstephen
 */
public final class SquadHolder {

    private static Squad squad;

    private SquadHolder() { }

    @Deprecated
    public static Squad get() {
        return squad;
    }

    @Deprecated
    public static void set(Squad squad) {
        SquadHolder.squad = squad;
    }

}
