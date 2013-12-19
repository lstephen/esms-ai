package com.ljs.ifootballmanager.ai.formation.selection;

import com.ljs.ai.search.Action;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.formation.Formation;
import com.ljs.ifootballmanager.ai.player.Player;

/**
 *
 * @author lstephen
 */
public class Move extends Action<Formation> {

    private final Player player;

    private final Role role;

    public Move(Player player, Role role) {
        super();
        this.player = player;
        this.role = role;
    }

    public Formation apply(Formation f) {
        Formation next = f.move(role, player);
        return next;
    }

}
