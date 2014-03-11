package com.ljs.ifootballmanager.ai.formation.score;

import com.google.common.collect.Sets;
import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.formation.Formation;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.player.Squad;
import com.ljs.ifootballmanager.ai.rating.Rating;
import java.io.PrintWriter;
import java.util.Set;

/**
 * There is a bug in here, in that it will cap the fatigued value,
 * rather than capping the initial value and having fatigue applied there.
 *
 * Perhaps this can be fixed by accepting the original squad. We then look
 * up the original player, and calculate a fatigue factor. Then we can apply
 * the cap to the original player * fatigue factor and calculate the score.
 *
 * @author lstephen
 */
public final class CappedScorer implements FormationScorer {

    private final Squad squad;

    private final Double cap;

    private CappedScorer(Squad squad, Double cap) {
        this.squad = squad;
        this.cap = cap;
    }

    public Double score(Formation f, Tactic t) {
        return DefaultScorer.get().score(capped(f), t);
    }

    public Double scoring(Formation f, Tactic t) {
        return DefaultScorer.get().scoring(capped(f), t);
    }

    public Double defending(Formation f, Tactic t) {
        return DefaultScorer.get().defending(capped(f), t);
    }

    public void print(Formation f, PrintWriter w) { }

    private Formation capped(Formation f) {
        Double fatigue = 1.0;

        for (Player p : f.players()) {

            Player o = squad.findPlayer(p.getName());
            Double ft = p.getSkill(Rating.TACKLING) / o.getSkill(Rating.TACKLING);

            if (ft < fatigue) {
                fatigue = ft;
            }
        }

        Set<Player> updated = Sets.newHashSet();
        for (Player p : f.unsortedPlayers()) {
            updated.add(p.withSkillCap(cap * fatigue));
        }
        return f.withUpdatedPlayers(updated);
    }

    public static CappedScorer create(Squad squad, Double cap) {
        return new CappedScorer(squad, cap);
    }
}
