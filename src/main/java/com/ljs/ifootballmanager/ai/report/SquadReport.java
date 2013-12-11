package com.ljs.ifootballmanager.ai.report;

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

    private SquadReport(Tactic tactic, Iterable<Player> squad) {
        this.tactic = tactic;
        this.squad = squad;
    }

    public void print(PrintWriter w) {
        Role[] roles = Role.values();

        w.format("%-15s ", tactic);

        w.format(" %2s %5s ", "", "OVR");

        for (Role r : roles) {
            w.format("%5s ", r.name());
        }

        w.println();

        for (Player p : Player.byOverall(tactic).reverse().immutableSortedCopy(squad)) {
            w.format("%-15s ", p.getName());

            RatingInRole best = p.getOverall(tactic);

            w.format(" %2s %5d ", best.getRole(), best.getRating());

            for (Role r : roles) {
                w.format("%5d ", p.evaluate(r, tactic).getRating());
            }

            w.println();
        }

    }

    public static SquadReport create(Tactic tactic, Iterable<Player> squad) {
        return new SquadReport(tactic, squad);
    }
}
