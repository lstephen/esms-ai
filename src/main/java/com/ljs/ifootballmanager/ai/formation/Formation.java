package com.ljs.ifootballmanager.ai.formation;

import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.ljs.ai.search.RepeatedHillClimbing;
import com.ljs.ai.search.State;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.formation.score.DefaultScorer;
import com.ljs.ifootballmanager.ai.formation.score.FormationScorer;
import com.ljs.ifootballmanager.ai.formation.selection.Actions;
import com.ljs.ifootballmanager.ai.formation.selection.RandomFormationGenerator;
import com.ljs.ifootballmanager.ai.formation.validate.FormationValidator;
import com.ljs.ifootballmanager.ai.league.League;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.report.Report;
import com.ljs.ifootballmanager.ai.selection.Substitution;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author lstephen
 */
public final class Formation implements State, Report {

    private final FormationValidator validator;

    private final FormationScorer scorer;

    private final Multimap<Role, Player> positions;

    private final Map<Player, Role> players = Maps.newHashMap();

    private final Tactic tactic;

    private Formation(FormationValidator validator, FormationScorer scorer, Tactic tactic, Multimap<Role, Player> in) {
        this.validator = validator;
        this.scorer = scorer;
        this.tactic = tactic;
        this.positions = in;
    }

    public FormationValidator getValidator() {
        return validator;
    }

    public Tactic getTactic() {
        return tactic;
    }

    public Formation withTactic(Tactic tactic) {
        return new Formation(validator, scorer, tactic, positions);
    }

    public Player getPenaltyKicker() {
        return Player.byRating(Role.FW, tactic).max(players());
    }

    public ImmutableSet<Role> getRoles() {
        return ImmutableSet.copyOf(positions.keySet());
    }

    public Formation move(Role r, Player p) {
        Multimap<Role, Player> f = HashMultimap.create(positions);

        if (contains(p)) {
            Player inFormation = findInFormation(p);

            f.remove(findRole(inFormation), inFormation);

            f.put(r, inFormation);
        } else {
            f.put(r, p);
        }

        return Formation.create(validator, tactic, f);
    }

    public Role findRole(Player p) {
        if (!players.containsKey(p)) {
            for (Role r : positions.keySet()) {
                if (positions.get(r).contains(p)) {
                    players.put(p, r);
                    break;
                }
            }
        }
        return players.get(p);
    }

    private Player findInFormation(Player p) {
        Role r = findRole(p);

        for (Player inFormation : positions.get(r)) {
            if (p.equals(inFormation)) {
                return inFormation;
            }
        }

        throw new IllegalStateException();
    }

    public boolean contains(Player p) {
        return positions.containsValue(p);
    }

    public ImmutableSet<Player> players(Role r) {
        return ImmutableSet.copyOf(positions.get(r));
    }

    public ImmutableList<Player> players() {
        List<Player> players = Lists.newArrayList();

        for (Role r : Ordering.natural().sortedCopy(positions.keySet())) {
            for (Player p : Player.byName().sortedCopy(positions.get(r))) {
                players.add(p);
            }
        }

        return ImmutableList.copyOf(players);
    }

    public Iterable<Player> unsortedPlayers() {
        return positions.values();
    }

    private Formation substitute(Substitution s) {
        return substitute(s.getIn(), s.getRole(), s.getOut());
    }

    public Formation substitute(Player in, Role r, Player out) {
        Multimap<Role, Player> f = HashMultimap.create(positions);

        f.remove(findRole(out), out);
        f.put(r, in);

        return Formation.create(validator, tactic, f);
    }

    public boolean isValid(Substitution s) {
        if (!contains(s.getOut())) {
            return false;
        }
        if (s.getRole().equals(findRole(s.getOut()))) {
            return true;
        }
        return substitute(s).isValid();
    }

    public Double score() {
        return scorer.score(this, tactic);
    }

    public Double score(Tactic tactic) {
        return scorer.score(this, tactic);
    }

    public Integer scoring(Tactic tactic) {
        return scorer.scoring(this, tactic);
    }

    public Integer defending(Tactic tactic) {
        return scorer.defending(this, tactic);
    }

    public Boolean isValid() {
        if (positions.size() != 11) {
            return false;
        }
        if (ImmutableSet.copyOf(positions.values()).size() != 11) {
            return false;
        }

        return validator.isValid(this);
    }

    public Integer count(Role r) {
        return positions.get(r).size();
    }

    public Integer count(Iterable<Role> rs) {
        Integer count = 0;
        for (Role r : rs) {
            count += count(r);
        }
        return count;
    }

    public void print(PrintWriter w) {
        w.format("%s%n", getTactic());
        printPlayers(w);

        scorer.print(this, w);
    }

    public void printPlayers(PrintWriter w) {
        for (Player p : players()) {
            w.format("%s %s%n", findRole(p), p.getName());
        }
    }

    public static Formation create(FormationValidator validator, Tactic tactic, Multimap<Role, Player> players) {
        return new Formation(validator, new DefaultScorer(), tactic, players);
    }

    private static Formation create(League league, Tactic tactic, Multimap<Role, Player> players) {
        return create(league.getFormationValidator(), tactic, players);
    }

    public static Formation select(League league, Iterable<Player> available) {
        return select(league, SelectionCriteria.create(league, available));
    }

    public static Formation select(League league, SelectionCriteria criteria) {
        Set<Formation> formations = Sets.newHashSet();
        for (Tactic t : Tactic.values()) {
            formations.add(select(league, t, criteria));
        }

        return byScore().max(formations);

    }

    private static Formation select(League league, Tactic tactic, SelectionCriteria criteria) {
        return new RepeatedHillClimbing<Formation>(
            Formation.class,
            RandomFormationGenerator.create(league.getFormationValidator(), tactic, criteria),
            Actions.create(criteria))
            .search();
    }

    private static Ordering<Formation> byScore() {
        return Ordering
            .natural()
            .onResultOf(new Function<Formation, Double>() {
                public Double apply(Formation f) {
                    return f.score();
                }
            });
    }

}
