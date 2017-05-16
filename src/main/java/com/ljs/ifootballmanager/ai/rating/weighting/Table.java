package com.ljs.ifootballmanager.ai.rating.weighting;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.rating.TacticWeightings;
import com.ljs.ifootballmanager.ai.rating.Weighting;
import com.ljs.ifootballmanager.ai.rating.Weightings;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;

/** @author lstephen */
public class Table implements Weightings {

  private final ImmutableMap<Pair<Role, Tactic>, Weighting> table;

  private final ImmutableMultimap<Pair<Tactic, Tactic>, Pair<Role, Weighting>> bonus;

  private Table(Builder builder) {
    this.table = ImmutableMap.copyOf(builder.table);
    this.bonus = ImmutableMultimap.copyOf(builder.bonus);
  }

  public Weighting get(Role r, Tactic t) {
    return table.get(Pair.of(r, t));
  }

  private Iterable<Pair<Role, Weighting>> getBonus(Tactic t, Tactic vs) {
    return bonus.get(Pair.of(t, vs));
  }

  public TacticWeightings forTactic(final Tactic t) {
    return ForTactic.create(this, t);
  }

  public static Builder builder() {
    return Builder.create();
  }

  private static Table build(Builder builder) {
    return new Table(builder);
  }

  public static final class Builder {

    private Map<Pair<Role, Tactic>, Weighting> table = Maps.newHashMap();

    private Multimap<Pair<Tactic, Tactic>, Pair<Role, Weighting>> bonus = HashMultimap.create();

    private Builder() {}

    public Builder from(Table table) {
      this.table.putAll(table.table);
      return this;
    }

    public Builder forAllTactics(Role r, Weighting w) {
      for (Tactic t : Tactic.values()) {
        add(r, t, w);
      }
      return this;
    }

    public Builder add(Role r, Tactic t, Weighting w) {
      table.put(Pair.of(r, t), w);
      return this;
    }

    public Builder bonus(Role r, Tactic t, Weighting w, Tactic... vs) {
      for (Tactic v : vs) {
        bonus.put(Pair.of(t, v), Pair.of(r, w));
      }
      return this;
    }

    public Table build() {
      return Table.build(this);
    }

    private static Builder create() {
      return new Builder();
    }
  }

  private static final class ForTactic implements TacticWeightings {

    private final Table table;

    private final Tactic tactic;

    private ForTactic(Table table, Tactic tactic) {
      this.table = table;
      this.tactic = tactic;
    }

    public Weighting inRole(Role r) {
      return table.get(r, tactic);
    }

    public TacticWeightings vs(Tactic vs) {
      return VsTactic.create(table, tactic, vs);
    }

    public static ForTactic create(Table table, Tactic tactic) {
      return new ForTactic(table, tactic);
    }
  }

  private static final class VsTactic implements TacticWeightings {

    private final Table table;
    private final Tactic tactic;
    private final Tactic vs;

    private VsTactic(Table table, Tactic t, Tactic vs) {
      this.table = table;
      this.tactic = t;
      this.vs = vs;
    }

    public Weighting inRole(Role r) {
      Weighting base = table.get(r, tactic);
      Weighting bonus = Weighting.create(0, 0, 0);

      for (Pair<Role, Weighting> b : table.getBonus(tactic, vs)) {
        if (b.getLeft() == r) {
          bonus = b.getRight();
        }
      }

      return base.add(bonus);
    }

    public TacticWeightings vs(Tactic vs) {
      return create(table, tactic, vs);
    }

    public static VsTactic create(Table table, Tactic t, Tactic vs) {
      return new VsTactic(table, t, vs);
    }
  }
}
