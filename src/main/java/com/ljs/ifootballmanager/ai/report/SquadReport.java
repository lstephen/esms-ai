package com.ljs.ifootballmanager.ai.report;

import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.league.League;
import com.ljs.ifootballmanager.ai.math.Maths;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.value.RatingInRole;
import com.ljs.ifootballmanager.ai.value.Value;
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

    private Value value;

    private SquadReport(League league, Tactic tactic, Iterable<Player> squad) {
        this.league = league;
        this.tactic = tactic;
        this.squad = squad;

        ordering = Player.byOverall(tactic).reverse();
        value = league.getPlayerValue();
    }

    public SquadReport sortByValue() {
        ordering = Player.byValue(league).reverse();
        return this;
    }

    public SquadReport sortByValue(Value value) {
        this.value = value;
        ordering = Player.byValue(value).reverse();
        return this;
    }

    public void print(PrintWriter w) {
        Role[] roles = Role.values();
        Tactic[] tactics = Tactic.values();

        w.format("%-15s ", tactic);

        w.format("%2s %2s %5s ", "", "", "OVR");

        for (Role r : roles) {
            w.format("%3s ", r.name());
        }

        w.format(" (%3s) ", "VAL");

        for (Tactic t : tactics) {
            w.format("%3s    ", t.getCode());
        }

        w.println();

        for (Player p : ordering.immutableSortedCopy(squad)) {
            w.format("%-15s ", p.getName());

            RatingInRole best = p.getOverall(tactic);

            w.format("%2d %2s %5d ",
                p.getAge(),
                best.getRole(),
                Maths.round(best.getRating()));

            for (Role r : roles) {
                w.format("%3d ", Maths.round(p.evaluate(r, tactic).getRating()));
            }

            w.format(" (%3d) ", Maths.round(value.getValue(p)));

            for (Tactic t : tactics) {
                RatingInRole rir = p.getOverall(t);
                w.format(
                    "%3d%3s ",
                    Maths.round(rir.getRating()),
                    rir.getRole() == best.getRole() ? "" : "-" + rir.getRole());
            }

            w.format(
                "%s%1s%1s ",
                p.getRosterStatus(),
                Iterables.contains(league.getForcedPlay(), p.getName()) ? "F" : "",
                league.isReserveEligible(p) ? "r" : "");

            w.format("%s", p.getComment());

            w.println();
        }

    }

    public static SquadReport create(League league, Tactic tactic, Iterable<Player> squad) {
        return new SquadReport(league, tactic, squad);
    }
}
