package com.ljs.ifootballmanager.ai.value;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.formation.Formation;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.player.Squad;
import java.util.List;

/**
 *
 * @author lstephen
 */
public class ReplacementLevel {

    private Squad squad;

    private Formation firstXI;

    private ReplacementLevel(Squad squad, Formation firstXI) {
        this.squad = squad;
        this.firstXI = firstXI;
    }

    public Double getValueVsReplacement(Player p) {
        List<Double> values = Lists.newArrayList();

        for (Role r : Role.values()) {
            values.add(getValueVsReplecement(p, r));
        }

        return Ordering.natural().max(values);
    }

    private Double getValueVsReplecement(Player p, Role r) {
        return p.evaluate(r, Tactic.NORMAL).getRating() - getReplacementLevel(r, Tactic.NORMAL);
    }

    public Double getReplacementLevel(Role role, Tactic t) {
        ImmutableSet<Player> replacements =
            ImmutableSet.copyOf(Sets.difference(squad.players(), ImmutableSet.copyOf(firstXI.players())));

        return Player
            .byRating(role, t)
            .max(replacements)
            .getRating(role, t);
    }

    public static ReplacementLevel create(Squad squad, Formation firstXI) {
        return new ReplacementLevel(squad, firstXI);
    }

}
