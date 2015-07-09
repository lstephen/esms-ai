package com.ljs.ifootballmanager.ai.formation.selection;

import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.formation.Formation;
import com.ljs.ifootballmanager.ai.formation.SelectionCriteria;
import com.ljs.ifootballmanager.ai.player.Player;

import com.github.lstephen.ai.search.action.Action;
import com.github.lstephen.ai.search.action.ActionGenerator;
import com.github.lstephen.ai.search.action.SequencedAction;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.stream.Stream;

/**
 *
 * @author lstephen
 */
public class Actions implements ActionGenerator<Formation> {

    private SelectionCriteria criteria;

    private Actions(SelectionCriteria criteria) {
        this.criteria = criteria;
    }

    public Stream<Action<Formation>> apply(Formation f) {
        List<Move> moves = moves(f);
        List<Substitute> subs = substitutions(f);
        return Stream.concat(
            Stream.concat(moves.stream(), subs.stream()),
            Stream.concat(SequencedAction.allPairs(moves), SequencedAction.merged(moves, subs)));
    }

    private List<Move> moves(Formation f) {
        List<Move> moves = Lists.newArrayList();

        for (Player p : f.unsortedPlayers()) {
            for (Role r : Role.values()) {
                moves.add(new Move(p, r));
            }
        }

        return moves;
    }

    private List<Substitute> substitutions(Formation f) {
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
