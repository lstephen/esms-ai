package com.ljs.ifootballmanager.ai.selection;

import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.ljs.ai.search.hillclimbing.HillClimbing;
import com.ljs.ai.search.hillclimbing.RepeatedHillClimbing;
import com.ljs.ai.search.hillclimbing.Validator;
import com.ljs.ai.search.hillclimbing.action.Action;
import com.ljs.ai.search.hillclimbing.action.ActionGenerator;
import com.ljs.ai.search.hillclimbing.action.SequencedAction;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.formation.Formation;
import com.ljs.ifootballmanager.ai.formation.SelectionCriteria;
import com.ljs.ifootballmanager.ai.league.League;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.report.Report;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 *
 * @author lstephen
 */
public final class ChangePlan implements Report {

    private final Formation formation;

    private final ImmutableSet<Change> changes;

    private final Map<Integer, Double> scores;

    private ChangePlan(Formation formation, Iterable<? extends Change> cs, Map<Integer, Double> scores) {
        this.formation = formation;
        this.changes = ImmutableSet.copyOf(cs);
        this.scores = Maps.newHashMap(scores);
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

        return ImmutableList.copyOf(Change.Meta.byMinute().immutableSortedCopy(cs));
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

    public Boolean isChangeAt(Integer minute) {
        for (Change c : changes()) {
            if (c.getMinute().equals(minute)) {
                return true;
            }
        }
        return false;
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

    public Tactic getBestScoringTactic() {
        return Ordering
            .natural()
            .onResultOf(new Function<Tactic, Integer>() {
                public Integer apply(Tactic t) {
                    return scoring(t);
                }
            })
            .max(Arrays.asList(Tactic.values()));
    }

    public Tactic getBestDefensiveTactic() {
        return Ordering
            .natural()
            .onResultOf(new Function<Tactic, Integer>() {
                public Integer apply(Tactic t) {
                    return defending(t);
                }
            })
            .max(Arrays.asList(Tactic.values()));
    }

    private Integer scoring(Tactic t) {
        Double score = 0.0;
        for (Integer minute = 1; minute <= 90; minute++) {
            Formation f = getFormationAt(minute);
            score += ((f.scoring(t) + f.score(t)) * minute / 90);
        }
        return score.intValue();
    }

    private Integer defending(Tactic t) {
        Double score = 0.0;
        for (Integer minute = 1; minute <= 90; minute++) {
            Formation f = getFormationAt(minute);
            score += ((f.defending(t) + f.score(t)) * minute / 90);
        }
        return score.intValue();
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

        return new ChangePlan(formation, cs, scoresBefore(c.getMinute()));
    }

    private ChangePlan remove(Change c) {
        Set<Change> cs = Sets.newHashSet(changes);
        cs.remove(c);
        return new ChangePlan(formation, cs, scoresBefore(c.getMinute()));
    }

    private Map<Integer, Double> scoresBefore(Integer minute) {
        Map<Integer, Double> scores = Maps.newHashMap();

        for (int i = 0; i < minute; i++) {
            if (scores.containsKey(i)) {
                scores.put(i, this.scores.get(i));
            }
        }

        return scores;
    }

    public Boolean isValid() {
        // Max is 15, but we reserve 6 for injury backups, and 3 for score based tactics
        if (changes().size() > 6) {
            return false;
        }

        if (changes(Substitution.class).size() > 3) {
            return false;
        }

        Set<Integer> minutes = Sets.newHashSet();

        for (Change c : changes) {
            if (!c.isValid(this)) {
                return false;
            }
            minutes.add(c.getMinute());
        }

        if (minutes.size() != changes.size()) {
            return false;
        }

        return true;
    }

    public Double score() {
        Double score = 0.0;

        for (Integer minute = 0; minute <= 90; minute++) {
            score += (score(minute) * minute / 90);
        }

        return score - changes.size();
    }

    public Double score(Integer minute) {
        if (scores.containsKey(minute)) {
            return scores.get(minute);
        }

        Double score = getFormationAt(minute).score();

        scores.put(minute, score);

        return score;
    }

    public Formation getFormationAt(Integer minute) {
        Multimap<Role, Player> players = HashMultimap.create();

        for (Player p : formation.players()) {
            players.put(formation.findRole(p), p.afterMinutes(minute));
        }

        Formation f = Formation.create(formation.getValidator(), formation.getTactic(), players);

        for (Change c : changesMadeAt(minute)) {
            f = c.apply(f, minute);
        }

        return f;
    }

    public static ChangePlan select(League league, final Formation f, final Iterable<Player> squad) {
        return select(league, f, SelectionCriteria.create(league, squad));

    }
    public static ChangePlan select(League league, final Formation f, final SelectionCriteria criteria) {
        HillClimbing.Builder<ChangePlan> builder = HillClimbing
            .<ChangePlan>builder()
            .validator(new Validator<ChangePlan>() {
                public Boolean apply(ChangePlan cp) {
                    return cp.isValid();
                }
            })
            .heuristic(Ordering
                .natural()
                .onResultOf(new Function<ChangePlan, Double>() {
                    public Double apply(ChangePlan cp) {
                        return cp.score();
                    }
                }))
            .actionGenerator(actionsFunction(league, criteria));


        return new RepeatedHillClimbing<ChangePlan>(
            new Callable<ChangePlan>() {
                public ChangePlan call() {
                    return randomChangePlan(f, criteria);
                }
            },
            builder)
            .search();
    }

    private static ChangePlan randomChangePlan(Formation f, SelectionCriteria criteria) {
        Random rng = new Random();

        Set<Change> changes = Sets.newHashSet();

        List<Player> starters = Lists.newArrayList(f.players());
        List<Player> all = Lists.newArrayList(criteria.getAll());

        Collections.shuffle(starters);
        Collections.shuffle(all);

        List<Integer> minutes = Lists.newArrayList();
        for(int minute = 1; minute < 90; minute++) {
            minutes.add(minute);
        }

        Collections.shuffle(minutes);

        for (int i = 0; i < 3; i++) {
            if (all.isEmpty() || all.size() <= starters.size()) {
                break;
            }
            Player out = starters.get(i);
            if (criteria.isRequired(out)) {
                continue;
            }

            Player in = all.remove(0);
            while (starters.contains(in)) {
                in = all.remove(0);
            }
            Substitution s = Substitution
                .builder()
                .in(in, f.findRole(out))
                .out(out)
                .minute(minutes.remove(0))
                .build();

            changes.add(s);
        }

        return new ChangePlan(f, changes, ImmutableMap.<Integer, Double>of());
    }

    private static ActionGenerator<ChangePlan> actionsFunction(final League league, final SelectionCriteria criteria) {
        return new ActionGenerator<ChangePlan>() {

            @Override
            public Iterable<Action<ChangePlan>> apply(ChangePlan cp) {
                List<Action<ChangePlan>> actions = Lists.newArrayList();

                ImmutableSet<RemoveChange> removes = ImmutableSet.copyOf(removes(cp));
                ImmutableSet<Action<ChangePlan>> adds = ImmutableSet.copyOf(adds(cp));

                actions.addAll(adds);
                actions.addAll(removes);

                actions.addAll(SequencedAction.merged(removes, adds));

                actions.addAll(combines(cp));

                return actions;
            }

            private Set<Substitution> availableSubstitutions(ChangePlan cp) {
                Set<Substitution> ss = Sets.newHashSet();

                for (Role r : Role.values()) {
                    if (r == Role.GK) {
                        continue;
                    }
                    for (Integer minute = 45; minute <= 90; minute++) {
                        if (cp.isChangeAt(minute)) {
                            continue;
                        }
                        Formation currentFormation = cp.getFormationAt(minute);
                        for (Player in : criteria.getAll()) {
                            if (cp.formation.contains(in)) {
                                continue;
                            }

                            for (Player out : currentFormation.players()) {
                                if (currentFormation.findRole(out) == Role.GK) {
                                    continue;
                                }
                                if (criteria.isRequired(out)) {
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

            private Set<Action<ChangePlan>> adds(ChangePlan cp) {
                Set<Action<ChangePlan>> adds = Sets.newHashSet();

                for (Substitution s : availableSubstitutions(cp)) {
                    adds.add(new AddChange(s));
                }

                for (Integer minute = 45; minute <= 90; minute++) {
                    if (cp.isChangeAt(minute)) {
                        continue;
                    }
                    Formation f = cp.getFormationAt(minute);
                    for (Tactic t : Tactic.values()) {
                        if (t != f.getTactic()) {
                            adds.add(new AddChange(TacticChange.create(t, minute)));
                        }
                    }

                    for (Player p : f.players()) {
                        for (Role r : Role.values()) {
                            if (r != f.findRole(p) && r != Role.GK) {
                                adds.add(new AddChange(ChangePosition.create(p, r, minute)));
                            }
                        }
                    }
                }

                return adds;
            }

            private Set<Action<ChangePlan>> combines(ChangePlan cp) {
                Set<Action<ChangePlan>> actions = Sets.newHashSet();

                ImmutableList<Substitution> subs = ImmutableList.copyOf(cp.changes(Substitution.class));

                for (int i = 0; i < subs.size(); i++) {
                    for (int j = i + 1; j < subs.size(); j++) {
                        Substitution lhs = subs.get(i);
                        Substitution rhs = subs.get(j);

                        if (lhs.getIn().equals(rhs.getOut())) {
                            SequencedAction removes =
                                SequencedAction.create(
                                    new RemoveChange(lhs),
                                    new RemoveChange(rhs));

                            Substitution atLhs = Substitution
                                .builder()
                                .in(rhs.getIn(), rhs.getRole())
                                .out(lhs.getOut())
                                .minute(lhs.getMinute())
                                .build();

                            Substitution atRhs = Substitution
                                .builder()
                                .in(rhs.getIn(), rhs.getRole())
                                .out(rhs.getOut())
                                .minute(rhs.getMinute())
                                .build();


                            actions.add(SequencedAction.create(removes, new AddChange(atLhs)));
                            actions.add(SequencedAction.create(removes, new AddChange(atRhs)));
                        }

                    }
                }

                return actions;
            }
        };
    }

    private static class AddChange implements Action<ChangePlan> {

        private final Change add;

        public AddChange(Change add) {
            super();
            this.add = add;
        }

        public ChangePlan apply(ChangePlan cp) {
            return cp.with(add);
        }
    }

    private static class RemoveChange implements Action<ChangePlan> {
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
