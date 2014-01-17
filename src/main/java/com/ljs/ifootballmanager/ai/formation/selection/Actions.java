package com.ljs.ifootballmanager.ai.formation.selection;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.ljs.ai.search.hillclimbing.action.Action;
import com.ljs.ai.search.hillclimbing.action.ActionGenerator;
import com.ljs.ai.search.hillclimbing.action.SequencedAction;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.formation.Formation;
import com.ljs.ifootballmanager.ai.formation.SelectionCriteria;
import com.ljs.ifootballmanager.ai.player.Player;
import java.util.Set;

/**
 *
 * @author lstephen
 */
public class Actions implements ActionGenerator<Formation> {

    private SelectionCriteria criteria;

    private Actions(SelectionCriteria criteria) {
        this.criteria = criteria;
    }

    public ImmutableSet<Action<Formation>> apply(Formation f) {
        ImmutableSet<Move> moves = moves(f);
        ImmutableSet<Substitute> subs = substitutions(f);
        return ImmutableSet.copyOf(Iterables.concat(moves, subs, SequencedAction.allPairs(moves), SequencedAction.merged(moves, subs)));
    }

    private ImmutableSet<Move> moves(Formation f) {
        Set<Move> moves = Sets.newHashSet();

        for (Player p : f.unsortedPlayers()) {
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
        for (Player in : criteria.getAll()) {
            if (!f.contains(in)) {
                for (Player out : f.unsortedPlayers()) {
                    if (criteria.isRequired(out)) {
                        continue;
                    }
                    for (Role r : Role.values()) {
                        actions.add(new Substitute(in, r, out));
                    }
                }
            }
        }
        return ImmutableSet.copyOf(actions);
    }

    public static Actions create(SelectionCriteria sc) {
        return new Actions(sc);
    }

}
