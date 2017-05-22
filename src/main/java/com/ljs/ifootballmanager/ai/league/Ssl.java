package com.ljs.ifootballmanager.ai.league;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.ljs.ifootballmanager.ai.formation.validate.FormationValidator;
import com.ljs.ifootballmanager.ai.formation.validate.FormationValidatorFactory;
import com.ljs.ifootballmanager.ai.formation.validate.PlayerValidator;
import com.ljs.ifootballmanager.ai.formation.validate.PlayerValidatorFactory;
import com.ljs.ifootballmanager.ai.info.InfoValue;
import com.ljs.ifootballmanager.ai.info.SslInfoValue;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.rating.Weightings;
import com.ljs.ifootballmanager.ai.rating.weighting.WeightingsFactory;
import com.ljs.ifootballmanager.ai.value.Potential;
import com.ljs.ifootballmanager.ai.value.Value;
import com.ljs.ifootballmanager.ai.value.impl.SslPotential;
import com.ljs.ifootballmanager.ai.value.impl.SslValue;

/** @author lstephen */
public class Ssl implements League {

  private final String team;

  private final Optional<String> reserveTeam;

  private Ssl(String team, Optional<String> reserveTeam) {
    this.team = team;
    this.reserveTeam = reserveTeam;
  }

  public String getTeam() {
    return team;
  }

  public Optional<String> getVs() {
    return Optional.absent();
  }

  public Optional<String> getReserveTeam() {
    return reserveTeam;
  }

  public Optional<Double> getSeniorSkillsCap() {
    return Optional.of(27.0);
    // JUS cup
    //return Optional.absent();
  }

  public Optional<Double> getYouthSkillsCap() {
    return Optional.of(18.0);
  }

  public Iterable<String> getForcedPlay() {
    return ImmutableList.<String>of();
  }

  public FormationValidator getFormationValidator() {
    return FormationValidatorFactory.ssl();
    //return FormationValidatorFactory.jusCup();
  }

  public PlayerValidator getPlayerValidator() {
    return PlayerValidatorFactory.anyRole();
  }

  public Iterable<String> getAdditionalPlayerFiles() {
    return ImmutableList.of("/free_agents.txt", "/for_auction.txt", "/for_sale.txt");
  }

  public Weightings getWeightings() {
    return WeightingsFactory.ssl();
  }

  @Override
  public Boolean isReserveEligible(Player p) {
    return p.getAge() <= 21;
  }

  public Value getPlayerValue() {
    return SslValue.create();
  }

  public Value getAgeValue() {
    return SslValue.create().getAgeValue();
  }

  public Potential getPlayerPotential() {
    return SslPotential.create();
  }

  public Optional<InfoValue> getInfoValue() {
    return Optional.<InfoValue>of(SslInfoValue.get());
  }

  public static Ssl create(String team) {
    return new Ssl(team, Optional.<String>absent());
  }

  public static Ssl create(String team, String reserveTeam) {
    return new Ssl(team, Optional.of(reserveTeam));
  }
}
