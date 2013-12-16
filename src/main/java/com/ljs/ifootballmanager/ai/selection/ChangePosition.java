package com.ljs.ifootballmanager.ai.selection;

import com.ljs.ifootballmanager.ai.formation.Formation;
import com.google.common.base.Function;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.player.Player;
import java.io.PrintWriter;

/**
 *
 * @author lstephen
 */
public final class ChangePosition implements Change {

    private Player player;

    private Role role;

    private Integer minute;

    private ChangePosition(Player player, Role role, Integer minute) {
        this.player = player;
        this.role = role;
        this.minute = minute;
    }

    public Integer getMinute() {
        return minute;
    }

    public void print(PrintWriter w) {
        w.format("CHANGEPOS %s %s IF MIN=%s%n", player.getName(), role, minute);
    }

    public void print(PrintWriter w, Function<Player, Integer> playerIdx) {
        w.format("CHANGEPOS %d %s IF MIN=%d%n", playerIdx.apply(player), role, minute);
    }

    public Boolean isValid(ChangePlan cp) {
        if (!cp.getFormationAt(minute - 1).contains(player)) {
            return false;
        }
        if (!apply(cp.getFormationAt(minute), minute).isValid()) {
            return false;
        }
        return true;
    }

    public Formation apply(Formation f, Integer minute) {
        return f.move(role, player);
    }

    public static ChangePosition create(Player p, Role r, Integer m) {
        return new ChangePosition(p, r, m);
    }

}
