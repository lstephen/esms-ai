package com.ljs.ifootballmanager.ai.rating;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.league.League;
import java.util.Map;

/**
 *
 * @author lstephen
 */
public final class Ratings {

    private final League league;

    private final ImmutableMap<Rating, Integer> ratings;

    private Ratings(Builder builder) {
        this.league = builder.league;
        ratings = ImmutableMap.copyOf(builder.ratings);
    }

    public Integer overall(Role r, Tactic t) {
        return overall(league.getWeightings().forTactic(t).inRole(r));
    }

    public Integer overall(Weighting w) {
        Integer overall = 0;

        for (Rating r : ratings.keySet()) {
            overall += w.get(r) * ratings.get(r);
        }

        return overall;
    }

    public Ratings atPercent(Integer percent) {
        Builder builder = Builder.create();
        builder.league(league);
        
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
        return league.getWeightings().forTactic(tc).inRole(rl).get(rt) * ratings.get(rt);
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

        private League league;

        private final Map<Rating, Integer> ratings = Maps.newHashMap();

        private Builder() { }

        public Builder league(League league) {
            this.league = league;
            return this;
        }

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
