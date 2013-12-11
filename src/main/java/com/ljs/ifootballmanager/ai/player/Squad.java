package com.ljs.ifootballmanager.ai.player;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.io.CharSource;
import com.ljs.ifootballmanager.ai.rating.Ratings;
import java.io.IOException;
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

    public Iterable<Player> players() {
        return players;
    }

    public Iterable<Player> forSelection() {
        Set<Optional<Player>> ps = Sets.newHashSet();

        for (Player p : players) {
            ps.add(p.forSelection());
        }

        return Optional.presentInstances(ps);
    }

    public static Squad load(CharSource source) throws IOException {
        Set<Player> ps = Sets.newHashSet();

        for (String line : source.readLines()) {
            if (Strings.isNullOrEmpty(line) || line.startsWith("Name") || line.startsWith("----")) {
                continue;
            }

            String[] split = StringUtils.split(line);

            String name = split[0];
            Integer age = Integer.parseInt(split[1]);

            Ratings ratings = Ratings
                .builder()
                .stopping(Integer.parseInt(split[3]))
                .tackling(Integer.parseInt(split[4]))
                .passing(Integer.parseInt(split[5]))
                .shooting(Integer.parseInt(split[6]))
                .build();

            Player p = Player.create(name, age, ratings);

            if (!split[24].equals("0")) {
                p.injured();
            }
            if (!split[25].equals("0")) {
                p.suspended();
            }

            if (split.length > 26) {
                p.setFitness(Integer.parseInt(split[26]));
            }

            ps.add(p);
        }

        return new Squad(ps);
    }

}
