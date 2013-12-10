package com.ljs.ifootballmanager.ai.selection;

import aima.core.search.framework.HeuristicFunction;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.league.League;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.search.Action;
import com.ljs.ifootballmanager.ai.search.ActionsFunction;
import com.ljs.ifootballmanager.ai.search.RepeatedHillClimbing;
import com.ljs.ifootballmanager.ai.search.State;
import java.io.PrintWriter;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 *
 * @author lstephen
 */
public final class ChangePlan implements State {

    private final Formation formation;

    private final ImmutableSet<Substitution> substitutions;

    private ChangePlan(Formation formation, Iterable<Substitution> subs) {
        this.formation = formation;
        this.substitutions = ImmutableSet.copyOf(subs);
    }

    public void print(PrintWriter w) {
        for (Substitution s : Substitution.byMinute().sortedCopy(substitutions)) {
            s.print(w);
        }
    }

    private ChangePlan withSubstitution(Substitution s) {
        Set<Substitution> ss = Sets.newHashSet(substitutions);

        ss.add(s);

        return new ChangePlan(formation, ss);
    }

    private ChangePlan removeSubstitution(Substitution s) {
        Set<Substitution> ss = Sets.newHashSet(substitutions);

        ss.remove(s);

        return new ChangePlan(formation, ss);
    }

    public Boolean isValid() {
        Set<Player> in = Sets.newHashSet();
        Set<Player> out = Sets.newHashSet();

        for (Substitution s : substitutions) {
            if (!getFormationAt(s.getMinute() - 1).isValid(s)) {
                return false;
            }
            if (out.contains(s.getOut())) {
                return false;
            }
            if (in.contains(s.getIn())) {
                return false;
            }
            in.add(s.getIn());
            out.add(s.getOut());
        }

        return true;

    }

    public Integer score() {
        Integer score = 0;

        for (Integer minute = 0; minute <= 90; minute++) {
            score += score(minute);
        }

        return score + substitutions.size();
    }

    public Integer score(Integer minute) {
        Integer score = 0;
        Formation current = getFormationAt(minute);
        for (Player p : current.players()) {
            score += p.evaluate(current.findRole(p)).getRating();
        }
        return score;

    }

    public Boolean isUsed(Player p) {
        if (formation.contains(p)) {
            return true;
        }

        for (Substitution s : substitutions) {
            if (s.getIn().equals(p)) {
                return true;
            }
        }

        return false;
    }

    private Formation getFormationAt(Integer minute) {
        Multimap<Role, Player> players = HashMultimap.create();

        for (Player p : formation.players()) {
            players.put(formation.findRole(p), p.atPercent(getFitnessAfterMinutesPlayed(minute)));
        }

        Formation f = Formation.create(formation.getLeague(), players);

        for (Substitution s : Substitution.byMinute().sortedCopy(substitutions)) {
            boolean isMade = s.getMinute() <= minute;

            if (isMade) {
                f = f.substitute(
                    s.getIn().atPercent(getFitnessAfterMinutesPlayed(minute - s.getMinute())),
                    s.getRole(),
                    s.getOut());
            }
        }

        return f;
    }

    private Integer getFitnessAfterMinutesPlayed(Integer minutesPlayed) {
        return 100 - minutesPlayed / 3;
    }

    public static ChangePlan select(League league, final Formation f, Iterable<Player> squad) {
        return new RepeatedHillClimbing<ChangePlan>(
            ChangePlan.class,
            heuristic(),
            new Callable<ChangePlan>() {
                public ChangePlan call() {
                    return new ChangePlan(f, ImmutableSet.<Substitution>of());
                }
            },
            actionsFunction(league, squad))
            .search();
    }

    private static HeuristicFunction heuristic() {
        return new HeuristicFunction() {
            @Override
            public double h(Object state) {
                return -ChangePlan.class.cast(state).score();
            }
        };
    }

    private static ActionsFunction<ChangePlan> actionsFunction(final League league, final Iterable<Player> available) {
        return new ActionsFunction<ChangePlan>() {

            @Override
            public Set<Action<ChangePlan>> getActions(ChangePlan cp) {
                Set<Action<ChangePlan>> actions = Sets.newHashSet();

                if (cp.substitutions.size() < 3) {
                    actions.addAll(addSubsitution(cp));
                }

                actions.addAll(changeSubstitution(cp));

                for (Substitution s : cp.substitutions) {
                    actions.add(new RemoveSubstitution(s));
                }

                return actions;
            }

            private Set<Substitution> availableSubstitutions(ChangePlan cp) {
                Set<Player> toConsider = Sets.newHashSet();

                for (Role r : Role.values()) {
                    for (Player p : Player.byRating(r).reverse().sortedCopy(available)) {
                        if (!cp.isUsed(p)) {
                            toConsider.add(p);
                            break;
                        }
                    }
                }

                Set<Substitution> ss = Sets.newHashSet();
                for (Player in : toConsider) {
                    for (Integer minute = 0; minute <= 90; minute++) {
                        Formation currentFormation = cp.getFormationAt(minute);
                        for (Player out : currentFormation.players()) {
                            for (Role r : Role.values()) {
                                Substitution s = Substitution
                                    .builder()
                                    .in(in, r)
                                    .out(out)
                                    .minute(minute)
                                    .build();

                                ss.add(s);
                            }
                        }
                    }
                }

                return ss;
            }

            private Set<AddSubstitution> addSubsitution(ChangePlan cp) {
                Set<AddSubstitution> adds = Sets.newHashSet();

                for (Substitution s : availableSubstitutions(cp)) {
                    adds.add(new AddSubstitution(s));
                }

                return adds;
            }

            private Set<ChangeSubstitution> changeSubstitution(ChangePlan cp) {
                Set<ChangeSubstitution> changes = Sets.newHashSet();

                for (Substitution add : availableSubstitutions(cp)) {
                    for (Substitution remove : cp.substitutions) {
                        changes.add(new ChangeSubstitution(remove, add));
                    }
                }

                return changes;
            }
        };
    }

    private static class AddSubstitution extends Action<ChangePlan> {

        private final Substitution substitution;

        public AddSubstitution(Substitution substitution) {
            super();
            this.substitution = substitution;
        }

        public ChangePlan apply(ChangePlan cp) {
            return cp.withSubstitution(substitution);
        }
    }

    private static class ChangeSubstitution extends Action<ChangePlan> {

        private final Substitution remove;
        private final Substitution add;

        public ChangeSubstitution(Substitution remove, Substitution add) {
            super();
            this.remove = remove;
            this.add = add;
        }

        public ChangePlan apply(ChangePlan cp) {
            return cp.removeSubstitution(remove).withSubstitution(add);
        }
    }

    private static class RemoveSubstitution extends Action<ChangePlan> {
        private final Substitution remove;

        public RemoveSubstitution(Substitution remove) {
            super();
            this.remove = remove;
        }

        public ChangePlan apply(ChangePlan cp) {
            return cp.removeSubstitution(remove);
        }
    }

}
