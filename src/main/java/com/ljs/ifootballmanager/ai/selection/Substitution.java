package com.ljs.ifootballmanager.ai.selection;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.formation.Formation;
import com.ljs.ifootballmanager.ai.player.Player;
import java.io.PrintWriter;
import java.util.Objects;

/** @author lstephen */
public final class Substitution implements Change {

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
    w.format("SUB %s %s %s IF MIN = %d%n", out.getName(), in.getName(), role, minute);
  }

  public void print(PrintWriter w, Function<Player, Integer> playerIdx) {
    w.format("SUB %s %s %s IF MIN = %d%n", playerIdx.apply(out), playerIdx.apply(in), role, minute);
  }

  public ImmutableSet<Player> getPlayersInvolved() {
    return ImmutableSet.of(in, out);
  }

  public Boolean isValid(ChangePlan cp) {
    if (!cp.getFormationAt(minute - 1).isValid(this)) {
      return false;
    }
    for (Substitution s : cp.changesMadeAt(minute, Substitution.class)) {
      if (s == this) {
        continue;
      }
      if (s.getIn().equals(getIn())) {
        return false;
      }
      if (s.getOut().equals(getIn())) {
        return false;
      }
    }

    return true;
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

  public Formation apply(Formation f, Integer minute) {
    return f.substitute(in.afterMinutes(minute - getMinute()), role, out);
  }

  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof Substitution)) {
      return false;
    }

    Substitution rhs = Substitution.class.cast(obj);

    return Objects.equals(in, rhs.in)
        && Objects.equals(out, rhs.out)
        && Objects.equals(role, rhs.role)
        && Objects.equals(minute, rhs.minute);
  }

  public int hashCode() {
    return Objects.hash(in, out, role, minute);
  }

  public static Builder builder() {
    return Builder.create();
  }

  private static Substitution build(Builder builder) {
    return new Substitution(builder);
  }

  public static final class Builder {

    private Player in;
    private Player out;
    private Role role;
    private Integer minute;

    private Builder() {}

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
