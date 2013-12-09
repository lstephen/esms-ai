package com.ljs.ifootballmanager.ai.rating;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
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
        Integer sum = 0;

        for (Rating r : ratings.keySet()) {
            overall += w.get(r) * ratings.get(r);
            sum += w.get(r);
        }

        return 50 * overall / sum;
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
