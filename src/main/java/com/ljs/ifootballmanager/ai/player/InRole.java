package com.ljs.ifootballmanager.ai.player;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import com.ljs.ifootballmanager.ai.Role;

/**
 *
 * @author lstephen
 */
public final class InRole {

    private final Player player;

    private final Role role;

    private InRole(Player player, Role role) {
        this.player = player;
        this.role = role;
    }

    public String getName() {
        return getPlayer().getName();
    }

    public Player getPlayer() {
        return player;
    }

    public Role getRole() {
        return role;
    }

    public Integer getRating() {
        return player.evaluate(role).getRating();
    }

    public static InRole create(Player player, Role role) {
        return new InRole(player, role);
    }

    public static Ordering<InRole> byRating() {
        return Ordering.natural().
            onResultOf(new Function<InRole, Integer>() {
            public Integer apply(InRole ir) {
                return ir.getRating();
            }
        });
    }

    public static Ordering<InRole> byRole() {
        return Ordering.natural().
            onResultOf(new Function<InRole, Role>() {
            public Role apply(InRole ir) {
                return ir.getRole();
            }
        }).compound(byName());
    }

    public static Ordering<InRole> byName() {
        return Ordering.natural().
            onResultOf(new Function<InRole, String>() {
            public String apply(InRole p) {
                return p.getName();
            }
        });
    }

}
