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
import com.ljs.ifootballmanager.ai.value.impl.EslValue;
import com.ljs.ifootballmanager.ai.value.impl.EslPotential;

/**
 *
 * @author lstephen
 */
public final class Esl implements League {

    private Esl() { }

    public String getTeam() {
        return "wat";
    }

    public Optional<String> getReserveTeam() {
        return Optional.absent();
    }

    public Iterable<String> getForcedPlay() {
      return ImmutableList.of();
    }

    public FormationValidator getFormationValidator() {
        return FormationValidatorFactory.esl();
    }

    public PlayerValidator getPlayerValidator() {
        return PlayerValidatorFactory.anyRole();
    }

    public Iterable<String> getAdditionalPlayerFiles() {
        return ImmutableList.of();
    }

    public Weightings getWeightings() {
        return com.ljs.ifootballmanager.ai.rating.weighting.EliteFootballLeague.get();
    }

    public Boolean isReserveEligible(Player p) {
      return Boolean.FALSE;
    }

     public Value getPlayerValue() {
       return EslValue.create();
     }

    public Potential getPlayerPotential() {
      return EslPotential.create();
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
