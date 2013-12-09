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

    private final Integer rating;

    private RatingInRole(Role role, Integer rating) {
        this.role = role;
        this.rating = rating;
    }

    public Role getRole() {
        return role;
    }

    public Integer getRating() {
        return rating;
    }

    public static RatingInRole create(Role role, Integer rating) {
        return new RatingInRole(role, rating);
    }

    public static Ordering<RatingInRole> byRating() {
        return Ordering
            .natural()
            .onResultOf(new Function<RatingInRole, Integer>() {
                public Integer apply(RatingInRole rr) {
                    return rr.getRating();
                }
            });
    }

}
