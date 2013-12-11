package com.ljs.ifootballmanager.ai.report;

import com.google.common.collect.Ordering;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.value.RatingInRole;
import java.io.PrintWriter;

/**
 *
 * @author lstephen
 */
public class SquadReport implements Report {

    private final Tactic tactic;

    private final Iterable<Player> squad;

    private Ordering<Player> ordering;

    private SquadReport(Tactic tactic, Iterable<Player> squad) {
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

        w.format("%-15s ", tactic);

        w.format(" %2s %2s %5s ", "", "", "OVR");

        for (Role r : roles) {
            w.format("%5s ", r.name());
        }

        w.println();

        for (Player p : ordering.immutableSortedCopy(squad)) {
            w.format("%-15s ", p.getName());

            RatingInRole best = p.getOverall(tactic);

            w.format(" %2d %2s %5d ", p.getAge(), best.getRole(), best.getRating());

            for (Role r : roles) {
                w.format("%5d ", p.evaluate(r, tactic).getRating());
            }

            w.format(" (%5d) ", p.getValue());

            w.println();
        }

    }

    public static SquadReport create(Tactic tactic, Iterable<Player> squad) {
        return new SquadReport(tactic, squad);
    }
}
