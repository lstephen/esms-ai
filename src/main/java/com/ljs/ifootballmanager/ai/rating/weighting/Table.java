package com.ljs.ifootballmanager.ai.rating.weighting;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.rating.TacticWeightings;
import com.ljs.ifootballmanager.ai.rating.Weighting;
import com.ljs.ifootballmanager.ai.rating.Weightings;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author lstephen
 */
public class Table implements Weightings {

    private final ImmutableMap<Pair<Role, Tactic>, Weighting> table;

    private Table(Builder builder) {
        this.table = ImmutableMap.copyOf(builder.table);
    }

    public Weighting get(Role r, Tactic t) {
        return table.get(Pair.of(r, t));
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

        private Builder() { }

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

        public TacticWeightings vs(Tactic t) {
            return this;
        }

        public static ForTactic create(Table table, Tactic tactic) {
            return new ForTactic(table, tactic);
        }

    }

}
