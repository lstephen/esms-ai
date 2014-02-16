package com.ljs.ifootballmanager.ai;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.io.CharSink;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.ljs.ifootballmanager.ai.formation.Formation;
import com.ljs.ifootballmanager.ai.formation.score.DefaultScorer;
import com.ljs.ifootballmanager.ai.formation.score.FormationScorer;
import com.ljs.ifootballmanager.ai.formation.score.YouthTeamScorer;
import com.ljs.ifootballmanager.ai.league.IFootballManager;
import com.ljs.ifootballmanager.ai.league.Jafl;
import com.ljs.ifootballmanager.ai.league.League;
import com.ljs.ifootballmanager.ai.league.Ssl;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.player.Squad;
import com.ljs.ifootballmanager.ai.report.Report;
import com.ljs.ifootballmanager.ai.report.Reports;
import com.ljs.ifootballmanager.ai.report.SquadReport;
import com.ljs.ifootballmanager.ai.report.SquadSummaryReport;
import com.ljs.ifootballmanager.ai.report.TeamSheet;
import com.ljs.ifootballmanager.ai.selection.Bench;
import com.ljs.ifootballmanager.ai.selection.ChangePlan;
import com.ljs.ifootballmanager.ai.value.ReplacementLevel;
import com.ljs.ifootballmanager.ai.value.ReplacementLevelHolder;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Set;
import javax.swing.JOptionPane;

/**
 * Hello world!
 *
 */
public class Main {

    private static final ImmutableMap<String, League> SITES =
        ImmutableMap
            .<String, League>builder()
            .put("IFM - LIV", IFootballManager.create("liv"))
            .put("IFM - NOR", IFootballManager.create("nor"))
            .put("IFM - DER", IFootballManager.create("der"))
            .put("JAFL - GLI", Jafl.get())
            .put("SSL - MIS", Ssl.get())
            .build();

    public static void main( String[] args ) throws IOException {
        new Main().run();
    }

    public void run() throws IOException {
        String site = System.getProperty("site");

        if (site == null) {
            site = (String) JOptionPane.showInputDialog(
                null,
                "Please select league:",
                "ESMS-AI",
                JOptionPane.QUESTION_MESSAGE,
                null,
                Ordering.natural().sortedCopy(SITES.keySet()).toArray(),
                null);
        }

        run(SITES.get(site));
    }

    private void run(League league) throws IOException {
        CharSink sink = Files.asCharSink(new File("c:/esms", league.getTeam() + "ovr.txt"), Charsets.ISO_8859_1);

        try (
            Writer w = sink.openStream();
            PrintWriter p = new PrintWriter(w); ) {

            run(league, p);

            p.flush();
        }
    }

    public void run(League league, PrintWriter w) throws IOException {
        System.out.println("Running for: " + league.getTeam());

        Squad squad = Squad.load(league);

        Formation firstXI = Formation.select(league, squad.players(), DefaultScorer.get());

        ReplacementLevelHolder.set(ReplacementLevel.create(squad, firstXI));

        print(w, "Squad", SquadReport.create(league, Tactic.NORMAL, squad.players()).sortByValue());
        print(w, SquadReport.create(league, firstXI.getTactic(), squad.players()));


        print(w, "1st XI", firstXI);

        ImmutableSet<Player> remaining = ImmutableSet.copyOf(
            Sets.difference(
                ImmutableSet.copyOf(squad.players()),
                ImmutableSet.copyOf(firstXI.players())));

        Formation secondXI = null;
        if (remaining.size() >= 11) {
            secondXI = Formation.select(league, remaining, DefaultScorer.get());
            print(w, "2nd XI", secondXI);
            remaining = ImmutableSet.copyOf(
                Sets.difference(
                    remaining, ImmutableSet.copyOf(secondXI.players())));
        }

        Formation reservesXI = null;
        if (league.getReserveTeam().isPresent()) {
            reservesXI = Formation.select(league, squad.reserves(), YouthTeamScorer.create(league, squad));
            print(w, "Reserves XI", reservesXI);
            remaining = ImmutableSet.copyOf(
                Sets.difference(
                    remaining, ImmutableSet.copyOf(reservesXI.players())));
        }

        print(w, "1st XI", SquadReport.create(league, Tactic.NORMAL, firstXI.players()).sortByValue());
        if (secondXI != null) {
            print(w, "Second XI", SquadReport.create(league, Tactic.NORMAL, secondXI.players()).sortByValue());
        }
        if (reservesXI != null) {
            print(w, "Reserves XI", SquadReport.create(league, Tactic.NORMAL, reservesXI.players()).sortByValue());
        }
        print(w, "Remaining", SquadReport.create(league, Tactic.NORMAL, remaining).sortByValue());

        File sheetFile = new File("c:/esms", league.getTeam() + "sht.txt");
        CharSink sheet = Files.asCharSink(sheetFile, Charsets.ISO_8859_1);
        printSelection(w, league, "Selection", league.getTeam(), squad.forSelection(), sheet, DefaultScorer.get());
        Files.copy(sheetFile, new File("c:/esms/shts", sheetFile.getName()));

        if (league.getReserveTeam().isPresent()) {
            File rsheetFile = new File("c:/esms", league.getReserveTeam().get() + "sht.txt");
            CharSink rsheet = Files.asCharSink(rsheetFile, Charsets.ISO_8859_1);
            printSelection(w, league, "Reserves Selection", league.getReserveTeam().get(), squad.forReservesSelection(), rsheet, YouthTeamScorer.create(league, squad));
            Files.copy(rsheetFile, new File("c:/esms/shts", rsheetFile.getName()));
        }

        print(
            w,
            SquadSummaryReport
                .builder()
                .squad(squad)
                .firstXI(firstXI)
                .secondXI(secondXI)
                .reservesXI(reservesXI)
                .build());

        for (String f : league.getAdditionalPlayerFiles()) {
            String resource = "/" + league.getClass().getSimpleName() + f;

            System.out.println("Loading:" + resource);
            Squad additional = Squad.load(league, Resources.asCharSource(getClass().getResource(resource), Charsets.ISO_8859_1));

            print(w, f, SquadReport.create(league, Tactic.NORMAL, additional.players()).sortByValue());
            print(w, f, SquadReport.create(league, firstXI.getTactic(), additional.players()).sortByValue());
        }
    }

    private void printSelection(PrintWriter w, League league, String title, String team, Iterable<Player> available, CharSink sheet, FormationScorer scorer) throws IOException {
        Set<String> forced = Sets.newHashSet();

        for (Player p : available) {
            if (Iterables.contains(league.getForcedPlay(), p.getName())) {
                forced.add(p.getName());
            }
        }

        Formation formation = Formation.select(league, available, scorer);

        ChangePlan cp =
            ChangePlan.select(league, formation, available);
        Bench bench =
            Bench.select(formation, cp.getSubstitutes(), available);

        print(w, title, SquadReport.create(league, formation.getTactic(), available));
        print(w, formation, bench, cp);

        Reports.print(TeamSheet.create(team, formation, cp, bench)).to(sheet);
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
