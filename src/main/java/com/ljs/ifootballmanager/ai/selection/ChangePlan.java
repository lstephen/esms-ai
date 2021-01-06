package com.ljs.ifootballmanager.ai.selection;

import com.github.lstephen.ai.search.HillClimbing;
import com.github.lstephen.ai.search.RepeatedHillClimbing;
import com.github.lstephen.ai.search.action.Action;
import com.github.lstephen.ai.search.action.ActionGenerator;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.ljs.ifootballmanager.ai.Context;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.formation.Formation;
import com.ljs.ifootballmanager.ai.formation.SelectionCriteria;
import com.ljs.ifootballmanager.ai.league.League;
import com.ljs.ifootballmanager.ai.league.LeagueHolder;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.report.Report;
import com.ljs.ifootballmanager.ai.value.OverallValue;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;

/** @author lstephen */
public final class ChangePlan implements Report {

  private static final Integer GAME_MINUTES = 90;

  private static final Double INJURY_CHANCE_PER_MINUTE = 0.003;

  private final Formation formation;

  private final ImmutableSet<Change> changes;

  private final Map<Integer, Double> scores;

  private final Map<Pair<Player, Integer>, Player> afterMinutes;

  private final Boolean isAllowChangePos;

  private ChangePlan(
      Formation formation,
      Iterable<? extends Change> cs,
      Map<Integer, Double> scores,
      Map<Pair<Player, Integer>, Player> afterMinutes) {
    this.formation = formation;
    this.changes = ImmutableSet.copyOf(cs);
    this.scores = Maps.newHashMap(scores);
    this.afterMinutes = Maps.newHashMap(afterMinutes);

    isAllowChangePos = LeagueHolder.get().isAllowedChangePos();
  }

  private ImmutableList<Change> changes() {
    return Change.Meta.byMinute().immutableSortedCopy(changes);
  }

  public <C extends Change> ImmutableList<C> changes(Class<C> clazz) {
    List<C> cs = Lists.newArrayList();

    for (Change c : changes()) {
      if (clazz.isInstance(c)) {
        cs.add(clazz.cast(c));
      }
    }

    return ImmutableList.copyOf(Change.Meta.byMinute().immutableSortedCopy(cs));
  }

  public ImmutableList<Change> changesMadeAt(Integer minute) {
    List<Change> cs = Lists.newArrayList();

    for (Change c : changes()) {
      if (c.getMinute() <= minute) {
        cs.add(c);
      }
    }

    return ImmutableList.copyOf(cs);
  }

  public <C extends Change> ImmutableList<C> changesMadeAt(Integer minute, Class<C> clazz) {
    List<C> cs = Lists.newArrayList();

    for (Change c : changesMadeAt(minute)) {
      if (clazz.isInstance(c)) {
        cs.add(clazz.cast(c));
      }
    }

    return ImmutableList.copyOf(cs);
  }

  public Boolean isChangeAt(Integer minute) {
    for (Change c : changes()) {
      if (c.getMinute().equals(minute)) {
        return true;
      }
    }
    return false;
  }

  public void print(PrintWriter w) {
    for (Change c : changes()) {
      c.print(w);
    }

    ScoreTactics.create(formation, this).print(w);
  }

  public void print(PrintWriter w, Function<Player, Integer> playerIdx) {
    for (Change c : changes()) {
      c.print(w, playerIdx);
    }

    ScoreTactics.create(formation, this).print(w);
  }

  public Tactic getBestScoringTactic() {
    return Ordering.natural()
        .onResultOf(
            new Function<Tactic, Double>() {
              public Double apply(Tactic t) {
                return scoring(t);
              }
            })
        .max(Arrays.asList(Tactic.values()));
  }

  public Tactic getBestDefensiveTactic() {
    return Ordering.natural()
        .onResultOf(
            new Function<Tactic, Double>() {
              public Double apply(Tactic t) {
                return defending(t);
              }
            })
        .max(Arrays.asList(Tactic.values()));
  }

  private double scoring(Tactic t) {
    double score = 0.0;

    for (Integer minute = 1; minute < GAME_MINUTES; minute++) {
      Formation f = getFormationAt(minute);
      score += weightedAtMinute(f.scoring(t) + f.score(t), minute);
    }

    return score;
  }

  private double defending(Tactic t) {
    double score = 0.0;
    for (Integer minute = 1; minute <= GAME_MINUTES; minute++) {
      Formation f = getFormationAt(minute);
      score += weightedAtMinute(f.defending(t) + f.score(t), minute);
    }
    return score;
  }

  private double weightedAtMinute(double score, Integer minute) {
    return score * weightingAtMinute(minute);
  }

  private double weightingAtMinute(Integer minute) {
    return Math.pow((double) minute / GAME_MINUTES, 2);
  }

  public ImmutableSet<Player> getSubstitutes() {
    Set<Player> subs = Sets.newHashSet();

    for (Substitution s : changes(Substitution.class)) {
      subs.add(s.getIn());
    }

    return ImmutableSet.copyOf(subs);
  }

  private ChangePlan with(Change c) {
    Set<Change> cs = Sets.newHashSet(changes);
    cs.add(c);

    return new ChangePlan(formation, cs, scoresBefore(c.getMinute()), afterMinutes);
  }

  private ChangePlan remove(Change c) {
    Set<Change> cs = Sets.newHashSet(changes);
    cs.remove(c);
    return new ChangePlan(formation, cs, scoresBefore(c.getMinute()), afterMinutes);
  }

  private Map<Integer, Double> scoresBefore(Integer minute) {
    Map<Integer, Double> scores = Maps.newHashMap();

    for (int i = 0; i < minute; i++) {
      if (scores.containsKey(i)) {
        scores.put(i, this.scores.get(i));
      }
    }

    return scores;
  }

  private Boolean isAllowChangePos() {
    return isAllowChangePos;
  }

  public Boolean isValid() {
    // Max is 15, and 5 for score based tactics, 2 for rest based
    if (changes().size() > 8) {
      return false;
    }

    if (changes(Substitution.class).size() > 3) {
      return false;
    }

    if (!isAllowChangePos() && !changes(ChangePosition.class).isEmpty()) {
      return false;
    }

    if (!changes(TacticChange.class).isEmpty()) {
      return false;
    }

    Set<Integer> minutes = Sets.newHashSet();

    for (Change c : changes) {
      if (!c.isValid(this)) {
        return false;
      }
      minutes.add(c.getMinute());
    }

    if (minutes.size() != changes.size()) {
      return false;
    }

    return true;
  }

  public Double score() {
    Double score = 0.0;

    for (Integer minute = 0; minute <= 90; minute++) {
      score += weightedAtMinute(score(minute), minute);
    }

    return score;
  }

  public Double score(Integer minute) {
    if (scores.containsKey(minute)) {
      return scores.get(minute);
    }

    Double score = getFormationAt(minute).score();

    Integer substitutionsMade = changesMadeAt(minute, Substitution.class).size();

    Double chanceOfPlayingWithTen = chanceOfInjuries(4 - substitutionsMade, minute);

    score = (1.0 - chanceOfPlayingWithTen) * score + chanceOfPlayingWithTen * 0.9 * score;

    scores.put(minute, score);

    return score;
  }

  private Double chanceOfInjuries(Integer number, Integer minute) {
    return Math.pow(1.0 - Math.pow(1.0 - INJURY_CHANCE_PER_MINUTE, minute), number);
  }

  public Formation getFormationAt(Integer minute) {
    Set<Player> players = Sets.newHashSet();

    for (Player p : formation.players()) {
      players.add(p.afterMinutes(minute));
    }

    // GK's don't fatigue
    players.addAll(formation.players(Role.GK));

    Formation f = formation.withUpdatedPlayers(players);

    for (Change c : changesMadeAt(minute)) {
      f = c.apply(f, minute);
    }

    return f;
  }

  private Player afterMinutes(Player p, Integer minute) {
    Pair<Player, Integer> key = Pair.of(p, minute);

    if (!afterMinutes.containsKey(key)) {
      afterMinutes.put(key, p.afterMinutes(minute));
    }

    return afterMinutes.get(key);
  }

  private static Ordering<ChangePlan> byScore() {
    return Ordering.natural().onResultOf(ChangePlan::score);
  }

  public static Ordering<ChangePlan> byChangesSize() {
    return Ordering.natural().onResultOf((ChangePlan cp) -> cp.changes.size());
  }

  public static ChangePlan select(
      Context ctx, League league, final Formation f, final Iterable<Player> squad) {
    return select(ctx, league, f, SelectionCriteria.create(league, squad));
  }

  public static ChangePlan select(
      Context ctx, League league, final Formation f, final SelectionCriteria criteria) {

    HillClimbing<ChangePlan> hc =
        HillClimbing.<ChangePlan>builder()
            .validator(ChangePlan::isValid)
            .heuristic(byScore().compound(byChangesSize().reverse()))
            .actionGenerator(actionsFunction(ctx, league, criteria))
            .build();

    return new RepeatedHillClimbing<ChangePlan>(() -> zero(f), hc).search();
  }

  private static ChangePlan zero(Formation f) {
    return new ChangePlan(
        f,
        Sets.newHashSet(),
        ImmutableMap.<Integer, Double>of(),
        ImmutableMap.<Pair<Player, Integer>, Player>of());
  }

  private static ActionGenerator<ChangePlan> actionsFunction(
      final Context ctx, final League league, final SelectionCriteria criteria) {
    return new ActionGenerator<ChangePlan>() {

      @Override
      public Stream<Action<ChangePlan>> apply(ChangePlan cp) {
        List<Action<ChangePlan>> actions = Lists.newArrayList();

        if (cp != null) {
          actions.addAll(adds(cp));
        }

        return actions.stream();
      }

      private Double getValue(Player p) {
        return OverallValue.create(ctx).getValue(p);
      }

      private Set<Player> getBestSubstitutes(ChangePlan cp) {
        Formation f = cp.getFormationAt(0);

        return criteria.getAll().stream()
            .filter(p -> !f.contains(p))
            .sorted(Ordering.natural().onResultOf((Player p) -> getValue(p)).reverse())
            .limit(5)
            .collect(Collectors.toSet());
      }

      private Set<Substitution> availableSubstitutions(ChangePlan cp) {
        Set<Player> possibleSubs = getBestSubstitutes(cp);

        Set<Substitution> ss = Sets.newHashSet();

        for (Substitution s : cp.changes(Substitution.class)) {
          for (Role r : Role.values()) {
            if (!r.equals(s.getRole())) {
              ss.add(
                  Substitution.builder()
                      .in(s.getIn(), r)
                      .out(s.getOut())
                      .minute(s.getMinute())
                      .build());
            }
          }
        }

        for (Role r : Role.values()) {
          if (r == Role.GK) {
            continue;
          }
          for (Integer minute = 45; minute <= 90; minute += (minute < 75 ? 5 : 3)) {
            if (cp.isChangeAt(minute)) {
              continue;
            }
            Formation currentFormation = cp.getFormationAt(minute);
            for (Player in : possibleSubs) {
              if (cp.formation.contains(in)) {
                continue;
              }

              for (Player out : currentFormation.players()) {
                if (currentFormation.findRole(out) == Role.GK) {
                  continue;
                }
                if (criteria.isRequired(out)) {
                  continue;
                }
                Substitution s = Substitution.builder().in(in, r).out(out).minute(minute).build();

                ss.add(s);
              }
            }
          }
        }

        return ss;
      }

      private Set<Action<ChangePlan>> adds(ChangePlan cp) {
        Set<Action<ChangePlan>> adds = Sets.newHashSet();

        for (Substitution s : availableSubstitutions(cp)) {
          adds.add(new AddChange(s));
        }

        if (cp.isAllowChangePos()) {
          for (Integer minute = 45; minute <= 90; minute += (minute < 75 ? 5 : 3)) {
            if (cp.isChangeAt(minute)) {
              continue;
            }
            Formation f = cp.getFormationAt(minute);

            for (Player p : f.players()) {
              for (Role r : Role.values()) {
                if (r != f.findRole(p) && r != Role.GK) {
                  adds.add(new AddChange(ChangePosition.create(p, r, minute)));
                }
              }
            }
          }
        }

        return adds;
      }
    };
  }

  private static class AddChange implements Action<ChangePlan> {

    private final Change add;

    public AddChange(Change add) {
      super();
      this.add = add;
    }

    public ChangePlan apply(ChangePlan cp) {
      return cp.with(add);
    }

    public boolean equals(Object obj) {
      if (obj == null) {
        return false;
      }
      if (obj == this) {
        return true;
      }
      if (!(obj instanceof AddChange)) {
        return false;
      }

      AddChange rhs = AddChange.class.cast(obj);

      return Objects.equals(add, rhs.add);
    }

    public int hashCode() {
      return Objects.hash(add);
    }
  }
}
