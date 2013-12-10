package com.ljs.ifootballmanager.ai.report;

import com.ljs.ifootballmanager.ai.Squad;
import com.ljs.ifootballmanager.ai.league.League;
import com.ljs.ifootballmanager.ai.selection.Formation;
import java.io.PrintWriter;

/**
 *
 * @author lstephen
 */
public class TeamSheet implements Report {

    private final League league;

    private final Squad squad;

    private TeamSheet(League league, Squad squad) {
        this.league = league;
        this.squad = squad;
    }

    public void print(PrintWriter w) {
        w.println(league.getTeam());
        w.println("N");
        w.println();

        Formation.select(league, squad.players()).print(w);

        /*Starters starters = Starters.select(squad.players());

        for (InRole p : InRole.byRole().sortedCopy(Formation.select(starters))) {
            w.format("%s %s%n", p.getRole(), p.getName());
        }

        w.println();

        for (Player p : Bench.select(Sets.difference(ImmutableSet.copyOf(squad.players()), ImmutableSet.copyOf(starters)))) {
            w.format("%s %s%n", p.getOverall().getRole(), p.getName());
        }

        w.println();

        w.format("PK: %s%n", Selections.select(Role.FW, starters).getName());*/


    }


    public static TeamSheet create(League league, Squad squad) {
        return new TeamSheet(league, squad);
    }

}
