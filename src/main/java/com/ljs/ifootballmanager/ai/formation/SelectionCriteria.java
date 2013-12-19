package com.ljs.ifootballmanager.ai.formation;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.ljs.ifootballmanager.ai.league.League;
import com.ljs.ifootballmanager.ai.player.Player;
import java.util.Set;

/**
 *
 * @author lstephen
 */
public final class SelectionCriteria {

    private final ImmutableSet<Player> forced;

    private final ImmutableSet<Player> all;

    private SelectionCriteria(Iterable<Player> forced, Iterable<Player> all) {
        this.forced = ImmutableSet.copyOf(forced);
        this.all = ImmutableSet.copyOf(all);
    }

    public ImmutableSet<Player> getAll() {
        return all;
    }

    public ImmutableSet<Player> getRequired() {
        return forced;
    }

    public Boolean isRequired(Player p) {
        return forced.contains(p);
    }

    public ImmutableSet<Player> getOptional() {
        return ImmutableSet.copyOf(Sets.difference(all, forced));
    }

    public static SelectionCriteria create(League league, Iterable<Player> all) {
        Set<Player> forced = Sets.newHashSet();

        for (Player p : all) {
            if (Iterables.contains(league.getForcedPlay(), p.getName())) {
                forced.add(p);
            }
        }

        return new SelectionCriteria(forced, all);
    }



}
