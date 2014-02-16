package com.ljs.ifootballmanager.ai.selection;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.ljs.ai.search.Action;
import com.ljs.ai.search.ActionsFunction;
import com.ljs.ai.search.RepeatedHillClimbing;
import com.ljs.ai.search.State;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.formation.Formation;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.rating.Rating;
import com.ljs.ifootballmanager.ai.report.Report;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 *
 * @author lstephen
 */
public class Bench implements State, Report {

    private static final Integer BENCH_SIZE = 5;

    private final Formation formation;

    private final ImmutableSet<Player> bench;

    private Bench(Formation formation, Iterable<Player> bench) {
        this.formation = formation;
        this.bench = ImmutableSet.copyOf(bench);
    }

    @Override
    public Boolean isValid() {
        return bench.size() == BENCH_SIZE;
    }

    @Override
    public Double score() {
        if (bench.isEmpty()) {
            return 0.0;
        }

        Double score = 0.0;

        for (Role r : formation.getRoles()) {
            score += findSubstitute(r).getRating(r, formation.getTactic());
        }

        score *= 10000;

        for (Player p : bench) {
            score += p.getOverall(formation.getTactic()).getRating();
        }

        for (Player p : bench) {
            score -= ((double) p.getAge() / 1000);
        }

        return score;
    }

    private Player findSubstitute(Role r) {
        for (Player p : players()) {
            if (getRole(p) == r) {
                return p;
            }
        }

        if (r == Role.AM || r == Role.MF || r == Role.DM) {
            for (Player p : players()) {
                switch (getRole(p)) {
                    case AM:
                    case MF:
                    case DM:
                        return p;
                    default:
                        break;
                }
            }
        }

        switch (r) {
            case GK:
                return Player.bySkill(Rating.STOPPING).max(bench);
            case DF:
                return Player.bySkill(Rating.TACKLING).max(bench);
            case DM:
            case MF:
            case AM:
                return Player.bySkill(Rating.PASSING).max(bench);
            case FW:
                return Player.bySkill(Rating.SHOOTING).max(bench);
        }

        throw new IllegalStateException();
    }

    private Role getRole(Player p) {
        return p.getOverall(formation.getTactic()).getRole();
    }

    public ImmutableList<Player> players() {
        Ordering<Player> ordering = Ordering
            .natural()
            .onResultOf(new Function<Player, Role>() {
                public Role apply(Player p) {
                    return p.getOverall(formation.getTactic()).getRole();
                }
            })
            .compound(Player.byOverall(formation.getTactic()))
            .compound(Player.byTieBreak());

        return ordering.immutableSortedCopy(bench);
    }

    public void print(PrintWriter w) {
        if (bench.isEmpty()) {
            return;
        }

        printPlayers(w);

        w.println();

        for (Role r : Role.values()) {
            w.format("SUB %1$s %2$s %1$s IF INJURED %1$s%n", r, findSubstitute(r).getName());
        }
    }

    public void printPlayers(PrintWriter w) {
        for (Player p : players()) {
            w.format("%s %s%n", getRole(p), p.getName());
        }
    }

    private static Bench create(Formation formation, Iterable<Player> bench) {
        return new Bench(formation, bench);
    }

    public static Bench select(Formation formation, Iterable<Player> substitutes, Iterable<Player> available) {
        return new RepeatedHillClimbing<Bench>(
            Bench.class,
            initialState(formation, substitutes, available),
            actionsFunction(formation, substitutes, available))
            .search();

    }

    private static Callable<Bench> initialState(
        final Formation formation,
        final Iterable<Player> subsitutes,
        final Iterable<Player> available) {

        return new Callable<Bench>() {
            public Bench call() {
                List<Player> remaining = Lists.newArrayList();

                for (Player p : available) {
                    if (!(formation.contains(p) || Iterables.contains(subsitutes, p))) {
                        remaining.add(p);
                    }
                }

                Collections.shuffle(remaining);

                return Bench.create(formation, Iterables.limit(Iterables.concat(subsitutes, remaining), BENCH_SIZE));
            }};
    }

    private static ActionsFunction<Bench> actionsFunction(
        final Formation formation,
        final Iterable<Player> subsitutes,
        final Iterable<Player> available) {

        return new ActionsFunction<Bench>() {

            @Override
            public Iterable<? extends Action<Bench>> getActions(Bench state) {
                List<Player> remaining = Lists.newArrayList();

                for (Player p : available) {
                    if (!(formation.contains(p) || Iterables.contains(subsitutes, p) || state.bench.contains(p))) {
                        remaining.add(p);
                    }
                }

                Set<Action<Bench>> actions = Sets.newHashSet();

                for (Player in : remaining) {
                    for (Player out : state.bench) {
                        if (Iterables.contains(subsitutes, out)) {
                            continue;
                        }

                        actions.add(new ChangePlayer(in, out));
                    }
                }

                return actions;
            }
        };
    }

    private static class ChangePlayer extends Action<Bench> {

        private final Player in;
        private final Player out;

        public ChangePlayer(Player in, Player out) {
            this.in = in;
            this.out = out;
        }

        public Bench apply(Bench bench) {
            Set<Player> players = Sets.newHashSet(bench.bench);

            players.remove(out);
            players.add(in);

            return Bench.create(bench.formation, players);
        }
    }


}
