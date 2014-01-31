package com.ljs.ifootballmanager.ai.rating;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.league.League;
import java.util.Arrays;
import java.util.Map;

/**
 *
 * @author lstephen
 */
public final class Ratings {

    private final League league;

    private final ImmutableMap<Rating, Double> ratings;

    private Ratings(Builder builder) {
        this.league = builder.league;
        ratings = ImmutableMap.copyOf(builder.ratings);
    }

    public Double overall(Role r, Tactic t) {
        TacticWeightings tw = league.getWeightings().forTactic(t);
        return overall(tw.inRole(r), tw);
    }

    public Double overall(Weighting w, TacticWeightings tw) {
        Double overall = 0.0;

        for (Rating r : ratings.keySet()) {
            overall += getWeightedSkill(w, r);
        }

        return overall;
    }

    public Ratings atPercent(Integer percent) {
        Builder builder = Builder.create(this);
        
        for (Rating r : ratings.keySet()) {
            Double newValue = ratings.get(r) * percent / 100;

            builder = builder.rating(r, newValue);
        }

        return builder.build();
    }

    public Ratings add(Rating r, Integer value) {
        return Builder
            .create(this)
            .rating(r, getSkill(r) + value)
            .build();
    }

    public Ratings subtract(Rating r, Integer value) {
        return add(r, -value);
    }

    public Double getSkill(Rating rt) {
        return ratings.get(rt);
    }

    public Double getWeightedSkill(Weighting w, Rating rt) {
        return getSkill(rt) * w.get(rt) / 100;
    }

    public Double getSkillRating(Role rl, Tactic tc, Rating rt) {
        return getWeightedSkill(league.getWeightings().forTactic(tc).inRole(rl), rt);
    }

    public Double getSkillRating(Role rl, Tactic tc, Rating rt, Tactic vs) {
        return getWeightedSkill(league.getWeightings().forTactic(tc).vs(vs).inRole(rl), rt);
    }

    public Double getMaximumSkill() {
        return Ordering.natural().max(ratings.values());
    }

    public ImmutableList<Rating> getSkillPriority() {
        return Ordering
            .natural()
            .reverse()
            .onResultOf(new Function<Rating, Double>() {
                public Double apply(Rating r) {
                    return getSkill(r);
                }
            })
            .immutableSortedCopy(Arrays.asList(Rating.values()));
    }

    public static Builder builder() {
        return Builder.create();
    }

    private static Ratings build(Builder builder) {
        return new Ratings(builder);
    }

    public static Ratings combine(Ratings rts, Ratings abs) {
        Ratings.Builder builder = builder().league(rts.league);

        for (Rating r : Rating.values()) {
            Double rt = rts.getSkill(r);
            Double ab = abs.getSkill(r);

            builder = builder.rating(r, rt + ab / 1000);
        }

        return builder.build();
    }

    public static final class Builder {

        private League league;

        private final Map<Rating, Double> ratings = Maps.newHashMap();

        private Builder() { }

        public Builder league(League league) {
            this.league = league;
            return this;
        }

        private Builder rating(Rating r, Integer v) {
            return rating(r, v.doubleValue());
        }

        private Builder rating(Rating r, Double v) {
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

        private static Builder create(Ratings ratings) {
            Builder b = create();
            b.league(ratings.league);
            b.ratings.putAll(ratings.ratings);
            return b;
        }

    }

}
