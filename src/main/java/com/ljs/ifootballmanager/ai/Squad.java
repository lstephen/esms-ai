package com.ljs.ifootballmanager.ai;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.io.CharSource;
import com.ljs.ifootballmanager.ai.player.Player;
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

    public static Squad load(CharSource source) throws IOException {
        Set<Player> ps = Sets.newHashSet();

        for (String line : source.readLines()) {
            if (line.startsWith("Name") || line.startsWith("----")) {
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

            ps.add(Player.create(name, age, ratings));
        }

        return new Squad(ps);
    }

}
