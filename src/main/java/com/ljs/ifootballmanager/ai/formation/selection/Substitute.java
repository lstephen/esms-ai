package com.ljs.ifootballmanager.ai.formation.selection;

import com.ljs.ai.search.Action;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.formation.Formation;
import com.ljs.ifootballmanager.ai.player.Player;

/**
 *
 * @author lstephen
 */
public class Substitute extends Action<Formation> {

    private final Player in;

    private final Role r;

    private final Player out;

    public Substitute(Player in, Role r, Player out) {
        super();
        this.in = in;
        this.r = r;
        this.out = out;
    }

    public Formation apply(Formation f) {
        Formation next = f.substitute(in, r, out);
        return next;
    }

}
