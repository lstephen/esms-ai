package com.ljs.ifootballmanager.ai;

import com.google.common.base.Charsets;
import com.google.common.io.CharSink;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.ljs.ifootballmanager.ai.league.IFootballManager;
import com.ljs.ifootballmanager.ai.league.League;
import com.ljs.ifootballmanager.ai.league.Pbemff;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.report.SquadReport;
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
        CharSink sink = Files.asCharSink(new File("c:/esms", league.getTeam() + ".txt"), Charsets.ISO_8859_1);

        try (
            Writer w = sink.openStream();
            PrintWriter p = new PrintWriter(w);) {

            run(league, p);

            p.flush();
        }
    }

    public void run(League league, PrintWriter w) throws IOException {
        System.out.println("Running for: " + league.getTeam());
        Squad squad = Squad.load(
            Resources
                .asByteSource(Main.class.getResource("/" + league.getTeam() + ".txt"))
                .asCharSource(Charsets.ISO_8859_1));

        w.println("** First Team ** ");
        run(league, squad.players(), w);

        w.flush();
        w.println();
        w.println("** Selection **");
        run(league, squad.forSelection(), w);
    }

    public void run(League league, Iterable<Player> ps, PrintWriter w) throws IOException {
        SquadReport.create(ps).print(w);
        w.println();

        Formation formation = Formation.select(league, ps);

        formation.print(w);
        w.println();
        ChangePlan.select(league, formation, ps).print(w);
        w.println();
    }

}
