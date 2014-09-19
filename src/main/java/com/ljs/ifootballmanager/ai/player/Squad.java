package com.ljs.ifootballmanager.ai.player;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.io.CharSource;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.ljs.ifootballmanager.ai.Main;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.league.League;
import com.ljs.ifootballmanager.ai.rating.Ratings;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;

/**
 *
 * @author lstephen
 */
public final class Squad {

    private final ImmutableList<Player> players;

    private Squad(Iterable<Player> ps) {
        this.players = ImmutableList.copyOf(ps);
    }

    public Ordering<Player> getOrdering() {
        return Ordering.explicit(players);
    }

    public Integer getGames() {
        return Ordering
            .natural()
            .max(
                FluentIterable
                    .from(players())
                    .transform(new Function<Player, Integer>() {
                        public Integer apply(Player p) {
                            return p.getGames();
                        }
                    }));
    }

    public ImmutableSet<Player> players() {
        return ImmutableSet.copyOf(players);
    }

    public Player findPlayer(String name) {
        for (Player p : players()) {
            if (name.equals(p.getName())) {
                return p;
            }
        }
        throw new IllegalStateException("Could not find: " + name);
    }

    public ImmutableSet<Player> players(Role r, Tactic t) {
        Set<Player> ps = Sets.newHashSet();

        for (Player p : players()) {
            if (p.getOverall(t).getRole() == r) {
                ps.add(p);
            }
        }
        return ImmutableSet.copyOf(ps);
    }

    public ImmutableSet<Player> reserves(League league) {
        Set<Player> rs = Sets.newHashSet();

        for (Player p : withCap(league.getYouthSkillsCap())) {
            if (p.isReserves()) {
                rs.add(p);
            }
        }

        return ImmutableSet.copyOf(rs);
    }

    public Iterable<Player> forSelection(League league) {
        Set<Optional<Player>> ps = Sets.newHashSet();

        for (Player p : withCap(league.getSeniorSkillsCap())) {
            ps.add(p.forSelection());
        }

        return Optional.presentInstances(ps);
    }

    public Iterable<Player> forReservesSelection(League league) {
        Set<Optional<Player>> ps = Sets.newHashSet();

        Optional<Double> cap = league.getYouthSkillsCap();

        for (Player p : players) {
            if (cap.isPresent()) {
                ps.add(p.withSkillCap(cap.get()).forReservesSelection());
            } else {
                ps.add(p.forReservesSelection());
            }
        }

        return Optional.presentInstances(ps);
    }

    private Iterable<Player> withCap(Optional<Double> cap) {
        Set<Player> ps = Sets.newHashSet();

        for (Player p : players) {
            if (cap.isPresent()) {
                ps.add(p.withSkillCap(cap.get()));
            } else {
                ps.add(p);
            }
        }

        return ps;
    }

    public Integer count(Role r) {
        Integer count = 0;
        for (Player p : players()) {
            if (p.getOverall(Tactic.NORMAL).getRole().equals(r)) {
                count++;
            }
        }
        return count;
    }

    private static Squad merge(Squad... sqs) {
        Iterable<Player> ps = Collections.emptyList();

        for (Squad s : sqs) {
            ps = Iterables.concat(ps, s.players());
        }

        return new Squad(ps);
    }

    public static Squad load(League league) throws IOException {
        Squad first =  load(league, league.getTeam(), Boolean.FALSE);


        if (league.getReserveTeam().isPresent()) {
            Squad reserves = load(league, league.getReserveTeam().get(), Boolean.TRUE);

            return Squad.merge(first, reserves);
        } else {
            return first;
        }
    }

    public static Squad load(League league, CharSource source) throws IOException {
        return load(league, source, Boolean.FALSE);
    }

    public static Squad load(League league, String team) throws IOException {
        return load(league, team, Boolean.FALSE);
    }

    private static Squad load(League league, String team, Boolean reserves) throws IOException {
        CharSource teamFile =
            Resources
                .asByteSource(Main.class.getResource("/" + league.getClass().getSimpleName() + "/" + team + ".txt"))
                .asCharSource(Charsets.ISO_8859_1);

        CharStreams.copy(teamFile, Files.asCharSink(new File("c:/esms", team + ".txt"), Charsets.ISO_8859_1));

        return load(league, teamFile, reserves);
    }

    private static Squad load(League league, CharSource source, Boolean reserves) throws IOException {
        List<Player> ps = Lists.newArrayList();

        for (String line : source.readLines()) {
            if (Strings.isNullOrEmpty(line) || line.startsWith("Name") || line.startsWith("----") || line.startsWith("#")) {
                continue;
            }

            String[] split = StringUtils.split(StringUtils.substringBefore(line, "#"));

            if (split.length < 12) {
                continue;
            }

            String name = split[0];
            Integer age = Integer.parseInt(split[1]);

            Ratings ratings = Ratings
                .builder(league)
                .stopping(Integer.parseInt(split[3]))
                .tackling(Integer.parseInt(split[4]))
                .passing(Integer.parseInt(split[5]))
                .shooting(Integer.parseInt(split[6]))
                .build();

            Ratings abilities = Ratings
                .builder(league)
                .stopping(Integer.parseInt(split[8]))
                .tackling(Integer.parseInt(split[9]))
                .passing(Integer.parseInt(split[10]))
                .shooting(Integer.parseInt(split[11]))
                .build();

            Player p = Player.create(name, age, ratings, abilities);

            p.setAggression(Integer.parseInt(split[7]));

            if (split.length > 12) {
                p.setGames(Integer.parseInt(split[12]));
            }

            if (split.length > 24 && !split[24].equals("0")) {
                p.injured();
            }
            if (split.length > 25 && !split[25].equals("0")) {
                p.suspended();
            }

            if (split.length > 26) {
                p.setFitness(Integer.parseInt(split[26]));
            }

            p.setComment(StringUtils.substringAfter(line, "#"));

            if (reserves) {
                p.reserves();
            }

            ps.add(p);
        }

        return new Squad(ps);
    }

}
