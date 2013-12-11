package com.ljs.ifootballmanager.ai.selection;

import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.league.League;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.report.Report;
import com.ljs.ifootballmanager.ai.search.Action;
import com.ljs.ifootballmanager.ai.search.ActionsFunction;
import com.ljs.ifootballmanager.ai.search.PairedAction;
import com.ljs.ifootballmanager.ai.search.RepeatedHillClimbing;
import com.ljs.ifootballmanager.ai.search.State;
import java.io.PrintWriter;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 *
 * @author lstephen
 */
public final class ChangePlan implements State, Report {

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

    public void printTactics(PrintWriter w, Function<Player, Integer> playerIdx) {
        for (Substitution s : Substitution.byMinute().sortedCopy(substitutions)) {
            s.print(w, playerIdx);
        }

    }

    public ImmutableSet<Player> getSubstitutes() {
        Set<Player> subs = Sets.newHashSet();

        for (Substitution s : substitutions) {
            subs.add(s.getIn());
        }

        return ImmutableSet.copyOf(subs);
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
            score += p.evaluate(current.findRole(p), current.getTactic()).getRating();
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

        Formation f = Formation.create(formation.getLeague(), formation.getTactic(), players);

        for (Substitution s : Substitution.byMinute().sortedCopy(substitutions)) {
            boolean isMade = s.getMinute() < minute;

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
            new Callable<ChangePlan>() {
                public ChangePlan call() {
                    return new ChangePlan(f, ImmutableSet.<Substitution>of());
                }
            },
            actionsFunction(league, squad))
            .search();
    }

    private static ActionsFunction<ChangePlan> actionsFunction(final League league, final Iterable<Player> available) {
        return new ActionsFunction<ChangePlan>() {

            @Override
            public Set<Action<ChangePlan>> getActions(ChangePlan cp) {
                Set<Action<ChangePlan>> actions = Sets.newHashSet();

                ImmutableSet<AddSubstitution> adds = ImmutableSet.copyOf(addSubsitution(cp));
                ImmutableSet<RemoveSubstitution> removes = ImmutableSet.copyOf(removeSubstitution(cp));

                if (cp.substitutions.size() < 3) {
                    actions.addAll(adds);
                }

                actions.addAll(removes);
                actions.addAll(PairedAction.merged(removes, adds));
                /*actions.addAll(addPositionChanges(cp));
                actions.addAll(removePositionChanges(cp));

                actions.addAll(PairedAction.merged(removePositionChanges(cp), addPositionChanges(cp)));*/

                return actions;
            }

            private Set<Substitution> availableSubstitutions(ChangePlan cp) {
                Set<Substitution> ss = Sets.newHashSet();

                for (Role r : Role.values()) {
                    for (Player in : Player.byRating(r, cp.formation.getTactic()).reverse().sortedCopy(available)) {
                        if (cp.isUsed(in)) {
                            continue;
                        }

                        for (Integer minute = 1; minute <= 90; minute++) {
                            Formation currentFormation = cp.getFormationAt(minute);
                            for (Player out : currentFormation.players()) {
                                Substitution s = Substitution
                                    .builder()
                                    .in(in, r)
                                    .out(out)
                                    .minute(minute)
                                    .build();

                                ss.add(s);
                            }
                        }
                        break;
                    }
                }

                return ss;
            }

            private Set<RemoveSubstitution> removeSubstitution(ChangePlan cp) {
                Set<RemoveSubstitution> removes = Sets.newHashSet();
                for (Substitution s : cp.substitutions) {
                    removes.add(new RemoveSubstitution(s));
                }
                return removes;
            }

            private Set<AddSubstitution> addSubsitution(ChangePlan cp) {
                Set<AddSubstitution> adds = Sets.newHashSet();

                for (Substitution s : availableSubstitutions(cp)) {
                    adds.add(new AddSubstitution(s));
                }

                return adds;
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
