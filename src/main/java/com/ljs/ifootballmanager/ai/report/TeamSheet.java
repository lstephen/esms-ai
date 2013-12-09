package com.ljs.ifootballmanager.ai.report;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.Squad;
import com.ljs.ifootballmanager.ai.player.InRole;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.selection.Bench;
import com.ljs.ifootballmanager.ai.selection.Formation;
import com.ljs.ifootballmanager.ai.selection.Selections;
import com.ljs.ifootballmanager.ai.selection.Starters;
import java.io.PrintWriter;

/**
 *
 * @author lstephen
 */
public class TeamSheet implements Report {

    private final String team;

    private final Squad squad;

    private TeamSheet(String team, Squad squad) {
        this.team = team;
        this.squad = squad;
    }

    public void print(PrintWriter w) {
        w.println(team);
        w.println("N");
        w.println();

        Starters starters = Starters.select(squad.players());

        for (InRole p : InRole.byRole().sortedCopy(Formation.select(starters))) {
            w.format("%s %s%n", p.getRole(), p.getName());
        }

        w.println();

        for (Player p : Bench.select(Sets.difference(ImmutableSet.copyOf(squad.players()), ImmutableSet.copyOf(starters)))) {
            w.format("%s %s%n", p.getOverall().getRole(), p.getName());
        }

        w.println();

        w.format("PK: %s%n", Selections.select(Role.FW, starters).getName());


    }


    public static TeamSheet create(String team, Squad squad) {
        return new TeamSheet(team, squad);
    }

}
