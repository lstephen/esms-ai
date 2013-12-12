package com.ljs.ifootballmanager.ai;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.io.CharSink;
import com.google.common.io.CharSource;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.ljs.ifootballmanager.ai.league.IFootballManager;
import com.ljs.ifootballmanager.ai.league.League;
import com.ljs.ifootballmanager.ai.league.Pbemff;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.player.Squad;
import com.ljs.ifootballmanager.ai.report.Report;
import com.ljs.ifootballmanager.ai.report.SquadReport;
import com.ljs.ifootballmanager.ai.report.SquadSummaryReport;
import com.ljs.ifootballmanager.ai.report.TeamSheet;
import com.ljs.ifootballmanager.ai.selection.Bench;
import com.ljs.ifootballmanager.ai.selection.ChangePlan;
import com.ljs.ifootballmanager.ai.selection.Formation;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * Hello world!
 *
 */
public class Main {

    public static void main( String[] args ) throws IOException {
        new Main().run();
    }

    public void run() throws IOException {
        run(IFootballManager.get());
        run(Pbemff.get());
    }

    private void run(League league) throws IOException {
        CharSink sink = Files.asCharSink(new File("c:/esms", league.getTeam() + "ovr.txt"), Charsets.ISO_8859_1);
        CharSink sheetSink = Files.asCharSink(new File("c:/esms", league.getTeam() + "sht.txt"), Charsets.ISO_8859_1);

        try (
            Writer w = sink.openStream();
            PrintWriter p = new PrintWriter(w);
            Writer sheet = sheetSink.openStream();
            PrintWriter sheetWriter = new PrintWriter(sheet)) {

            run(league, p, sheetWriter);

            p.flush();
        }
    }

    public void run(League league, PrintWriter w, PrintWriter sheet) throws IOException {
        System.out.println("Running for: " + league.getTeam());
        CharSource team =
            Resources
                .asByteSource(Main.class.getResource("/" + league.getTeam() + ".txt"))
                .asCharSource(Charsets.ISO_8859_1);

        CharStreams.copy(team, Files.asCharSink(new File("c:/esms", league.getTeam() + ".txt"), Charsets.ISO_8859_1));

        Squad squad = Squad.load(team);

        Formation firstXI = Formation.select(league, squad.players());

        w.println("** Squad **");
        print(w, SquadReport.create(Tactic.NORMAL, squad.players()));
        print(w, SquadReport.create(firstXI.getTactic(), squad.players()));

        print(w, "1st XI", firstXI);

        ImmutableSet<Player> remaining = ImmutableSet.copyOf(
            Sets.difference(
                ImmutableSet.copyOf(squad.players()),
                ImmutableSet.copyOf(firstXI.players())));

        Formation secondXI = null;
        if (remaining.size() > 11) {
            secondXI = Formation.select(league, remaining);
            print(w, "2nd XI", secondXI);
            remaining = ImmutableSet.copyOf(
                Sets.difference(
                    remaining, ImmutableSet.copyOf(secondXI.players())));
        }

        print(w, "1st XI", SquadReport.create(Tactic.NORMAL, firstXI.players()).sortByValue());
        if (secondXI != null) {
            print(w, "Second XI", SquadReport.create(Tactic.NORMAL, secondXI.players()).sortByValue());
        }
        print(w, "Remaining", SquadReport.create(Tactic.NORMAL, remaining).sortByValue());

        Formation formation = Formation.select(league, squad.forSelection());
        ChangePlan cp =
            ChangePlan.select(league, formation, squad.forSelection());
        Bench bench =
            Bench.select(formation, cp.getSubstitutes(), squad.forSelection());

        print(w, "Selection", SquadReport.create(formation.getTactic(), squad.forSelection()));
        print(w, formation, bench, cp);

        TeamSheet.create(league, formation, cp, bench).print(sheet);


        print(w, "Value", SquadReport.create(Tactic.NORMAL, squad.players()).sortByValue());

        print(w, SquadSummaryReport.create(squad, firstXI, secondXI));

        for (String f : league.getAdditionalPlayerFiles()) {
            Squad additional = Squad.load(Resources.asCharSource(getClass().getResource(f), Charsets.ISO_8859_1));

            print(w, f, SquadReport.create(Tactic.NORMAL, additional.players()).sortByValue());
        }
    }

    private void print(PrintWriter w, String title, Report report) {
        w.format("** %s **%n", title);
        print(w, report);
    }

    private void print(PrintWriter w, Report... rs) {
        for (Report r : rs) {
            r.print(w);
            w.println();
            w.flush();
        }
    }

}
