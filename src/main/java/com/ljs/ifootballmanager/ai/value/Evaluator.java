package com.ljs.ifootballmanager.ai.value;

import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.rating.Ratings;

/**
 *
 * @author lstephen
 */
public final class Evaluator {

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
