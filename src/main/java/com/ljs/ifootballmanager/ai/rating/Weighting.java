package com.ljs.ifootballmanager.ai.rating;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;

/**
 *
 * @author lstephen
 */
public final class Weighting {

    private final ImmutableMap<Rating, Integer> weights;

    private Weighting(Builder builder) {
        weights = ImmutableMap.copyOf(builder.weights);
    }

    public Integer get(Rating rating) {
        return weights.containsKey(rating)
            ? weights.get(rating)
            : 0;
    }

    public Integer sum() {
        Integer sum = 0;
        for (Integer weight : weights.values()) {
            sum += weight;
        }
        return sum;
    }

    public static Builder builder() {
        return Builder.create();
    }

    private static Weighting build(Builder builder) {
        return new Weighting(builder);
    }

    public static final class Builder {

        private final Map<Rating, Integer> weights = Maps.newHashMap();

        private Builder() { }

        public Builder st(Integer st) {
            return stopping(st);
        }

        public Builder stopping(Integer st) {
            weights.put(Rating.STOPPING, st);
            return this;
        }

        public Builder tk(Integer tk) {
            return tackling(tk);
        }

        public Builder tackling(Integer tk) {
            weights.put(Rating.TACKLING, tk);
            return this;
        }

        public Builder ps(Integer ps) {
            return passing(ps);
        }

        public Builder passing(Integer ps) {
            weights.put(Rating.PASSING, ps);
            return this;
        }

        public Builder sh(Integer sh) {
            return shooting(sh);
        }

        public Builder shooting(Integer sh) {
            weights.put(Rating.SHOOTING, sh);
            return this;
        }

        public Weighting build() {
            return Weighting.build(this);
        }

        private static Builder create() {
            return new Builder();
        }

    }

}
