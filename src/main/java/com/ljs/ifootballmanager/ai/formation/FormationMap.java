package com.ljs.ifootballmanager.ai.formation;

import com.google.common.base.Supplier;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.player.Player;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author lstephen
 */
public class FormationMap {

    private Multimap<Role, Player> roles = Multimaps.newSetMultimap(
        Maps.<Role, Collection<Player>>newEnumMap(Role.class),
        new Supplier<Set<Player>>() {
            public Set<Player> get() {
                return Sets.<Player>newHashSet();
            }
        });

    private Map<Player, Role> players = Maps.newHashMap();

    private FormationMap() { }

    public Boolean contains(Player p) {
        return players.containsKey(p);
    }

    public Set<Role> roles() {
        return roles.keySet();
    }

    public Collection<Player> get(Role r) {
        return roles.get(r);
    }

    public Role get(Player p) {
        return players.get(p);
    }

    public void put(Role r, Player p) {
        roles.put(r, p);
        players.put(p, r);
    }

    public void remove(Role r, Player p) {
        roles.remove(r, p);
        players.remove(p);
    }

    public Integer size() {
        return roles.size();
    }

    public Set<Player> players() {
        return players.keySet();
    }

    public static FormationMap create() {
        return new FormationMap();
    }

    public static FormationMap create(FormationMap other) {
        FormationMap result = create();

        result.roles = HashMultimap.create(other.roles);
        result.players = Maps.newHashMap(other.players);

        return result;
    }

    public static FormationMap create(Multimap<Role, Player> other) {
        FormationMap result = create();

        for (Map.Entry<Role, Player> e : other.entries()) {
            result.put(e.getKey(), e.getValue());
        }

        return result;
    }


}
