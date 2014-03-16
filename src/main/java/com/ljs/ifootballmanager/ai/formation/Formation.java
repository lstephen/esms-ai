package com.ljs.ifootballmanager.ai.formation;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.ljs.ai.search.hillclimbing.HillClimbing;
import com.ljs.ai.search.hillclimbing.RepeatedHillClimbing;
import com.ljs.ai.search.hillclimbing.Validator;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.formation.score.DefaultScorer;
import com.ljs.ifootballmanager.ai.formation.score.FormationScorer;
import com.ljs.ifootballmanager.ai.formation.selection.Actions;
import com.ljs.ifootballmanager.ai.formation.selection.RandomFormationGenerator;
import com.ljs.ifootballmanager.ai.formation.validate.FormationValidator;
import com.ljs.ifootballmanager.ai.league.League;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.rating.Rating;
import com.ljs.ifootballmanager.ai.report.Report;
import com.ljs.ifootballmanager.ai.selection.Substitution;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 *
 * @author lstephen
 */
public final class Formation implements Report {

    private final FormationValidator validator;

    private final FormationScorer scorer;

    private final FormationMap positions;

    private final Tactic tactic;

    private Formation(FormationValidator validator, FormationScorer scorer, Tactic tactic, FormationMap in) {
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

    public FormationScorer getScorer() {
        return scorer;
    }

    public Formation withScorer(FormationScorer scorer) {
        return new Formation(validator, scorer, tactic, positions);
    }

    public Formation withTactic(Tactic tactic) {
        return new Formation(validator, scorer, tactic, positions);
    }

    public Formation withUpdatedPlayers(Iterable<Player> ps) {
        FormationMap f = FormationMap.create(positions);

        for (Player p : ps) {
            Role r = findRole(p);
            f.remove(r, p);
            f.put(r, p);
        }

        return new Formation(validator, scorer, tactic, f);
    }

    public Player getPenaltyKicker() {
        return Player.byRating(Role.FW, tactic).max(players());
    }

    public ImmutableSet<Role> getRoles() {
        return ImmutableSet.copyOf(positions.roles());
    }

    public Formation move(Role r, Player p) {
        FormationMap f = FormationMap.create(positions);

        if (contains(p)) {
            Player inFormation = findInFormation(p);

            f.remove(findRole(inFormation), inFormation);

            f.put(r, inFormation);
        } else {
            f.put(r, p);
        }

        return Formation.create(validator, scorer, tactic, f);
    }

    public Role findRole(Player p) {
        return positions.get(p);
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
        return positions.contains(p);
    }

    public ImmutableSet<Player> players(Role r) {
        return ImmutableSet.copyOf(positions.get(r));
    }

    public ImmutableList<Player> players() {
        List<Player> players = Lists.newArrayList();

        for (Role r : Ordering.natural().sortedCopy(positions.roles())) {
            for (Player p : Player.byName().sortedCopy(positions.get(r))) {
                players.add(p);
            }
        }

        return ImmutableList.copyOf(players);
    }

    public Iterable<Player> unsortedPlayers() {
        return positions.players();
    }

    private Formation substitute(Substitution s) {
        return substitute(s.getIn(), s.getRole(), s.getOut());
    }

    public Formation substitute(Player in, Role r, Player out) {
        FormationMap f = FormationMap.create(positions);

        f.remove(findRole(out), out);
        f.put(r, in);

        return Formation.create(validator, scorer, tactic, f);
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

    public Double scoring() {
        return scoring(tactic);
    }

    public Double scoring(Tactic tactic) {
        return scorer.scoring(this, tactic);
    }

    public Double defending() {
        return defending(tactic);
    }

    public Double defending(Tactic tactic) {
        return scorer.defending(this, tactic);
    }

    public Boolean isValid() {
        if (positions.size() != 11) {
            return false;
        }
        if (ImmutableSet.copyOf(positions.players()).size() != 11) {
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

    public static Formation create(FormationValidator validator, FormationScorer scorer, Tactic tactic, Multimap<Role, Player> players) {
        return create(validator, scorer, tactic, FormationMap.create(players));
    }

    public static Formation create(FormationValidator validator, FormationScorer scorer, Tactic tactic, FormationMap players) {
        return new Formation(validator, scorer, tactic, players);
    }

    public static Formation create(FormationValidator validator, Tactic tactic, Multimap<Role, Player> players) {
        return create(validator, DefaultScorer.get(), tactic, players);
    }

    public static ImmutableList<Formation> select(League league, Iterable<Player> available, FormationScorer scorer) {
        return select(league, SelectionCriteria.create(league, available), scorer);
    }

    public static Formation select(League league, Tactic tactic, Iterable<Player> available, FormationScorer scorer) {
        return select(league, tactic, SelectionCriteria.create(league, available), scorer);
    }

    public static ImmutableList<Formation> select(League league, SelectionCriteria criteria, FormationScorer scorer) {
        Set<Formation> formations = Sets.newHashSet();
        for (Tactic t : Tactic.values()) {
            formations.add(select(league, t, criteria, scorer));
        }

        final Double max = byScore(scorer).max(formations).score();

        return byScore(scorer)
            .reverse()
            .immutableSortedCopy(
                FluentIterable
                    .from(formations)
                    .filter(new Predicate<Formation>() {
                        public boolean apply(Formation f) {
                            return f.score() > max * 0.95;
                        }
                    })
                );
    }

    public static Formation selectOne(League league, SelectionCriteria criteria, FormationScorer scorer) {
        ImmutableList<Formation> candidates = select(league, criteria, scorer);

        Double base = byScore(scorer).min(candidates).score() - 1;

        Multiset<Formation> weighted = HashMultiset.create();
        for (Formation f : candidates) {
            int weighting = (int) Math.round(f.score() - base);
            weighted.add(f, weighting);
            System.out.print(f.getTactic().getCode() + ":" + weighting + " ");
        }

        List<Formation> weightedList = Lists.newArrayList(weighted);

        Collections.shuffle(weightedList);

        System.out.println("(" + weightedList.size() + ") -> " + weightedList.get(0).getTactic());

        return weightedList.get(0);
    }

    private static Formation select(League league, Tactic tactic, SelectionCriteria criteria, FormationScorer scorer) {

        HillClimbing.Builder<Formation> builder = HillClimbing
            .<Formation>builder()
            .validator(new Validator<Formation>() {
                @Override
                public Boolean apply(Formation f) {
                    return f.isValid();
                }
            })
            .heuristic(byScore(scorer).compound(byAbilitySum()))
            .actionGenerator(Actions.create(criteria));

        return new RepeatedHillClimbing<Formation>(
            RandomFormationGenerator.create(league.getFormationValidator(), scorer, tactic, criteria),
            builder)
            .search();
    }

    private static Ordering<Formation> byScore(final FormationScorer scorer) {
        return Ordering
            .natural()
            .onResultOf(new Function<Formation, Double>() {
                public Double apply(Formation f) {
                    return scorer.score(f, f.getTactic());
                }
            });
    }

    private static Ordering<Formation> byAbilitySum() {
        return Ordering
            .natural()
            .onResultOf(new Function<Formation, Double>() {
                public Double apply(Formation f) {
                    Double sum = 0.0;
                    for (Player p : f.players()) {
                        for (Rating rt : Rating.values()) {
                            sum += p.getAbilityRating(f.findRole(p), f.getTactic(), rt);
                        }
                    }
                    return sum;
                }
            });
    }

}
