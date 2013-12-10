package com.ljs.ifootballmanager.ai.selection;

import aima.core.agent.Action;
import aima.core.search.framework.ActionsFunction;
import aima.core.search.framework.GoalTest;
import aima.core.search.framework.HeuristicFunction;
import aima.core.search.framework.Problem;
import aima.core.search.framework.ResultFunction;
import aima.core.search.local.HillClimbingSearch;
import com.google.common.base.Throwables;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.league.League;
import com.ljs.ifootballmanager.ai.player.Player;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author lstephen
 */
public final class Formation {

    private final ImmutableMultimap<Role, Player> positions;

    private Formation(Multimap<Role, Player> positions) {
        this.positions = ImmutableMultimap.copyOf(positions);
    }

    private Formation move(Role r, Player p) {
        Multimap<Role, Player> f = HashMultimap.create(positions);

        f.remove(findRole(p), p);

        f.put(r, p);

        return Formation.create(f);
    }

    private Role findRole(Player p) {
        for (Role r : positions.keySet()) {
            if (positions.get(r).contains(p)) {
                return r;
            }
        }

        throw new IllegalStateException();
    }

    private boolean contains(Player p) {
        return positions.containsValue(p);
    }

    private ImmutableCollection<Player> players() {
        return positions.values();
    }

    private Formation substitute(Player in, Role r, Player out) {
        Multimap<Role, Player> f = HashMultimap.create(positions);

        f.remove(findRole(out), out);
        f.put(r, in);

        return Formation.create(f);
    }

    private Integer score(League league) {
        Integer score = 0;
        Integer ageScore = 0;

        for (Map.Entry<Role, Player> entry : positions.entries()) {
            score += entry.getValue().evaluate(entry.getKey()).getRating();
            ageScore += 50 - entry.getValue().getAge();
        }

        return score * 1000 + ageScore;
    }

    private Boolean isValid(League league) {
        for (Role r : Role.values()) {
            if (count(r) < league.getMinimum(r)) {
                return false;
            }

            if (count(r) > league.getMaximum(r)) {
                return false;
            }
        }

        if (count(Role.DM) + count(Role.MF) + count(Role.AM)
            > league.getMaximum(Role.MF)) {
            return false;
        }

        return Boolean.TRUE;
    }

    private Integer count(Role r) {
        return positions.get(r).size();
    }

    public void print(PrintWriter w) {
        for (Role r : Ordering.natural().sortedCopy(positions.keySet())) {
            for (Player p : Player.byName().sortedCopy(positions.get(r))) {
                w.format("%s %s%n", r, p.getName());
            }
        }
    }

    private static Formation create(Multimap<Role, Player> players) {
        return new Formation(players);
    }

    public static Formation select(League league, Iterable<Player> available) {
        HillClimbingSearch search = new HillClimbingSearch(heuristic(league));

        try {
            search.search(problem(league, available));
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }

        return Formation.class.cast(search.getLastSearchState());
    }

    private static HeuristicFunction heuristic(final League league) {
        return new HeuristicFunction() {
            public double h(Object state) {
                Formation f = Formation.class.cast(state);

                return -f.score(league);
            }
        };
    }

    private static Problem problem(League league, Iterable<Player> available) {
        return new Problem(
            initialState(league, available),
            actionsFunction(league, available),
            resultFunction(),
            goalTest(league));
    }

    private static Formation initialState(League league, Iterable<Player> available) {
        List<Player> shuffled = Lists.newArrayList(available);
        Collections.shuffle(shuffled);

        Multimap<Role, Player> initialState = HashMultimap.create();
        for (Player p : Iterables.limit(shuffled, 11)) {
            initialState.put(p.getOverall().getRole(), p);
        }
        Formation f = create(initialState);

        return f.isValid(league) ? f : initialState(league, available);
    }

    private static ActionsFunction actionsFunction(final League league, final Iterable<Player> available) {
        return new ActionsFunction() {
            public Set<Action> actions(Object s) {
                return ImmutableSet.<Action>copyOf(actions(Formation.class.cast(s)));
            }

            public ImmutableSet<FormationAction> actions(Formation f) {
                Set<FormationAction> actions = Sets.newHashSet();

                for (FormationAction a : Iterables.concat(moves(f), substitutions(f))) {
                    if (a.isValid(league, f)) {
                        actions.add(a);
                    }
                }

                return ImmutableSet.copyOf(actions);
            }

            private ImmutableSet<Move> moves(Formation f) {
                Set<Move> moves = Sets.newHashSet();

                for (Player p : f.players()) {
                    Role current = f.findRole(p);

                    for (Role r : Role.values()) {
                        if (r != current) {
                            moves.add(new Move(p, r));
                        }
                    }
                }

                return ImmutableSet.copyOf(moves);
            }

            private ImmutableSet<Substitute> substitutions(Formation f) {
                Set<Substitute> actions = Sets.newHashSet();
                for (Player in : available) {
                    if (!f.contains(in)) {
                        for (Player out : f.players()) {
                            for (Role r : Role.values()) {
                                actions.add(new Substitute(in, r, out));
                            }
                        }
                    }
                }
                return ImmutableSet.copyOf(actions);
            }
        };
    }

    private static ResultFunction resultFunction() {
        return new ResultFunction() {
            public Formation result(Object s, Action a) {
                return FormationAction.class.cast(a).apply(Formation.class.cast(s));
            }
        };
    }

    private static GoalTest goalTest(final League league) {
        return new GoalTest() {
            public boolean isGoalState(Object state) {
                return Formation.class.cast(state).isValid(league);
            }
        };
    }

    private abstract static class FormationAction implements Action {

        @Override
        public boolean isNoOp() {
            return false;
        }

        public boolean isValid(League l, Formation f) {
            return apply(f).isValid(l);
        }

        public Formation apply(Object state) {
            return apply(Formation.class.cast(state));
        }

        public abstract Formation apply(Formation f);
    }

    private static class Move extends FormationAction {

        private final Player player;
        private final Role role;

        public Move(Player player, Role role) {
            super();
            this.player = player;
            this.role = role;
        }

        public Formation apply(Formation f) {
            return f.move(role, player);
        }
    }

    private static class Substitute extends FormationAction {

        private final Player in;
        private final Role r;
        private final Player out;

        public Substitute(Player in, Role r, Player out) {
            super();
            this.in = in;
            this.r = r;
            this.out = out;
        }

        public Formation apply(Formation f) {
            return f.substitute(in, r, out);
        }
    }

}
