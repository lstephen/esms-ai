package com.ljs.ifootballmanager.ai.formation;

import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.formation.score.DefaultScorer;
import com.ljs.ifootballmanager.ai.formation.score.FormationScorer;
import com.ljs.ifootballmanager.ai.formation.selection.Actions;
import com.ljs.ifootballmanager.ai.formation.selection.RandomFormationGenerator;
import com.ljs.ifootballmanager.ai.formation.validate.FormationValidator;
import com.ljs.ifootballmanager.ai.league.League;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.player.SquadHolder;
import com.ljs.ifootballmanager.ai.rating.Rating;
import com.ljs.ifootballmanager.ai.report.Report;
import com.ljs.ifootballmanager.ai.selection.Substitution;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

import com.github.lstephen.ai.search.HillClimbing;
import com.github.lstephen.ai.search.RepeatedHillClimbing;
import com.github.lstephen.ai.search.Validator;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

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
    return Player
      .bySkill(Rating.SHOOTING)
      .compound(Player.byTieBreak())
      .max(players());
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
      for (Player p : SquadHolder.get().getOrdering().sortedCopy(positions.get(r))) {
        players.add(p);
      }
    }

    return ImmutableList.copyOf(players);
  }

  public Stream<Player> playerStream() {
    return players().stream();
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
      System.out.print(t.toString());
      formations.add(select(league, t, criteria, scorer));
    }

    Preconditions.checkState(formations.size() == Tactic.values().length);

    final Double max = byScore(scorer).max(formations).score();

    return byScore(scorer)
      .reverse()
      .immutableSortedCopy(formations);
  }

  public static Formation selectOne(League league, SelectionCriteria criteria, FormationScorer scorer) {
    ImmutableList<Formation> candidates = select(league, criteria, scorer);

    Double base = candidates.get(0).score() * .95 - 1;

    List<Formation> weighted = Lists.newArrayList();
    for (Formation f : candidates) {
      int weighting = Math.max((int) Math.round(f.score() - base), 1);
      for (int i = 0; i < weighting; i++) {
        weighted.add(f);
      }
      System.out.print(f.getTactic().getCode() + ":" + weighting + " ");
    }

    List<Formation> weightedList = Lists.newArrayList(weighted);

    Double seed = 0.0;

    for (Player p : criteria.getAll()) {
      seed += p.getAbilitiesSum();
      seed += p.getGames();
    }

    Random r = new Random(Math.round(seed));

    Integer idx = r.nextInt(weightedList.size());

    System.out.println("(" + weightedList.size() + ") -> " + weightedList.get(idx).getTactic());

    return weightedList.get(idx);
  }

  private static Formation select(League league, Tactic tactic, SelectionCriteria criteria, FormationScorer scorer) {

    HillClimbing<Formation> builder = HillClimbing
      .<Formation>builder()
      .validator(Formation::isValid)
      .heuristic(byScore(scorer).compound(byAge().reverse()).compound(byAbilitySum()))
      .actionGenerator(Actions.create(criteria))
      .build();

    return new RepeatedHillClimbing<Formation>(
        RandomFormationGenerator.create(league.getFormationValidator(), scorer, tactic, criteria),
        builder)
      .search();
  }

  private static Ordering<Formation> byScore(final FormationScorer scorer) {
    return Ordering
      .natural()
      .onResultOf((Formation f) -> scorer.score(f, f.getTactic()));
  }

  private static Ordering<Formation> byAge() {
    return Ordering
      .natural()
      .onResultOf((Formation f) -> f
          .players()
          .stream()
          .map(Player::getAge)
          .reduce(0, Integer::sum));
  }

  private static Ordering<Formation> byAbilitySum() {
    return Ordering
      .natural()
      .onResultOf((Formation f) -> f
          .players()
          .stream()
          .flatMap(p -> Arrays.stream(Rating.values()).map(r -> p.getAbilityRating(f.findRole(p), f.getTactic(), r)))
          .reduce(0.0, Double::sum));
  }

}
