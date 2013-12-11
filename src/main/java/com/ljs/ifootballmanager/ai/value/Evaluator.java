package com.ljs.ifootballmanager.ai.value;

import com.google.common.collect.ImmutableMap;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.rating.Ratings;
import com.ljs.ifootballmanager.ai.rating.Weighting;

/**
 *
 * @author lstephen
 */
public final class Evaluator {

    private static final ImmutableMap<Role, Weighting> WEIGHTINGS =
        ImmutableMap
            .<Role, Weighting>builder()
            .put(Role.GK, Weighting.builder().stopping(1).build())
            .put(
                Role.DF,
                Weighting.builder().tackling(100).passing(50).shooting(30).build())
            .put(
                Role.DM,
                Weighting.builder().tackling(67).passing(87).shooting(30).build())
            .put(
                Role.MF,
                Weighting.builder().tackling(30).passing(100).shooting(30).build())
            .put(
                Role.AM,
                Weighting.builder().tackling(30).passing(87).shooting(67).build())
            .put(
                Role.FW,
                Weighting.builder().tackling(30).passing(30).shooting(100).build())
            .build();


    private final Ratings ratings;

    private Evaluator(Ratings ratings) {
        this.ratings = ratings;
    }

    public RatingInRole evaluate(Role r, Tactic t) {
        return RatingInRole.create(r, ratings.overall(t.getWeighting(r)));
    }

    public static Evaluator create(Ratings rs) {
        return new Evaluator(rs);
    }

}
