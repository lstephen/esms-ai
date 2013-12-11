package com.ljs.ifootballmanager.ai.selection;

import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.league.League;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.report.Report;
import com.ljs.ifootballmanager.ai.search.Action;
import com.ljs.ifootballmanager.ai.search.ActionsFunction;
import com.ljs.ifootballmanager.ai.search.PairedAction;
import com.ljs.ifootballmanager.ai.search.RepeatedHillClimbing;
import com.ljs.ifootballmanager.ai.search.State;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 *
 * @author lstephen
 */
public final class Formation implements State, Report {

    private final League league;

    private final ImmutableMultimap<Role, Player> positions;

    private final Map<Player, Role> players = Maps.newHashMap();

    private final Tactic tactic;

    private Formation(League league, Tactic tactic, Multimap<Role, Player> in) {
        this.league = league;
        this.tactic = tactic;
        this.positions = ImmutableMultimap.copyOf(in);
    }

    public League getLeague() {
        return league;
    }

    public Tactic getTactic() {
        return tactic;
    }

    public Player getPenaltyKicker() {
        return Player.byRating(Role.FW, tactic).max(players());
    }

    public ImmutableSet<Role> getRoles() {
        return positions.keySet();
    }

    public Formation move(Role r, Player p) {
        Multimap<Role, Player> f = HashMultimap.create(positions);

        f.remove(findRole(p), p);

        f.put(r, p);

        return Formation.create(league, tactic, f);
    }

    public Role findRole(Player p) {
        if (!players.containsKey(p)) {
            for (Role r : positions.keySet()) {
                if (positions.get(r).contains(p)) {
                    players.put(p, r);
                    break;
                }
            }
        }
        return players.get(p);
    }

    public boolean contains(Player p) {
        return positions.containsValue(p);
    }

    public ImmutableList<Player> players() {
        List<Player> players = Lists.newArrayList();

        for (Role r : Ordering.natural().sortedCopy(positions.keySet())) {
            for (Player p : Player.byName().sortedCopy(positions.get(r))) {
                players.add(p);
            }
        }

        return ImmutableList.copyOf(players);
    }

    private Formation substitute(Substitution s) {
        return substitute(s.getIn(), s.getRole(), s.getOut());
    }

    public Formation substitute(Player in, Role r, Player out) {
        Multimap<Role, Player> f = HashMultimap.create(positions);

        f.remove(findRole(out), out);
        f.put(r, in);

        return Formation.create(league, tactic, f);
    }

    public boolean isValid(Substitution s) {
        if (!contains(s.getOut())) {
            return false;
        }
        if (s.getRole().equals(findRole(s.getOut()))) {
            return true;
        }
        return substitute(s).isValid();
    }

    public Integer score() {
        return score(tactic);
    }

    private Integer score(Tactic tactic) {
        Integer score = 0;
        Integer ageScore = 0;

        for (Map.Entry<Role, Player> entry : positions.entries()) {
            score += entry.getValue().evaluate(entry.getKey(), tactic).getRating();
            ageScore += 50 - entry.getValue().getAge();
        }

        return score * 1000 + ageScore;
    }

    public Boolean isValid() {
        if (positions.size() != 11) {
            return false;
        }
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
        w.format("%s%n", getTactic());
        printPlayers(w);
    }

    public void printPlayers(PrintWriter w) {
        for (Player p : players()) {
            w.format("%s %s%n", findRole(p), p.getName());
        }
    }

    public static Formation create(League league, Tactic tactic, Multimap<Role, Player> players) {
        return new Formation(league, tactic, players);
    }

    public static Formation select(League league, Iterable<Player> available) {
        Set<Formation> formations = Sets.newHashSet();
        for (Tactic t : Tactic.values()) {
            formations.add(select(league, t, available));
        }

        return byScore().max(formations);
    }

    public static Formation select(League league, Tactic tactic, Iterable<Player> available) {
        return new RepeatedHillClimbing<Formation>(
            Formation.class,
            initialState(league, tactic, available),
            actionsFunction(league, available))
            .search();
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

    private static Callable<Formation> initialState(final League league, final Tactic tactic, final Iterable<Player> available) {
        return new Callable<Formation>() {
            public Formation call() {
                List<Player> shuffled = Lists.newArrayList(available);
                Collections.shuffle(shuffled);

                Multimap<Role, Player> initialState = HashMultimap.create();

                initialState.put(Role.GK, shuffled.get(0));
                initialState.putAll(Role.DF, shuffled.subList(1, 5));
                initialState.putAll(Role.MF, shuffled.subList(5, 9));
                initialState.putAll(Role.FW, shuffled.subList(9, 11));

                Formation f = create(league, tactic, initialState);

                return f;
            }};
    }

    private static ActionsFunction<Formation> actionsFunction(final League league, final Iterable<Player> available) {
        return new ActionsFunction<Formation>() {

            public ImmutableSet<Action<Formation>> getActions(Formation f) {
                ImmutableSet<Move> moves = moves(f);
                ImmutableSet<Substitute> subs = substitutions(f);
                return ImmutableSet.copyOf(Iterables.concat(moves, subs, PairedAction.all(moves), PairedAction.merged(moves, subs)));
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

    private static class Move extends Action<Formation> {

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

    private static class Substitute extends Action<Formation> {

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
