package com.ljs.ifootballmanager.ai.report;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.formation.Formation;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.player.Squad;
import java.io.PrintWriter;

/** @author lstephen */
public class SquadSummaryReport implements Report {

  private final Squad squad;

  private final Formation firstXI;

  private final Optional<Formation> secondXI;

  private final Optional<Formation> reservesXI;

  private SquadSummaryReport(Builder builder) {
    this.squad = builder.squad;
    this.firstXI = builder.firstXI;
    this.secondXI = builder.secondXI;
    this.reservesXI = builder.reservesXI;
  }

  private Long getMax(Role r, Tactic t, Iterable<Player> ps) {
    return Math.round(Player.byRating(r, t).max(ps).getRating(r, t));
  }

  private Long getMin(Role r, Iterable<Player> ps) {
    return getMin(r, Tactic.NORMAL, ps);
  }

  private Long getMin(Role r, Tactic t, Iterable<Player> ps) {
    return Math.round(Player.byRating(r, t).min(ps).getRating(r, t));
  }

  public void print(PrintWriter w) {
    Role[] roles = Role.values();

    w.format("%28s ", "");
    for (Role r : roles) {
      w.format("%5s ", r.name());
    }
    w.println();

    w.format("%28s ", "Count");
    for (Role r : roles) {
      w.format("%5s ", squad.count(r));
    }
    w.println();

    w.format("%28s ", "Max");
    for (Role r : roles) {
      w.format("%5s ", getMax(r, Tactic.NORMAL, squad.players()));
    }
    w.println();

    w.format("%28s ", "1st XI (N)");
    for (Role r : roles) {
      ImmutableSet<Player> ps = firstXI.players(r);

      w.format("%5s ", ps.isEmpty() ? "" : getMin(r, ps));
    }
    w.println();

    w.format("%28s ", "1st XI (" + firstXI.getTactic().getCode() + ")");
    for (Role r : roles) {
      ImmutableSet<Player> ps = firstXI.players(r);

      w.format("%5s ", ps.isEmpty() ? "" : getMin(r, firstXI.getTactic(), ps));
    }
    w.println();

    if (secondXI.isPresent()) {
      w.format("%28s ", "2nd XI (N)");
      for (Role r : roles) {
        ImmutableSet<Player> ps = secondXI.get().players(r);

        w.format("%5s ", ps.isEmpty() ? "" : getMin(r, ps));
      }
      w.println();
      w.format("%28s ", "2nd XI (" + secondXI.get().getTactic().getCode() + ")");
      for (Role r : roles) {
        ImmutableSet<Player> ps = secondXI.get().players(r);

        w.format("%5s ", ps.isEmpty() ? "" : getMin(r, secondXI.get().getTactic(), ps));
      }
      w.println();
    }

    if (reservesXI.isPresent()) {
      w.format("%28s ", "Reserves (N)");
      for (Role r : roles) {
        ImmutableSet<Player> ps = reservesXI.get().players(r);

        w.format("%5s ", ps.isEmpty() ? "" : getMin(r, ps));
      }
      w.println();
      w.format("%28s ", "Reserves (" + reservesXI.get().getTactic().getCode() + ")");
      for (Role r : roles) {
        ImmutableSet<Player> ps = reservesXI.get().players(r);

        w.format("%5s ", ps.isEmpty() ? "" : getMin(r, reservesXI.get().getTactic(), ps));
      }
      w.println();
    }

    w.format("%28s%n", "Min");

    for (Tactic t : Tactic.values()) {
      w.format("%28s ", t);
      for (Role r : roles) {
        ImmutableSet<Player> ps = squad.players(r, t);
        w.format("%5s ", ps.isEmpty() ? "" : getMin(r, t, ps));
      }
      w.println();
    }
  }

  public static Builder builder() {
    return Builder.create();
  }

  private static SquadSummaryReport build(Builder builder) {
    return new SquadSummaryReport(builder);
  }

  public static class Builder {

    private Squad squad;

    private Formation firstXI;

    private Optional<Formation> secondXI = Optional.absent();

    private Optional<Formation> reservesXI = Optional.absent();

    private Builder() {}

    public Builder squad(Squad squad) {
      this.squad = squad;
      return this;
    }

    public Builder firstXI(Formation f) {
      this.firstXI = f;
      return this;
    }

    public Builder secondXI(Formation f) {
      secondXI = Optional.fromNullable(f);
      return this;
    }

    public Builder reservesXI(Formation f) {
      reservesXI = Optional.fromNullable(f);
      return this;
    }

    public SquadSummaryReport build() {
      return SquadSummaryReport.build(this);
    }

    private static Builder create() {
      return new Builder();
    }
  }
}
