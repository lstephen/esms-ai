package com.ljs.ifootballmanager.ai.report;

import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.value.RatingInRole;
import java.io.PrintWriter;

/**
 *
 * @author lstephen
 */
public class SquadReport implements Report {

    private final Iterable<Player> squad;

    private SquadReport(Iterable<Player> squad) {
        this.squad = squad;
    }


    public void print(PrintWriter w) {
        Role[] roles = Role.values();

        w.format("%-15s ", "Name");

        w.format(" %2s %5s ", "", "OVR");

        for (Role r : roles) {
            w.format("%5s ", r.name());
        }

        w.println();

        for (Player p : Player.byOverall().reverse().immutableSortedCopy(squad)) {
            w.format("%-15s ", p.getName());

            RatingInRole best = p.getOverall();

            w.format(" %2s %5d ", best.getRole(), best.getRating());

            for (Role r : roles) {
                w.format("%5d ", p.evaluate(r).getRating());
            }


            w.println();
        }

    }

    public static SquadReport create(Iterable<Player> squad) {
        return new SquadReport(squad);
    }
}
