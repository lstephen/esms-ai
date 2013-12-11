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
public final class Substitution {

    private final Player in;
    private final Player out;
    private final Role role;
    private final Integer minute;

    private Substitution(Builder builder) {
        this.in = builder.in;
        this.out = builder.out;
        this.role = builder.role;
        this.minute = builder.minute;
    }

    public void print(PrintWriter w) {
        w.format("SUB %s %s %s IF MIN = %d%n", in.getName(), out.getName(), role, minute);
    }

    public void print(PrintWriter w, Function<Player, Integer> playerIdx) {
        w.format("SUB %s %s %s IF MIN = %d%n", playerIdx.apply(in), playerIdx.apply(out), role, minute);
    }

    public Player getIn() {
        return in;
    }

    public Role getRole() {
        return role;
    }

    public Player getOut() {
        return out;
    }

    public Integer getMinute() {
        return minute;
    }

    public static Builder builder() {
        return Builder.create();
    }

    private static Substitution build(Builder builder) {
        return new Substitution(builder);
    }

    public static Ordering<Substitution> byMinute() {
        return Ordering
            .natural()
            .onResultOf(new Function<Substitution, Integer>() {
                public Integer apply(Substitution s) {
                    return s.getMinute();
                }
            });
    }

    public static final class Builder {

        private Player in;
        private Player out;
        private Role role;
        private Integer minute;

        private Builder() { }

        public Builder in(Player player, Role role) {
            this.in = player;
            this.role = role;
            return this;
        }

        public Builder out(Player player) {
            this.out = player;
            return this;
        }

        public Builder minute(Integer minute) {
            this.minute = minute;
            return this;
        }

        public Substitution build() {
            return Substitution.build(this);
        }

        private static Builder create() {
            return new Builder();
        }
    }

}
