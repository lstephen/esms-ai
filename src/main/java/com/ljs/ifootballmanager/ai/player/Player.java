package com.ljs.ifootballmanager.ai.player;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.rating.Ratings;
import com.ljs.ifootballmanager.ai.value.Evaluator;
import com.ljs.ifootballmanager.ai.value.RatingInRole;
import java.util.Set;

/**
 *
 * @author lstephen
 */
public final class Player {

    private final String name;

    private final Ratings ratings;

    private Player(String name, Ratings ratings) {
        this.name = name;
        this.ratings = ratings;
    }

    public String getName() {
        return name;
    }

    public InRole inRole(Role r) {
        return InRole.create(this, r);
    }

    public ImmutableSet<InRole> inAllRoles() {
        Set<InRole> inRoles = Sets.newHashSet();

        for (Role r : Role.values()) {
            inRoles.add(inRole(r));
        }

        return ImmutableSet.copyOf(inRoles);
    }

    public RatingInRole getOverall() {
        return RatingInRole.byRating().max(evaluateAll());
    }

    public ImmutableSet<RatingInRole> evaluateAll() {
        Set<RatingInRole> evaluations = Sets.newHashSet();

        for (Role r : Role.values()) {
            evaluations.add(evaluate(r));
        }

        return ImmutableSet.copyOf(evaluations);
    }

    public RatingInRole evaluate(Role r) {
        return Evaluator.create(ratings).evaluate(r);
    }

    public static Player create(String name, Ratings ratings) {
        return new Player(name, ratings);
    }

    public static Ordering<Player> byOverall() {
        return Ordering
            .natural()
            .onResultOf(new Function<Player, Integer>() {
                public Integer apply(Player p) {
                    return p.getOverall().getRating();
                }
            });
    }

    public static Ordering<Player> byRating(final Role r) {
        return Ordering
            .natural()
            .onResultOf(new Function<Player, Integer>() {
                public Integer apply(Player p) {
                    return p.evaluate(r).getRating();
                }
            });
    }

}
