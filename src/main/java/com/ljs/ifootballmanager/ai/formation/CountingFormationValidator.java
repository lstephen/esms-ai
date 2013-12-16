package com.ljs.ifootballmanager.ai.formation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.ljs.ifootballmanager.ai.Role;
import java.util.Map;

/**
 *
 * @author lstephen
 */
public class CountingFormationValidator implements FormationValidator {

    private ImmutableMap<ImmutableList<Role>, Integer> maximums;

    private ImmutableMap<ImmutableList<Role>, Integer> minimums;

    private CountingFormationValidator(Builder builder) {
        maximums = ImmutableMap.copyOf(builder.maximums);
        minimums = ImmutableMap.copyOf(builder.minimums);
    }

    public Boolean isValid(Formation f) {
        for (Map.Entry<ImmutableList<Role>, Integer> max : maximums.entrySet()) {
            if (f.count(max.getKey()) > max.getValue()) {
                return Boolean.FALSE;
            }
        }

        for (Map.Entry<ImmutableList<Role>, Integer> min : minimums.entrySet()) {
            if (f.count(min.getKey()) < min.getValue()) {
                return Boolean.FALSE;
            }
        }

        return Boolean.TRUE;
    }

    public static Builder builder() {
        return Builder.create();
    }

    private static CountingFormationValidator build(Builder builder) {
        return new CountingFormationValidator(builder);
    }

    public static class Builder {

        private Map<ImmutableList<Role>, Integer> maximums = Maps.newHashMap();

        private Map<ImmutableList<Role>, Integer> minimums = Maps.newHashMap();

        private Builder() { }

        public Builder exactly(Integer count, Role... rs) {
            return range(count, count, rs);
        }

        public Builder range(Integer min, Integer max, Role... rs) {
            max(max, rs);
            min(min, rs);
            return this;
        }

        public Builder max(Integer count, Role... rs) {
            maximums.put(ImmutableList.copyOf(rs), count);
            return this;
        }

        public Builder min(Integer count, Role... rs) {
            minimums.put(ImmutableList.copyOf(rs), count);
            return this;
        }

        public CountingFormationValidator build() {
            return CountingFormationValidator.build(this);
        }

        private static Builder create() {
            return new Builder();
        }
    }

}
