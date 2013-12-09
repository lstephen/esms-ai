package com.ljs.ifootballmanager.ai.selection;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
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
public final class Starters implements Iterable<Player> {

    private final ImmutableSet<Player> players;

    private Starters(Iterable<Player> players) {
        this.players = ImmutableSet.copyOf(players);
    }

    public Iterator<Player> iterator() {
        return players.iterator();
    }

    public static Starters select(Iterable<Player> available) {
        return new Starters(Selector.create().select(available));
    }

    private static final class Selector {

        private static final ImmutableMultiset<Role> REQUIRED =
            ImmutableMultiset.of(Role.GK, Role.DF, Role.DF, Role.DF);

        private static final ImmutableMultiset<Role> AVAILABLE =
            ImmutableMultiset
            .<Role>builder()
            .addCopies(Role.GK, 1)
            .addCopies(Role.DF, 5)
            .addCopies(Role.MF, 7)
            .addCopies(Role.AM, 5)
            .addCopies(Role.FW, 4)
            .build();

        private Selector() { }

        public ImmutableSet<Player> select(Iterable<Player> available) {
            Set<Player> selected = Sets.newHashSet();

            selected.addAll(selectRequired(available));

            selected.addAll(selectAvailable(11 - selected.size(), Sets
                .difference(
                ImmutableSet.copyOf(available), selected)));


            return ImmutableSet.copyOf(selected);
        }

        private ImmutableSet<Player> selectRequired(Iterable<Player> available) {
            Set<Player> selected = Sets.newHashSet();

            for (Role r : REQUIRED) {
                selected.add(
                    Selections.select(
                        r,
                        Sets.difference(
                            ImmutableSet.copyOf(available), selected)));
            }

            return ImmutableSet.copyOf(selected);
        }

        private ImmutableSet<Player> selectAvailable(
            Integer n, Iterable<Player> available) {

            Set<InRole> players = Sets.newHashSet();

            for (Player p : available) {
                players.addAll(p.inAllRoles());
            }

            Set<Player> selected = Sets.newHashSet();

            Multiset<Role> availableRoles = HashMultiset.create(
                Multisets.difference(AVAILABLE, REQUIRED));

            for (InRole p : InRole.byRating().reverse().sortedCopy(players)) {
                if (selected.size() >= n) {
                    break;
                }

                if (selected.contains(p.getPlayer())) {
                    continue;
                }

                if (availableRoles.contains(p.getRole())) {
                    selected.add(p.getPlayer());
                    availableRoles.remove(p.getRole());
                }
            }

            return ImmutableSet.copyOf(selected);
        }

        

        public static Selector create() {
            return new Selector();
        }
    }

}
