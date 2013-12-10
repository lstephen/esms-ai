package com.ljs.ifootballmanager.ai;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.ljs.ifootballmanager.ai.league.IFootballManager;
import com.ljs.ifootballmanager.ai.league.League;
import com.ljs.ifootballmanager.ai.league.Pbemff;
import com.ljs.ifootballmanager.ai.report.SquadReport;
import com.ljs.ifootballmanager.ai.report.TeamSheet;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * Hello world!
 *
 */
public class Main {

    public static void main( String[] args ) throws IOException {
        new Main().run(System.out);
    }

    public void run(OutputStream out) throws IOException {
        PrintWriter w = new PrintWriter(out);
        run(IFootballManager.get(), w);
        run(Pbemff.get(), w);
        w.flush();
    }

    public void run(League league, PrintWriter w) throws IOException {
        Squad squad = Squad.load(
            Resources
                .asByteSource(Main.class.getResource("/" + league.getTeam() + ".txt"))
                .asCharSource(Charsets.ISO_8859_1));

        SquadReport.create(squad).print(w);

        TeamSheet.create(league, squad).print(w);


    }

}
