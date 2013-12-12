package com.ljs.ifootballmanager.ai.selection;

import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
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
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 *
 * @author lstephen
 */
public final class ChangePlan implements State, Report {

    private final Formation formation;

    private final ImmutableSet<Change> changes;

    private ChangePlan(Formation formation, Iterable<Change> cs) {
        this.formation = formation;
        this.changes = ImmutableSet.copyOf(cs);
    }

    private ImmutableList<Change> changes() {
        return Change.Meta.byMinute().immutableSortedCopy(changes);
    }

    public <C extends Change> ImmutableList<C> changes(Class<C> clazz) {
        List<C> cs = Lists.newArrayList();

        for (Change c : changes()) {
            if (clazz.isInstance(c)) {
                cs.add(clazz.cast(c));
            }
        }

        return ImmutableList.copyOf(cs);
    }

    public ImmutableList<Change> changesMadeAt(Integer minute) {
        List<Change> cs = Lists.newArrayList();

        for (Change c : changes()) {
            if (c.getMinute() <= minute) {
                cs.add(c);
            }
        }

        return ImmutableList.copyOf(cs);
    }

    public <C extends Change> ImmutableList<C> changesMadeAt(Integer minute, Class<C> clazz) {
        List<C> cs = Lists.newArrayList();

        for (Change c : changesMadeAt(minute)) {
            if (clazz.isInstance(c)) {
                cs.add(clazz.cast(c));
            }

        }

        return ImmutableList.copyOf(cs);
    }

    public void print(PrintWriter w) {
        for (Change c : changes()) {
            c.print(w);
        }
    }

    public void print(PrintWriter w, Function<Player, Integer> playerIdx) {
        for (Change c : changes()) {
            c.print(w, playerIdx);
        }
    }

    public ImmutableSet<Player> getSubstitutes() {
        Set<Player> subs = Sets.newHashSet();

        for (Substitution s : changes(Substitution.class)) {
            subs.add(s.getIn());
        }

        return ImmutableSet.copyOf(subs);
    }

    private ChangePlan with(Change c) {
        Set<Change> cs = Sets.newHashSet(changes);
        cs.add(c);
        return new ChangePlan(formation, cs);
    }

    private ChangePlan remove(Change c) {
        Set<Change> cs = Sets.newHashSet(changes);
        cs.remove(c);
        return new ChangePlan(formation, cs);
    }

    public Boolean isValid() {

        if (changes(Substitution.class).size() > 3) {
            return false;
        }

        for (Change c : changes) {
            if (!c.isValid(this)) {
                return false;
            }
        }
        return true;
    }

    public Integer score() {
        Integer score = 0;

        for (Integer minute = 0; minute <= 90; minute++) {
            score += score(minute);
        }

        return score - changes.size();
    }

    public Integer score(Integer minute) {
        Integer score = 0;
        Formation current = getFormationAt(minute);
        for (Player p : current.players()) {
            score += p.evaluate(current.findRole(p), current.getTactic()).getRating();
        }
        return score;
    }

    public Formation getFormationAt(Integer minute) {
        Multimap<Role, Player> players = HashMultimap.create();

        for (Player p : formation.players()) {
            players.put(formation.findRole(p), p.afterMinutes(minute));
        }

        Formation f = Formation.create(formation.getLeague(), formation.getTactic(), players);

        for (Change c : changesMadeAt(minute)) {
            f = c.apply(f, minute);
        }

        return f;
    }

    public static ChangePlan select(League league, final Formation f, Iterable<Player> squad) {
        return new RepeatedHillClimbing<ChangePlan>(
            ChangePlan.class,
            new Callable<ChangePlan>() {
                public ChangePlan call() {
                    return new ChangePlan(f, ImmutableSet.<Change>of());
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

                ImmutableSet<AddChange> adds = ImmutableSet.copyOf(adds(cp));
                ImmutableSet<RemoveChange> removes = ImmutableSet.copyOf(removes(cp));

                actions.addAll(adds);
                actions.addAll(removes);
                actions.addAll(PairedAction.merged(removes, adds));

                return actions;
            }

            private Set<Substitution> availableSubstitutions(ChangePlan cp) {
                Set<Substitution> ss = Sets.newHashSet();

                for (Role r : Role.values()) {
                    for (Integer minute = 1; minute <= 90; minute++) {
                        Formation currentFormation = cp.getFormationAt(minute);
                        Integer psConsidered = 0;
                        for (Player in : Player.byRating(r, currentFormation.getTactic()).reverse().sortedCopy(available)) {
                            if (cp.formation.contains(in)) {
                                continue;
                            }

                            for (Player out : currentFormation.players()) {
                                if (out.evaluate(r, currentFormation.getTactic()).getRating()
                                    > in.evaluate(r, currentFormation.getTactic()).getRating()) {
                                    continue;
                                }

                                Substitution s = Substitution
                                    .builder()
                                    .in(in, r)
                                    .out(out)
                                    .minute(minute)
                                    .build();

                                ss.add(s);
                            }
                            psConsidered++;
                            if (psConsidered >= 3) {
                                break;
                            }
                        }
                    }
                }

                return ss;
            }

            private Set<RemoveChange> removes(ChangePlan cp) {
                Set<RemoveChange> removes = Sets.newHashSet();
                for (Change c : cp.changes()) {
                    removes.add(new RemoveChange(c));
                }
                return removes;
            }

            private Set<AddChange> adds(ChangePlan cp) {
                Set<AddChange> adds = Sets.newHashSet();

                for (Substitution s : availableSubstitutions(cp)) {
                    adds.add(new AddChange(s));
                }

                return adds;
            }
        };
    }

    private static class AddChange extends Action<ChangePlan> {

        private final Change add;

        public AddChange(Change add) {
            super();
            this.add = add;
        }

        public ChangePlan apply(ChangePlan cp) {
            return cp.with(add);
        }
    }

    private static class RemoveChange extends Action<ChangePlan> {
        private final Change remove;

        public RemoveChange(Change remove) {
            super();
            this.remove = remove;
        }

        public ChangePlan apply(ChangePlan cp) {
            return cp.remove(remove);
        }
    }

}
