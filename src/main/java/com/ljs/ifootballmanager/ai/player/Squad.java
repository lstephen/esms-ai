package com.ljs.ifootballmanager.ai.player;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
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
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author lstephen
 */
public final class Squad {

    private final ImmutableSet<Player> players;

    private Squad(Iterable<Player> ps) {
        this.players = ImmutableSet.copyOf(ps);
    }

    public ImmutableSet<Player> players() {
        return players;
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

    public ImmutableSet<Player> reserves() {
        Set<Player> rs = Sets.newHashSet();

        for (Player p : players()) {
            if (p.isReserves()) {
                rs.add(p);
            }
        }

        return ImmutableSet.copyOf(rs);
    }

    public Iterable<Player> forSelection() {
        Set<Optional<Player>> ps = Sets.newHashSet();

        for (Player p : players) {
            ps.add(p.forSelection());
        }

        return Optional.presentInstances(ps);
    }

    public Iterable<Player> forReservesSelection() {
        Set<Optional<Player>> ps = Sets.newHashSet();

        for (Player p : players) {
            ps.add(p.forReservesSelection());
        }

        return Optional.presentInstances(ps);
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
        Set<Player> ps = Sets.newHashSet();

        for (String line : source.readLines()) {
            if (Strings.isNullOrEmpty(line) || line.startsWith("Name") || line.startsWith("----") || line.startsWith("#")) {
                continue;
            }

            String[] split = StringUtils.split(line);

            String name = split[0];
            Integer age = Integer.parseInt(split[1]);

            Ratings ratings = Ratings
                .builder()
                .league(league)
                .stopping(Integer.parseInt(split[3]))
                .tackling(Integer.parseInt(split[4]))
                .passing(Integer.parseInt(split[5]))
                .shooting(Integer.parseInt(split[6]))
                .build();

            Player p = Player.create(name, age, ratings);

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
