package com.ljs.ifootballmanager.ai.selection;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.player.Player;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author lstephen
 */
public class Bench implements Iterable<Player> {

    private ImmutableSet<Player> bench;

    private Bench(Iterable<Player> bench) {
        this.bench = ImmutableSet.copyOf(bench);
    }

    public Iterator<Player> iterator() {
        return bench.iterator();
    }

    public static Bench select(Iterable<Player> available) {
        Set<Player> bench = Sets.newHashSet();

        for (Player p : Player.byOverall().sortedCopy(available)) {
            if (p.getOverall().getRole() != Role.GK) {
                bench.add(p);
                break;
            }
        }

        for (Role r : ImmutableSet.of(Role.GK, Role.DF, Role.MF, Role.FW)) {
            bench.add(Selections.select(r, Sets.difference(ImmutableSet.copyOf(available), bench)));
        }

        return new Bench(bench);
    }

}
