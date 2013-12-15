package com.ljs.ifootballmanager.ai.rating;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.Tactic;
import java.util.Map;

/**
 *
 * @author lstephen
 */
public final class Ratings {

    private final ImmutableMap<Rating, Integer> ratings;

    private Ratings(Builder builder) {
        ratings = ImmutableMap.copyOf(builder.ratings);
    }

    public Integer overall(Weighting w) {
        Integer overall = 0;

        for (Rating r : ratings.keySet()) {
            overall += w.get(r) * ratings.get(r);
        }

        return 100 * overall / w.sum();
    }

    public Ratings atPercent(Integer percent) {
        Builder builder = Builder.create();
        
        for (Rating r : ratings.keySet()) {
            builder.ratings.put(r, (ratings.get(r) * percent) / 100);
        }

        return builder.build();
    }

    public Integer getSkill(Rating rt) {
        return ratings.get(rt) * 100;
    }

    public Integer getWeightedSkill(Weighting w, Rating rt) {
        return w.get(rt) * getSkill(rt);
    }

    public Integer getSkillRating(Role rl, Tactic tc, Rating rt) {
        return tc.getWeighting(rl).get(rt) * ratings.get(rt);
    }

    public Integer getMaximumSkill() {
        return Ordering.natural().max(ratings.values());
    }

    public static Builder builder() {
        return Builder.create();
    }

    private static Ratings build(Builder builder) {
        return new Ratings(builder);
    }

    public static final class Builder {

        private final Map<Rating, Integer> ratings = Maps.newHashMap();

        private Builder() { }

        private Builder rating(Rating r, Integer v) {
            ratings.put(r, v);
            return this;
        }

        public Builder stopping(Integer st) {
            return rating(Rating.STOPPING, st);
        }

        public Builder tackling(Integer tk) {
            return rating(Rating.TACKLING, tk);
        }

        public Builder passing(Integer ps) {
            return rating(Rating.PASSING, ps);
        }

        public Builder shooting(Integer sh) {
            return rating(Rating.SHOOTING, sh);
        }

        public Ratings build() {
            return Ratings.build(this);
        }

        private static Builder create() {
            return new Builder();
        }

    }

}
