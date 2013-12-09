package com.ljs.ifootballmanager.ai.selection;

import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.player.Player;

/**
 *
 * @author lstephen
 */
public final class Selections {

    private Selections() { }

    public static Player select(Iterable<Player> available) {
        return Player.byOverall().max(available);
    }

    public static Player select(Role r, Iterable<Player> available) {
        return Player.byRating(r).max(available);
    }

}
