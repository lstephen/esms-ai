package com.ljs.ifootballmanager.ai.value;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import com.ljs.ifootballmanager.ai.Role;

/**
 *
 * @author lstephen
 */
public final class RatingInRole {

    private final Role role;

    private final Double rating;

    private RatingInRole(Role role, Double rating) {
        this.role = role;
        this.rating = rating;
    }

    public Role getRole() {
        return role;
    }

    public Double getRating() {
        return rating;
    }

    public static RatingInRole create(Role role, Double rating) {
        return new RatingInRole(role, rating);
    }

    public static Ordering<RatingInRole> byRating() {
        return Ordering
            .natural()
            .onResultOf(new Function<RatingInRole, Double>() {
                public Double apply(RatingInRole rr) {
                    return rr.getRating();
                }
            });
    }

}
