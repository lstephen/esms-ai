package com.ljs.ifootballmanager.ai.rating.weighting;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.rating.Weighting;
import com.ljs.ifootballmanager.ai.rating.Weightings;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author lstephen
 */
public class Table implements Weightings {

    private ImmutableMap<Pair<Role, Tactic>, Weighting> table;

    private Table(Builder builder) {
        this.table = ImmutableMap.copyOf(builder.table);
    }

    public Weighting get(Role r, Tactic t) {
        //Assertions.assertThat(table).containsKey(Pair.of(r, t));

        return table.get(Pair.of(r, t));
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

}
