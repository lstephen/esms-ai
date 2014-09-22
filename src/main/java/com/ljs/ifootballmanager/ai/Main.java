package com.ljs.ifootballmanager.ai;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.io.CharSink;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.ljs.ifootballmanager.ai.formation.Formation;
import com.ljs.ifootballmanager.ai.formation.SelectionCriteria;
import com.ljs.ifootballmanager.ai.formation.score.AtPotentialScorer;
import com.ljs.ifootballmanager.ai.formation.score.DefaultScorer;
import com.ljs.ifootballmanager.ai.formation.score.FormationScorer;
import com.ljs.ifootballmanager.ai.formation.score.SecondXIScorer;
import com.ljs.ifootballmanager.ai.league.EliteFootballLeague;
import com.ljs.ifootballmanager.ai.league.IFootballManager;
import com.ljs.ifootballmanager.ai.league.Jafl;
import com.ljs.ifootballmanager.ai.league.League;
import com.ljs.ifootballmanager.ai.league.LeagueHolder;
import com.ljs.ifootballmanager.ai.league.Ssl;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.player.Squad;
import com.ljs.ifootballmanager.ai.player.SquadHolder;
import com.ljs.ifootballmanager.ai.report.Report;
import com.ljs.ifootballmanager.ai.report.Reports;
import com.ljs.ifootballmanager.ai.report.SquadReport;
import com.ljs.ifootballmanager.ai.report.SquadSummaryReport;
import com.ljs.ifootballmanager.ai.report.TeamSheet;
import com.ljs.ifootballmanager.ai.selection.Bench;
import com.ljs.ifootballmanager.ai.selection.ChangePlan;
import com.ljs.ifootballmanager.ai.value.AcquisitionValue;
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
            .put("EFL - TTH", EliteFootballLeague.create())
            .put("IFM - LIV", IFootballManager.create("liv"))
            .put("IFM - NOR", IFootballManager.create("nor"))
            .put("IFM - DER", IFootballManager.create("der"))
            .put("IFM - Holland", IFootballManager.create("hol"))
            .put("JAFL - GLI", Jafl.get())
            .put("SSL - MIS", Ssl.create("mis", "msy"))
            .put("SSL - ITA", Ssl.create("ita"))
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

        LeagueHolder.set(league);

        Squad squad = Squad.load(league);
        SquadHolder.set(squad);

        ImmutableList<Formation> firstXICandidates = Formation.select(league, squad.players(), DefaultScorer.get());

        Set<Player> allFirstXI = Sets.newHashSet();
        for (Formation f : firstXICandidates) {
            allFirstXI.addAll(f.players());
        }

        Preconditions.checkState(!firstXICandidates.isEmpty());

        Formation firstXI = firstXICandidates.get(0);

        ReplacementLevelHolder.set(ReplacementLevel.create(squad, firstXI));

        print(w, SquadReport.create(league, firstXI.getTactic(), squad.players()).sortByValue());
        Integer count = 1;
        Integer total = firstXICandidates.size();
        for (Formation f : firstXICandidates) {
            print(w, String.format("1st XI (%d/%d)", count++, total), f);
        }

        Set<Player> remaining = Sets.newHashSet(
            Sets.difference(
                ImmutableSet.copyOf(squad.players()),
                ImmutableSet.copyOf(firstXI.players())));

        Iterable<Player> atPotentialCandidates = FluentIterable.from(squad.players()).filter(Predicates.not(Predicates.in(allFirstXI)));
        Formation atPotentialXI = Formation.select(league, atPotentialCandidates, AtPotentialScorer.create(league.getPlayerPotential())).get(0);

        Formation secondXI = null;
        if (remaining.size() >= 11) {
            secondXI = Formation.select(league, firstXI.getTactic(), remaining, SecondXIScorer.create(league));
            print(w, "2nd XI", secondXI);
            remaining.removeAll(secondXI.players());
        }

        remaining.removeAll(atPotentialXI.players());

        Set<Player> reservesSquad = Sets.newHashSet();
        Formation reservesXI = null;
        Set<Player> allReservesXI = Sets.newHashSet();
        if (league.getReserveTeam().isPresent()) {
            ImmutableList<Formation> reserveXICandiates = Formation.select(
                league,
                SelectionCriteria.create(ImmutableSet.<Player>of(), squad.reserves(league)),
                DefaultScorer.get());
            reservesXI = reserveXICandiates.get(0);

            for (Formation f : reserveXICandiates) {
                allReservesXI.addAll(f.players());
            }

            print(w, "Reserves XI", reservesXI);
            for (Player p : allReservesXI) {
                reservesSquad.add(squad.findPlayer(p.getName()));
            }
        }

        print(w, "At Potential XI", atPotentialXI);

        Set<Player> desiredSquad = Sets.newHashSet();
        desiredSquad.addAll(allFirstXI);
        desiredSquad.addAll(atPotentialXI.players());
        if (secondXI != null) {
            desiredSquad.addAll(secondXI.players());
        }

        Set<Player> firstSquad = Sets.newHashSet();

        for (Player p : Iterables.concat(desiredSquad)) {
            if (p.isReserves()) {
                reservesSquad.add(p);
            } else {
                firstSquad.add(p);
            }
        }

        remaining.removeAll(firstSquad);
        remaining.removeAll(reservesSquad);

        Set<Player> trainingSquadCandidates = Sets.newHashSet(remaining);

        while ((reservesSquad.size() < 21 || firstSquad.size() < 21) && !trainingSquadCandidates.isEmpty()) {
            Player toAdd = Player.byValue(AcquisitionValue.create(league)).max(trainingSquadCandidates);
            trainingSquadCandidates.remove(toAdd);

            if (toAdd.isReserves() && reservesSquad.size() < 21) {
                reservesSquad.add(toAdd);
                remaining.remove(toAdd);
            }
            if (!toAdd.isReserves() && firstSquad.size() < 21) {
                firstSquad.add(toAdd);
                remaining.remove(toAdd);
            }
        }

        print(w, "First XI", SquadReport.create(league, firstXI.getTactic(), allFirstXI).sortByValue());

        print(
            w,
            String.format("First Squad (%d)", firstSquad.size()),
            SquadReport
                .create(
                    league,
                    firstXI.getTactic(),
                    FluentIterable
                        .from(firstSquad)
                        .filter(Predicates.not(Predicates.in(allFirstXI))))
                .sortByValue());

        if (!reservesSquad.isEmpty()) {
            print(w, "Reserves XI", SquadReport.create(league, reservesXI.getTactic(), allReservesXI).sortByValue());
            print(
                w,
                String.format("Reserves Squad (%d)", reservesSquad.size()),
                SquadReport.create(league, reservesXI.getTactic(), FluentIterable.from(reservesSquad).filter(Predicates.not(Predicates.in(allReservesXI)))).sortByValue());
        }

        Set<Player> trainingSquad = Sets.newHashSet(Iterables.concat(firstSquad, reservesSquad));
        trainingSquad.removeAll(allFirstXI);
        if (secondXI != null) {
            trainingSquad.removeAll(secondXI.players());
        }
        if (reservesXI != null) {
            trainingSquad.removeAll(allReservesXI);
        }

        trainingSquad.removeAll(atPotentialXI.players());
        print(w, "Training Squad", SquadReport.create(league, firstXI.getTactic(), trainingSquad).sortByValue());

        print(
            w,
            "Potentials",
            SquadReport
                .create(
                    league,
                    firstXI.getTactic(),
                    FluentIterable
                        .from(atPotentialXI.players())
                        .filter(Predicates.not(Predicates.in(allFirstXI)))
                        .filter(Predicates.not(Predicates.in(allReservesXI))))
                .sortByValue());

        print(w, "Remaining", SquadReport.create(league, firstXI.getTactic(), remaining).sortByValue());

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

            print(w, f, SquadReport.create(league, firstXI.getTactic(), additional.players()).sortByValue());
        }

        File sheetFile = new File("c:/esms", league.getTeam() + "sht.txt");
        CharSink sheet = Files.asCharSink(sheetFile, Charsets.ISO_8859_1);
        printSelection(w, league, "Selection", league.getTeam(), squad.forSelection(league), sheet, DefaultScorer.get());
        Files.copy(sheetFile, new File("c:/esms/shts", sheetFile.getName()));

        if (league.getReserveTeam().isPresent()) {
            File rsheetFile = new File("c:/esms", league.getReserveTeam().get() + "sht.txt");
            CharSink rsheet = Files.asCharSink(rsheetFile, Charsets.ISO_8859_1);
            printSelection(w, league, "Reserves Selection", league.getReserveTeam().get(), squad.forReservesSelection(league), rsheet, DefaultScorer.get());
            Files.copy(rsheetFile, new File("c:/esms/shts", rsheetFile.getName()));
        }
    }

    private void printSelection(PrintWriter w, League league, String title, String team, Iterable<Player> available, CharSink sheet, FormationScorer scorer) throws IOException {
        Set<Player> forced = Sets.newHashSet();

        for (Player p : available) {
            if (Iterables.contains(league.getForcedPlay(), p.getName()) && p.isFullFitness()) {
                forced.add(p);
            }
        }

        Formation formation = Formation.selectOne(league, SelectionCriteria.create(forced, available), scorer);

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
