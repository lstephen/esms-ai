package com.ljs.ifootballmanager.ai.league;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.ljs.ifootballmanager.ai.formation.validate.FormationValidator;
import com.ljs.ifootballmanager.ai.formation.validate.FormationValidatorFactory;
import com.ljs.ifootballmanager.ai.formation.validate.PlayerValidator;
import com.ljs.ifootballmanager.ai.formation.validate.PlayerValidatorFactory;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.rating.Weightings;
import com.ljs.ifootballmanager.ai.value.Potential;
import com.ljs.ifootballmanager.ai.value.Value;
import com.ljs.ifootballmanager.ai.value.impl.EslPotential;
import com.ljs.ifootballmanager.ai.value.impl.EslValue;

/** @author lstephen */
public final class Ffo implements League {

  private Ffo() {}

  public String getTeam() {
    return "csk";
  }

  public Optional<String> getReserveTeam() {
    return Optional.absent();
  }

  public Iterable<String> getForcedPlay() {
    return ImmutableList.of("G_Jesus");
  }

  public FormationValidator getFormationValidator() {
    return FormationValidatorFactory.ffo();
  }

  public PlayerValidator getPlayerValidator() {
    return PlayerValidatorFactory.anyRole();
  }

  public Iterable<String> getAdditionalPlayerFiles() {
    return ImmutableList.of();
  }

  public Weightings getWeightings() {
    return com.ljs.ifootballmanager.ai.rating.weighting.Ffo.get();
  }

  public Boolean isReserveEligible(Player p) {
    return Boolean.FALSE;
  }

  public Value getPlayerValue() {
    return FfoValue.create();
  }

  public Value getAgeValue() {
    return FfoValue.create().getAgeValue();
  }

  public Potential getPlayerPotential() {
    return FfoPotential.create();
  }

  public Optional<Double> getSeniorSkillsCap() {
    return Optional.absent();
  }

  public Optional<Double> getYouthSkillsCap() {
    return Optional.absent();
  }

  public static Esl create() {
    return new Esl();
  }
}
