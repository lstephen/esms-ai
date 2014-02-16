package com.ljs.ifootballmanager.ai.formation.selection;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.ljs.ai.search.hillclimbing.action.Action;
import com.ljs.ai.search.hillclimbing.action.ActionGenerator;
import com.ljs.ai.search.hillclimbing.action.SequencedAction;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.formation.Formation;
import com.ljs.ifootballmanager.ai.formation.SelectionCriteria;
import com.ljs.ifootballmanager.ai.player.Player;
import java.util.List;

/**
 *
 * @author lstephen
 */
public class Actions implements ActionGenerator<Formation> {

    private SelectionCriteria criteria;

    private Actions(SelectionCriteria criteria) {
        this.criteria = criteria;
    }

    public Iterable<Action<Formation>> apply(Formation f) {
        Iterable<Move> moves = moves(f);
        Iterable<Substitute> subs = substitutions(f);
        return Iterables.concat(moves, subs, SequencedAction.allPairs(moves), SequencedAction.merged(moves, subs));
    }

    private Iterable<Move> moves(Formation f) {
        List<Move> moves = Lists.newArrayList();

        for (Player p : f.unsortedPlayers()) {
            for (Role r : Role.values()) {
                moves.add(new Move(p, r));
            }
        }

        return moves;
    }

    private Iterable<Substitute> substitutions(Formation f) {
        List<Substitute> actions = Lists.newArrayList();
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
        return actions;
    }

    public static Actions create(SelectionCriteria sc) {
        return new Actions(sc);
    }

}
