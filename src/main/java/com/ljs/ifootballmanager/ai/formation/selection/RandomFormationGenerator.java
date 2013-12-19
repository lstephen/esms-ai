package com.ljs.ifootballmanager.ai.formation.selection;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.formation.Formation;
import com.ljs.ifootballmanager.ai.formation.SelectionCriteria;
import com.ljs.ifootballmanager.ai.formation.score.FormationScorer;
import com.ljs.ifootballmanager.ai.formation.validate.FormationValidator;
import com.ljs.ifootballmanager.ai.player.Player;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

/**
 *
 * @author lstephen
 */
public final class RandomFormationGenerator implements Callable<Formation> {

    private final FormationValidator validator;

    private final FormationScorer scorer;

    private final Tactic tactic;

    private final SelectionCriteria criteria;

    private RandomFormationGenerator(
        FormationValidator validator,
        FormationScorer scorer,
        Tactic tactic,
        SelectionCriteria criteria) {

        this.validator = validator;
        this.scorer = scorer;
        this.tactic = tactic;
        this.criteria = criteria;
    }

    public Formation call() {
        List<Player> shuffled = Lists.newArrayList(criteria.getOptional());
        Collections.shuffle(shuffled);

        shuffled.addAll(0, criteria.getRequired());

        if (shuffled.size() >= 11) {

            Multimap<Role, Player> initialState = HashMultimap.create();

            initialState.put(Role.GK, shuffled.get(0));
            initialState.putAll(Role.DF, shuffled.subList(1, 5));
            initialState.putAll(Role.MF, shuffled.subList(5, 9));
            initialState.putAll(Role.FW, shuffled.subList(9, 11));

            return Formation.create(validator, scorer, tactic, initialState);
        } else {
            Multimap<Role, Player> initialState = HashMultimap.create();

            for (Player p : shuffled) {
                initialState.put(p.getOverall(tactic).getRole(), p);
            }

            return Formation.create(validator, scorer, tactic, initialState);
        }
    }

    public static RandomFormationGenerator create(FormationValidator validator, FormationScorer scorer, Tactic tactic, SelectionCriteria criteria) {
        return new RandomFormationGenerator(validator, scorer, tactic, criteria);
    }


}
