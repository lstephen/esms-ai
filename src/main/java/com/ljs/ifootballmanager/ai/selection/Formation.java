package com.ljs.ifootballmanager.ai.selection;

import aima.core.agent.Action;
import aima.core.search.framework.ActionsFunction;
import aima.core.search.framework.GoalTest;
import aima.core.search.framework.HeuristicFunction;
import aima.core.search.framework.Problem;
import aima.core.search.framework.ResultFunction;
import aima.core.search.local.HillClimbingSearch;
import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.player.InRole;
import com.ljs.ifootballmanager.ai.player.Player;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author lstephen
 */
public class Formation implements Iterable<InRole> {

    private final ImmutableSet<InRole> players;

    private Formation(Iterable<InRole> players) {
        this.players = ImmutableSet.copyOf(players);
    }

    public Iterator<InRole> iterator() {
        return players.iterator();
    }

    private Formation move(Player p, Role r) {
        Set<InRole> ps = Sets.newHashSet(players);

        for (InRole rs : players) {
            if (rs.getPlayer().equals(p)) {
                ps.remove(rs);
                break;
            }
        }

        ps.add(p.inRole(r));

        return Formation.create(ps);
    }

    private Integer score() {
        Integer score = 0;

        for (InRole pir : this) {
            score += pir.getRating();
        }

        return score;
    }

    public Boolean isValid() {
        return count(Role.GK) == 1
            && count(Role.DF) >= 3
            && count(Role.DF) <= 5
            && count(Role.DM) <= 5
            && count(Role.MF) <= 7
            && count(Role.AM) <= 5
            && count(Role.FW) <= 4;
    }

    private Integer count(Role r) {
        Integer count = 0;

        for (InRole pir : this) {
            if (pir.getRole().equals(r)) {
                count++;
            }
        }

        return count;
    }

    private static Ordering<Formation> byScore() {
        return Ordering
            .natural()
            .onResultOf(new Function<Formation, Integer>() {
                public Integer apply(Formation f) {
                    return f.score();
                }
            });
    }

    private static Formation create(Iterable<InRole> players) {
        return new Formation(players);
    }

    public static Formation select(Starters starters) {
        Problem p = problem(starters);

        HillClimbingSearch search = new HillClimbingSearch(heuristic());

        try {
            search.search(p);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }

        return Formation.class.cast(search.getLastSearchState());
    }

    private static HeuristicFunction heuristic() {
        return new HeuristicFunction() {
            public double h(Object o) {
                return -Formation.class.cast(o).score();
            }
        };
    }

    private static Problem problem(Iterable<Player> ps) {
        return new Problem(initialState(ps), actionsFunction(), resultFunction(), goalTest());
    }

    private static Formation initialState(Iterable<Player> ps) {
        Set<InRole> rs = Sets.newHashSet();

        for (Player p : ps) {
            rs.add(p.inRole(Role.MF));
        }

        return Formation.create(rs);
    }

    private static GoalTest goalTest() {
        return new GoalTest() {
            public boolean isGoalState(Object o) {
                return Formation.class.cast(o).isValid();
            }
        };
    }

    private static ActionsFunction actionsFunction() {
        return new ActionsFunction() {
            public Set<Action> actions(Object s) {
                return Sets.<Action>newHashSet(actions(Formation.class.cast(s)));
            }

            public Set<MovePlayer> actions(Formation f) {
                boolean currentIsValid = f.isValid();

                Set<MovePlayer> as = Sets.newHashSet();

                for (InRole pir : f) {
                    for (Role r : Role.values()) {
                        if (r.equals(pir.getRole())) {
                            continue;
                        }

                        MovePlayer action = new MovePlayer(pir.getPlayer(), r);

                        if (!currentIsValid || action.apply(f).isValid()) {
                            as.add(action);
                        }
                    }
                }

                return as;
            }
        };
    }

    private static ResultFunction resultFunction() {
        return new ResultFunction() {
            public Formation result(Object s, Action a) {
                return MovePlayer.class.cast(a).apply(Formation.class.cast(s));
            }};
    }

    private static class MovePlayer implements Action {

        private final Player p;

        private final Role to;

        public MovePlayer(Player p, Role to) {
            this.p = p;
            this.to = to;
        }

        public boolean isNoOp() { return false; }

        public Formation apply(Formation f) {
            return f.move(p, to);
        }
    }

}
