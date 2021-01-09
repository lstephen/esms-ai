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
import com.ljs.ifootballmanager.ai.value.impl.EflValue;

/** @author lstephen */
public final class EliteFootballLeague implements League {

  private EliteFootballLeague() {}

  public String getTeam() {
    return "tth";
  }

  public Optional<String> getReserveTeam() {
    return Optional.of("tthr");
  }

  public Iterable<String> getForcedPlay() {
    return ImmutableList.of();
    // return ImmutableList.of("R_Liverani", "G_Bengescu", "P_Singh", "J_Wallis");
  }

  public FormationValidator getFormationValidator() {
    return FormationValidatorFactory.efl();
  }

  public PlayerValidator getPlayerValidator() {
    return PlayerValidatorFactory.anyRole();
    // return PlayerValidatorFactory.inPrimaryRole();
  }

  public Iterable<String> getAdditionalPlayerFiles() {
    return ImmutableList.of("/for_auction.txt", "/for_transfer.txt", "/wdb.txt");
  }

  public Weightings getWeightings() {
    return com.ljs.ifootballmanager.ai.rating.weighting.EliteFootballLeague.get();
  }

  public Boolean isReserveEligible(Player p) {
    return p.getMaximumSkill() < 24.5 || (p.isGk() && p.getMaximumSkill() < 28.5);
  }

  public Value getAgeValue() {
    return EflValue.create().getAgeValue();
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

  public static EliteFootballLeague create() {
    return new EliteFootballLeague();
  }
}
