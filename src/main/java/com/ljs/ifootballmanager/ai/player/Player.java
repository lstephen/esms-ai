package com.ljs.ifootballmanager.ai.player;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.league.League;
import com.ljs.ifootballmanager.ai.rating.Rating;
import com.ljs.ifootballmanager.ai.rating.Ratings;
import com.ljs.ifootballmanager.ai.value.Evaluator;
import com.ljs.ifootballmanager.ai.value.RatingInRole;
import com.ljs.ifootballmanager.ai.value.Value;
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

    private final Ratings abilities;

    private Integer fitness = 100;

    private Boolean injured = Boolean.FALSE;

    private Boolean suspended = Boolean.FALSE;

    private Boolean reserves = Boolean.FALSE;

    private String comment = "";

    private Player(String name, Integer age, Ratings ratings, Ratings abilities) {
        this.name = name;
        this.age = age;
        this.ratings = ratings;
        this.abilities = abilities;
    }

    public Player atPercent(Integer percentage) {
        return new Player(name, age, ratings.atPercent(percentage), abilities);
    }

    public Player withSkillAdded(Rating rt, Integer amount) {
        return withSkills(ratings.add(rt, amount));
    }

    public Player withAbilityAdded(Rating rt, Integer amount) {
        Ratings newAbilities = abilities.add(rt, amount);
        Ratings newSkills = ratings;

        while (newAbilities.getSkill(rt) > 999.5) {
            newSkills = newSkills.add(rt, 1);
            newAbilities = newAbilities.subtract(rt, 1000);
        }

        return withSkills(newSkills).withAbilities(newAbilities);
    }

    private Player withSkills(Ratings skills) {
        return new Player(name, age, skills, abilities);
    }

    private Player withAbilities(Ratings abilities) {
        return new Player(name, age, ratings, abilities);
    }

    public Player afterMinutes(Integer minutes) {
        return atPercent((int) (100.0 * Math.pow(0.996, minutes)));
    }

    public Optional<Player> forSelection() {
        if (injured || suspended || reserves) {
            return Optional.absent();
        }
        return Optional.of(atPercent(fitness));
    }

    public Optional<Player> forReservesSelection() {
        if (injured || suspended || !reserves) {
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

    public void reserves() {
        this.reserves = Boolean.TRUE;
    }

    public Boolean isReserves() {
        return reserves;
    }

    public String getRosterStatus() {
        return String.format(
            "%1s%1s%1s",
            injured ? "I" : "",
            suspended ? "S" : "",
            reserves ? "R" : "");
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isGk() {
        return getOverall(Tactic.NORMAL).getRole() == Role.GK;
    }

    public RatingInRole getOverall(Tactic t) {
        return RatingInRole.byRating().max(evaluateAll(t));
    }

    public ImmutableSet<RatingInRole> evaluateAll(Tactic t) {
        Set<RatingInRole> evaluations = Sets.newHashSet();

        for (Role r : Role.values()) {
            evaluations.add(evaluate(r, t));
        }

        return ImmutableSet.copyOf(evaluations);
    }

    public RatingInRole evaluate(Role r, Tactic t) {
        return Evaluator.create(ratings).evaluate(r, t);
    }

    public Double getRating(Role r, Tactic t) {
        return evaluate(r, t).getRating();
    }

    public Double getSkillRating(Role rl, Tactic tc, Rating rt) {
        return ratings.getSkillRating(rl, tc, rt);
    }

    public Double getSkillRating(Role rl, Tactic tc, Rating rt, Tactic vs) {
        return ratings.getSkillRating(rl, tc, rt, vs);
    }

    public Double getSkill(Rating rt) {
        return ratings.getSkill(rt);
    }

    public Double getMaximumSkill() {
        return ratings.getMaximumSkill();
    }

    public Rating getPrimarySkill() {
        return ratings.getSkillPriority().get(0);
    }

    public Rating getSecondarySkill() {
        return ratings.getSkillPriority().get(1);
    }

    public Rating getTertiarySkill() {
        return ratings.getSkillPriority().get(2);
    }

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
        return name.hashCode();
    }

    public static Player create(String name, Integer age, Ratings ratings, Ratings abilities) {
        return new Player(name, age, ratings, abilities);
    }

    public static Ordering<Player> byOverall(final Tactic t) {
        return Ordering
            .natural()
            .onResultOf(new Function<Player, Double>() {
                public Double apply(Player p) {
                    return p.getOverall(t).getRating();
                }
            })
            .compound(byTieBreak());
    }

    public static Ordering<Player> byRating(final Role r, final Tactic t) {
        return Ordering
            .natural()
            .onResultOf(new Function<Player, Double>() {
                public Double apply(Player p) {
                    return p.evaluate(r, t).getRating();
                }
            });
    }

    public static Ordering<Player> byValue(final League league) {
        return byValue(league.getPlayerValue());
    }

    public static Ordering<Player> byValue(final Value value) {
        return Ordering
            .natural()
            .onResultOf(new Function<Player, Double>() {
                public Double apply(Player p) {
                    return value.getValue(p);
                }
            })
            .compound(byTieBreak());
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
