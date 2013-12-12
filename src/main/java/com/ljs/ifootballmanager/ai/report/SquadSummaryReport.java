package com.ljs.ifootballmanager.ai.report;

import com.google.common.collect.ImmutableSet;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.player.Squad;
import com.ljs.ifootballmanager.ai.selection.Formation;
import java.io.PrintWriter;

/**
 *
 * @author lstephen
 */
public class SquadSummaryReport implements Report {

    private final Squad squad;

    private final Formation firstXI;

    private final Formation secondXI;

    private SquadSummaryReport(Squad squad, Formation firstXI, Formation secondXI) {
        this.squad = squad;
        this.firstXI = firstXI;
        this.secondXI = secondXI;
    }


    public void print(PrintWriter w) {
        Role[] roles = Role.values();

        w.format("%28s ", "");
        for (Role r : roles) {
            w.format("%5s ", r.name());
        }
        w.println();

        w.format("%28s ", "Count");
        for (Role r : roles) {
            w.format("%5s ", squad.count(r));
        }
        w.println();

        w.format("%28s ", "Max");
        for (Role r : roles) {
            w.format("%5s ", Player.byRating(r, Tactic.NORMAL).max(squad.players()).getRating(r, Tactic.NORMAL));
        }
        w.println();

        w.format("%28s ", "1st XI");
        for (Role r : roles) {
            ImmutableSet<Player> ps = firstXI.players(r);

            w.format("%5s ", ps.isEmpty() ? "" : Player.byRating(r, Tactic.NORMAL).min(ps).getRating(r, Tactic.NORMAL));
        }
        w.println();

        if (secondXI != null) {
            w.format("%28s ", "2nd XI");
            for (Role r : roles) {
                ImmutableSet<Player> ps = secondXI.players(r);

                w.format("%5s ", ps.isEmpty() ? "" : Player.byRating(r, Tactic.NORMAL).min(ps).getRating(r, Tactic.NORMAL));
            }
        }
        w.println();

        w.format("%28s%n", "Min");

        for (Tactic t : Tactic.values()) {
            w.format("%28s ", t);
            for (Role r : roles) {
                ImmutableSet<Player> ps = squad.players(r, t);
                w.format("%5s ", ps.isEmpty() ? "" : Player.byRating(r, t).min(ps).getRating(r, t));
            }
            w.println();
        }
    }

    public static SquadSummaryReport create(Squad squad, Formation firstXI, Formation secondXI) {
        return new SquadSummaryReport(squad, firstXI, secondXI);
    }

}
