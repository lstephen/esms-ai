package com.ljs.ifootballmanager.ai;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.io.CharSink;
import com.google.common.io.Files;
import com.ljs.ifootballmanager.ai.formation.Formation;
import com.ljs.ifootballmanager.ai.formation.SelectionCriteria;
import com.ljs.ifootballmanager.ai.formation.score.AtPotentialScorer;
import com.ljs.ifootballmanager.ai.formation.score.DefaultScorer;
import com.ljs.ifootballmanager.ai.formation.score.FormationScorer;
import com.ljs.ifootballmanager.ai.league.EliteFootballLeague;
import com.ljs.ifootballmanager.ai.league.Esl;
import com.ljs.ifootballmanager.ai.league.League;
import com.ljs.ifootballmanager.ai.league.LeagueHolder;
import com.ljs.ifootballmanager.ai.league.Ssl;
import com.ljs.ifootballmanager.ai.league.Ucfl;
import com.ljs.ifootballmanager.ai.math.Maths;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.player.Squad;
import com.ljs.ifootballmanager.ai.player.SquadHolder;
import com.ljs.ifootballmanager.ai.report.ReplacementLevelReport;
import com.ljs.ifootballmanager.ai.report.Report;
import com.ljs.ifootballmanager.ai.report.Reports;
import com.ljs.ifootballmanager.ai.report.SquadReport;
import com.ljs.ifootballmanager.ai.report.SquadSummaryReport;
import com.ljs.ifootballmanager.ai.report.TeamSheet;
import com.ljs.ifootballmanager.ai.selection.Bench;
import com.ljs.ifootballmanager.ai.selection.ChangePlan;
import com.ljs.ifootballmanager.ai.selection.FirstXI;
import com.ljs.ifootballmanager.ai.value.AcquisitionValue;
import com.ljs.ifootballmanager.ai.value.ReplacementLevel;
import com.ljs.ifootballmanager.ai.value.ReplacementLevelHolder;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Set;
import java.util.stream.Collectors;

/** Hello world! */
public class Main {

  private static final ImmutableMap<String, League> SITES =
      ImmutableMap.<String, League>builder()
          .put("EFL_TTH", EliteFootballLeague.create())
          .put("ESL_WAT", Esl.create())
          //.put("IFM_LIV", IFootballManager.create("liv"))
          //.put("IFM_ NOR", IFootballManager.create("nor"))
          //.put("IFM - DER", IFootballManager.create("der"))
          //.put("IFM - Holland", IFootballManager.create("hol"))
          //.put("JAFL_GLI", Jafl.get())
          .put("SSL_MIS", Ssl.create("mis", "msy"))
          .put("UCFL_GLI", Ucfl.create("gli", "gly"))
          //.put("SSL - ARG", Ssl.create("arg"))
          .build();

  public static void main(String[] args) throws IOException {
    new Main().run();
  }

  public void run() throws IOException {
    String site = System.getenv("ESMSAI_SITE");

    Preconditions.checkNotNull(site, "ESMSAI_SITE must be provided");

    run(SITES.get(site));
  }

  private void run(League league) throws IOException {
    try (Writer w = output(league).openStream();
        PrintWriter p = new PrintWriter(w); ) {

      run(league, p);

      p.flush();
    }
  }

  private CharSink output(League league) {
    File baseDir = Config.get().getDataDirectory();
    return Files.asCharSink(new File(baseDir, league.getTeam() + "ovr.txt"), Charsets.ISO_8859_1);
  }

  public void run(League league, PrintWriter w) throws IOException {
    System.out.println("Running for: " + league.getTeam());

    Context ctx = new Context();

    ctx.setLeague(league);
    LeagueHolder.set(league);

    Squad squad = Squad.load(league);
    ctx.setSquad(squad);
    SquadHolder.set(squad);

    ctx.setFirstXI(FirstXI.select(ctx));

    Set<Player> allFirstXI = ctx.getFirstXI().getPlayers().collect(Collectors.toSet());

    Formation bestXI = ctx.getFirstXI().best();

    ReplacementLevelHolder.set(ReplacementLevel.create(ctx));
    ctx.setReplacementLevel(ReplacementLevelHolder.get());

    print(w, SquadReport.create(ctx, bestXI.getTactic(), squad.players()).sortByValue());
    ctx.getFirstXI().getFormations().forEach(f -> print(w, "1st XI", f));

    Set<Player> remaining =
        Sets.newHashSet(
            Sets.difference(
                ImmutableSet.copyOf(squad.players()), ImmutableSet.copyOf(bestXI.players())));

    Iterable<Player> atPotentialCandidates =
        FluentIterable.from(squad.players()).filter(Predicates.not(Predicates.in(allFirstXI)));
    Formation atPotentialXI =
        Formation.select(
                league,
                atPotentialCandidates,
                AtPotentialScorer.create(league.getPlayerPotential()))
            .get(0);

    remaining.removeAll(atPotentialXI.players());

    Set<Player> reservesSquad = Sets.newHashSet();
    Formation reservesXI = null;
    Set<Player> allReservesXI = Sets.newHashSet();
    if (league.getReserveTeam().isPresent()) {
      ImmutableList<Formation> reserveXICandiates =
          Formation.select(
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

    Set<Player> firstSquad = Sets.newHashSet();

    for (Player p : desiredSquad) {
      if (p.isReserves()) {
        reservesSquad.add(p);
      } else {
        firstSquad.add(p);
      }
    }

    remaining.removeAll(firstSquad);
    remaining.removeAll(reservesSquad);

    Set<Player> trainingSquadCandidates = Sets.newHashSet(remaining);

    while ((reservesSquad.size() < 21 || firstSquad.size() < 21)
        && !trainingSquadCandidates.isEmpty()) {
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

    print(w, "First XI", SquadReport.create(ctx, bestXI.getTactic(), allFirstXI).sortByValue());

    print(
        w,
        String.format("First Squad (%d)", firstSquad.size()),
        SquadReport.create(
                ctx,
                bestXI.getTactic(),
                FluentIterable.from(firstSquad).filter(Predicates.not(Predicates.in(allFirstXI))))
            .sortByValue());

    if (!reservesSquad.isEmpty()) {
      print(
          w,
          "Reserves XI",
          SquadReport.create(ctx, reservesXI.getTactic(), allReservesXI).sortByValue());
      print(
          w,
          String.format("Reserves Squad (%d)", reservesSquad.size()),
          SquadReport.create(
                  ctx,
                  reservesXI.getTactic(),
                  FluentIterable.from(reservesSquad)
                      .filter(Predicates.not(Predicates.in(allReservesXI))))
              .sortByValue());
    }

    Set<Player> trainingSquad = Sets.newHashSet(Iterables.concat(firstSquad, reservesSquad));
    trainingSquad.removeAll(allFirstXI);

    if (reservesXI != null) {
      trainingSquad.removeAll(allReservesXI);
    }

    trainingSquad.removeAll(atPotentialXI.players());
    print(
        w,
        "Training Squad",
        SquadReport.create(ctx, bestXI.getTactic(), trainingSquad).sortByValue());

    Iterable<Player> potentials =
        FluentIterable.from(atPotentialXI.players())
            .filter(Predicates.not(Predicates.in(allFirstXI)))
            .filter(Predicates.not(Predicates.in(allReservesXI)));

    print(w, "Potentials", SquadReport.create(ctx, bestXI.getTactic(), potentials).sortByValue());

    print(w, "Remaining", SquadReport.create(ctx, bestXI.getTactic(), remaining).sortByValue());

    print(w, "Replacement Level", ReplacementLevelReport.create(ctx));

    print(
        w,
        SquadSummaryReport.builder().squad(squad).firstXI(bestXI).reservesXI(reservesXI).build());

    for (String f : league.getAdditionalPlayerFiles()) {
      String resource = "/rosters/" + league.getClass().getSimpleName() + f;

      System.out.println("Loading:" + resource);

      File sq = new File(Config.get().getDataDirectory(), resource);

      Squad additional = Squad.load(league, Files.asCharSource(sq, Charsets.UTF_8));

      print(w, f, SquadReport.create(ctx, bestXI.getTactic(), additional.players()).sortByValue());
    }

    File sheetFile = new File(Config.get().getDataDirectory(), league.getTeam() + "sht.txt");
    CharSink sheet = Files.asCharSink(sheetFile, Charsets.UTF_8);
    printSelection(
        w,
        ctx,
        "Selection",
        league.getTeam(),
        squad.forSelection(league),
        league.getForcedPlay(),
        sheet,
        DefaultScorer.get());
    Files.copy(sheetFile, new File(Config.get().getDataDirectory(), "shts/" + sheetFile.getName()));

    if (league.getReserveTeam().isPresent()) {
      Set<String> forced = Sets.newHashSet();
      Iterables.addAll(forced, league.getForcedPlay());

      ReplacementLevel repl = ReplacementLevelHolder.get();

      for (Player p : potentials) {
        Integer vsRepl =
            Maths.round(repl.getValueVsReplacement(league.getPlayerPotential().atPotential(p)));

        if (vsRepl > 0) {
          forced.add(p.getName());
        }
      }

      File rsheetFile =
          new File(Config.get().getDataDirectory(), league.getReserveTeam().get() + "sht.txt");
      CharSink rsheet = Files.asCharSink(rsheetFile, Charsets.UTF_8);
      printSelection(
          w,
          ctx,
          "Reserves Selection",
          league.getReserveTeam().get(),
          squad.forReservesSelection(league),
          forced,
          rsheet,
          DefaultScorer.get());
      Files.copy(
          rsheetFile, new File(Config.get().getDataDirectory(), "shts/" + rsheetFile.getName()));
    }
  }

  private void printSelection(
      PrintWriter w,
      Context ctx,
      String title,
      String team,
      Iterable<Player> available,
      Iterable<String> forcedPlay,
      CharSink sheet,
      FormationScorer scorer)
      throws IOException {
    Set<Player> forced = Sets.newHashSet();

    for (Player p : available) {
      Boolean isForced = Iterables.contains(forcedPlay, p.getName());
      Boolean isFullFitness = SquadHolder.get().findPlayer(p.getName()).isFullFitness();

      if (isForced && isFullFitness) {
        forced.add(p);
      }
    }

    System.out.print("Forced:");
    for (Player p : Player.byName().sortedCopy(forced)) {
      System.out.print(p.getName() + "/");
    }
    System.out.println();

    Formation formation =
        Formation.selectOne(ctx.getLeague(), SelectionCriteria.create(forced, available), scorer);

    ChangePlan cp = ChangePlan.select(ctx.getLeague(), formation, available);
    Bench bench = Bench.select(formation, cp.getSubstitutes(), available);

    print(w, title, SquadReport.create(ctx, formation.getTactic(), available));
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
