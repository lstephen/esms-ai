package com.ljs.ifootballmanager.ai.report;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.ljs.ifootballmanager.ai.Context;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.WithContext;
import com.ljs.ifootballmanager.ai.league.League;
import com.ljs.ifootballmanager.ai.math.Maths;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.rating.Rating;
import com.ljs.ifootballmanager.ai.selection.FirstXI;
import com.ljs.ifootballmanager.ai.value.OverallValue;
import com.ljs.ifootballmanager.ai.value.RatingInRole;
import com.ljs.ifootballmanager.ai.value.ReplacementLevel;
import com.ljs.ifootballmanager.ai.value.ReplacementLevelHolder;
import com.ljs.ifootballmanager.ai.value.Value;
import java.io.PrintWriter;

/**
 *
 * @author lstephen
 */
public class SquadReport implements Report, WithContext {

    private final Context ctx;

    private final Tactic tactic;

    private final Iterable<Player> squad;

    private Ordering<Player> ordering;

    private Value value;

    private SquadReport(Context ctx, Tactic tactic, Iterable<Player> squad) {
        this.ctx = ctx;
        this.tactic = tactic;
        this.squad = squad;

        ordering = Player.byOverall(tactic).reverse().compound(Player.byTieBreak());
        value = getLeague().getPlayerValue();
    }

    public Context getContext() {
      return ctx;
    }

    private Double getValue(Player p) {
        Double ovr = value.getValue(p);
        Double vsRepl = ReplacementLevelHolder.get().getValueVsReplacement(p);

        Player atPotential = getLeague().getPlayerPotential().atPotential(p);

        if (ovr < value.getValue(atPotential)) {
            vsRepl = Math.max(0, vsRepl);
            vsRepl = Math.max(vsRepl, ReplacementLevelHolder.get().getValueVsReplacement(atPotential));
        }

        return ovr + vsRepl;
    }

    public SquadReport sortByValue() {
        ordering = Ordering
            .natural()
            .reverse()
            .onResultOf(new Function<Player, Long>() {
                public Long apply(Player p) {
                    return Math.round(getValue(p));
                }
            })
            .compound(Player.byTieBreak());
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

        ReplacementLevel repl = ReplacementLevelHolder.get();

        w.format("%-15s ", tactic);

        w.format("%2s %2s %8s %3s ", "", "", "", "OVR");

        for (Role r : roles) {
            w.format("%3s ", r.name());
        }

        w.format("| %3s %7s || %3s || ", "VAL", " vsRpl", "");

        getFirstXI().getTactics().forEach(t -> w.format("%3s    ", t.getCode()));

        w.println();

        for (Player p : ordering.immutableSortedCopy(squad)) {
            w.format("%-15s ", p.getName());

            RatingInRole best = OverallValue.create(ctx).getBest(p);

            String skills = String.format(
                "%2d/%2d/%2d",
                Math.round(p.getSkill(Rating.TACKLING)),
                Math.round(p.getSkill(Rating.PASSING)),
                Math.round(p.getSkill(Rating.SHOOTING)));

            if (p.getPrimarySkill() == Rating.STOPPING) {
                skills = String.format("   %d   ", Math.round(p.getSkill(Rating.STOPPING)));
            }

            w.format("%2d %2s %8s %3d ",
                p.getAge(),
                best.getRole(),
                skills,
                Maths.round(best.getRating()));

            for (Role r : roles) {
                w.format("%3d ", Maths.round(p.evaluate(r, tactic).getRating()));
            }

            Double ovr = value.getValue(p);
            Double vsRepl = repl.getValueVsReplacement(p);

            w.format(
                "| %3d %3d/%3d || %3d || ",
                Maths.round(ovr),
                Maths.round(vsRepl),
                Maths.round(repl.getValueVsReplacement(getLeague().getPlayerPotential().atPotential(p))),
                Maths.round(getValue(p)));

            getFirstXI().getTactics().forEach(t -> {
                RatingInRole rir = p.getOverall(t);
                w.format(
                    "%3d%3s ",
                    Maths.round(rir.getRating()),
                    rir.getRole() == best.getRole() ? "" : " " + rir.getRole());
            });

            w.format(
                "%s%1s%1s ",
                p.getRosterStatus(),
                Iterables.contains(getLeague().getForcedPlay(), p.getName()) ? "F" : "",
                getLeague().isReserveEligible(p) ? "r" : "");

            w.format("%s", p.getComment());

            w.println();
        }

    }

    public static SquadReport create(Context ctx, Tactic tactic, Iterable<Player> squad) {
        return new SquadReport(ctx, tactic, squad);
    }
}
