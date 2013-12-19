package com.ljs.ifootballmanager.ai.formation.score;

import com.google.common.collect.ImmutableSet;
import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.formation.Formation;
import com.ljs.ifootballmanager.ai.league.League;
import com.ljs.ifootballmanager.ai.player.Player;
import java.io.PrintWriter;

/**
 *
 * @author lstephen
 */
public class VsSquad implements FormationScorer {

    private League league;

    private ImmutableSet<Player> oppSquad;

    private VsSquad(League league, Iterable<Player> oppSquad) {
        this.league = league;
        this.oppSquad = ImmutableSet.copyOf(oppSquad);
    }

    public Double score(Formation f, Tactic t) {
        Formation opp = Formation.selectVs(league, oppSquad, f);

        Double us = VsFormation.create(opp).score(f, t);
        Double them = VsFormation.create(f).score(opp, opp.getTactic());

        return us - them;
    }

    public Integer scoring(Formation f, Tactic t) {
        return DefaultScorer.get().scoring(f, t);
    }

    public Integer defending(Formation f, Tactic t) {
        return DefaultScorer.get().defending(f, t);
    }

    public void print(Formation f, PrintWriter w) {
        Formation.selectVs(league, oppSquad, f).print(w);
        w.println();
    }

    public static VsSquad create(League league, Iterable<Player> oppSquad) {
        return new VsSquad(league, oppSquad);
    }

}
