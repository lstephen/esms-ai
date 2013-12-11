package com.ljs.ifootballmanager.ai.player;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.rating.Ratings;
import com.ljs.ifootballmanager.ai.value.Evaluator;
import com.ljs.ifootballmanager.ai.value.RatingInRole;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author lstephen
 */
public final class Player {

    private final String name;

    private final Integer age;

    private final Ratings ratings;

    private Integer fitness = 100;

    private Boolean injured = Boolean.FALSE;

    private Boolean suspended = Boolean.FALSE;

    private Player(String name, Integer age, Ratings ratings) {
        this.name = name;
        this.age = age;
        this.ratings = ratings;
    }

    public Player atPercent(Integer percentage) {
        return new Player(name, age, ratings.atPercent(percentage));
    }

    public Optional<Player> forSelection() {
        if (injured || suspended) {
            return Optional.absent();
        }
        return Optional.of(atPercent(fitness));
    }

    public String getName() {
        return name;
    }

    public Integer getAge() {
        return age;
    }

    public void setFitness(Integer fitness) {
        this.fitness = fitness;
    }

    public void injured() {
        this.injured = Boolean.TRUE;
    }

    public void suspended() {
        this.suspended = Boolean.TRUE;
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

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!getClass().equals(obj.getClass())) {
            return false;
        }

        Player rhs = Player.class.cast(obj);

        return Objects.equals(name, rhs.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public static Player create(String name, Integer age, Ratings ratings) {
        return new Player(name, age, ratings);
    }

    public static Ordering<Player> byOverall() {
        return Ordering
            .natural()
            .onResultOf(new Function<Player, Integer>() {
                public Integer apply(Player p) {
                    return p.getOverall().getRating();
                }
            })
            .compound(byTieBreak());
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

    public static Ordering<Player> byName() {
        return Ordering
            .natural()
            .onResultOf(new Function<Player, String>() {
                public String apply(Player p) {
                    return p.getName();
                }
            });
    }

    public static Ordering<Player> byAge() {
        return Ordering
            .natural()
            .onResultOf(new Function<Player, Integer>() {
                public Integer apply(Player p) {
                    return p.getAge();
                }
            });
    }

    public static Ordering<Player> byTieBreak() {
        return byAge().reverse().compound(byName());
    }
}
