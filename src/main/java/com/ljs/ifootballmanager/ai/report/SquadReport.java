package com.ljs.ifootballmanager.ai.report;

import com.google.common.collect.Ordering;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.league.League;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.value.RatingInRole;
import java.io.PrintWriter;

/**
 *
 * @author lstephen
 */
public class SquadReport implements Report {

    private final League league;

    private final Tactic tactic;

    private final Iterable<Player> squad;

    private Ordering<Player> ordering;

    private SquadReport(League league, Tactic tactic, Iterable<Player> squad) {
        this.league = league;
        this.tactic = tactic;
        this.squad = squad;

        ordering = Player.byOverall(tactic).reverse();
    }

    public SquadReport sortByValue() {
        ordering = Player.byValue().reverse();
        return this;
    }

    public void print(PrintWriter w) {
        Role[] roles = Role.values();
        Tactic[] tactics = Tactic.values();

        w.format("%-15s ", tactic);

        w.format(" %2s %2s %5s ", "", "", "OVR");

        for (Role r : roles) {
            w.format("%3s ", r.name());
        }

        w.format(" (%3s) ", "VAL");

        for (Tactic t : tactics) {
            w.format("%3s ", t.getCode());
        }

        w.println();

        for (Player p : ordering.immutableSortedCopy(squad)) {
            w.format("%-15s ", p.getName());

            RatingInRole best = p.getOverall(tactic);

            w.format("%1s%2d %2s %5d ",
                league.isReserveEligible(p) ? "R" : "",
                p.getAge(),
                best.getRole(),
                Math.round((double) best.getRating() / 100));

            for (Role r : roles) {
                w.format("%3d ", Math.round((double) p.evaluate(r, tactic).getRating() / 100));
            }

            w.format(" (%3d) ", Math.round((double) p.getValue() / 100));

            for (Tactic t : tactics) {
                w.format("%3d ", Math.round((double) p.getOverall(t).getRating() / 100));
            }

            w.format("%s", p.getComment());

            w.println();
        }

    }

    public static SquadReport create(League league, Tactic tactic, Iterable<Player> squad) {
        return new SquadReport(league, tactic, squad);
    }
}
