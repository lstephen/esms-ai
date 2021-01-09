package com.ljs.ifootballmanager.ai.league;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.ljs.ifootballmanager.ai.formation.validate.FormationValidator;
import com.ljs.ifootballmanager.ai.formation.validate.FormationValidatorFactory;
import com.ljs.ifootballmanager.ai.formation.validate.PlayerValidator;
import com.ljs.ifootballmanager.ai.formation.validate.PlayerValidatorFactory;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.rating.Weightings;
import com.ljs.ifootballmanager.ai.value.Value;
import com.ljs.ifootballmanager.ai.value.impl.EslValue;

/** @author lstephen */
public final class Esl implements League {

  private Esl() {}

  public String getTeam() {
    return "wba";
  }

  public Optional<String> getReserveTeam() {
    return Optional.absent();
  }

  public Iterable<String> getForcedPlay() {
    return ImmutableList.of();
    // return ImmutableList.of("R_Liverani", "G_Bengescu", "P_Singh", "J_Wallis");
  }

  public FormationValidator getFormationValidator() {
    return FormationValidatorFactory.esl();
  }

  public PlayerValidator getPlayerValidator() {
    return PlayerValidatorFactory.anyRole();
    // return PlayerValidatorFactory.inPrimaryRole();
  }

  public Iterable<String> getAdditionalPlayerFiles() {
    return ImmutableList.of("/for_auction.txt");
  }

  public Weightings getWeightings() {
    return com.ljs.ifootballmanager.ai.rating.weighting.Esl.get();
  }

  public Boolean isReserveEligible(Player p) {
    return false;
  }

  public Value getAgeValue() {
    return EslValue.create().getAgeValue();
  }

  public Optional<Double> getSeniorSkillsCap() {
    return Optional.absent();
  }

  public Optional<Double> getYouthSkillsCap() {
    return Optional.absent();
  }

  @Override
  public boolean isAllowedChangePos() {
    return false;
  }

  public static Esl create() {
    return new Esl();
  }
}
