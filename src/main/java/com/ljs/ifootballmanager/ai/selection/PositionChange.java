package com.ljs.ifootballmanager.ai.selection;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.player.Player;
import java.io.PrintWriter;

/**
 *
 * @author lstephen
 */
public class PositionChange {

    private Player player;
    private Role role;
    private Integer minute;

    private PositionChange(Player player, Role role, Integer minute) {
        this.player = player;
        this.role = role;
        this.minute = minute;
    }

    public void print(PrintWriter w) {
        w.format("CHANGEPOS %s %s IF MIN = %d%n", player.getName(), role, minute);
    }

    public Player getPlayer() {
        return player;
    }

    public Role getRole() {
        return role;
    }

    public Integer getMinute() {
        return minute;
    }

    public static PositionChange create(Player p, Role r, Integer m) {
        return new PositionChange(p, r, m);
    }

    public static Ordering<PositionChange> byMinute() {
        return Ordering
            .natural()
            .onResultOf(new Function<PositionChange, Integer>() {
                public Integer apply(PositionChange c) {
                    return c.getMinute();
                }
            });
    }



}
