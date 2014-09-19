package com.ljs.ifootballmanager.ai.league;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.ljs.ifootballmanager.ai.formation.validate.FormationValidator;
import com.ljs.ifootballmanager.ai.formation.validate.FormationValidatorFactory;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.rating.Weightings;
import com.ljs.ifootballmanager.ai.value.Potential;
import com.ljs.ifootballmanager.ai.value.Value;
import com.ljs.ifootballmanager.ai.value.impl.EflValue;
import com.ljs.ifootballmanager.ai.value.impl.JaflPotential;

/**
 *
 * @author lstephen
 */
public final class EliteFootballLeague implements League {

    private EliteFootballLeague() { }

    public String getTeam() {
        return "tth";
    }

    public Optional<String> getReserveTeam() {
        return Optional.of("tthr");
    }

    public Iterable<String> getForcedPlay() {
        return ImmutableList.of("R_Liverani", "Kike", "A_Andreasen", "J_Wallis");
    }

    public FormationValidator getFormationValidator() {
        return FormationValidatorFactory.efl();
    }

    public Iterable<String> getAdditionalPlayerFiles() {
        return ImmutableList.of("/for_transfer.txt", "/for_auction.txt", "/mnc.txt", "/val.txt");
    }

    public Weightings getWeightings() {
        return com.ljs.ifootballmanager.ai.rating.weighting.EliteFootballLeague.get();
    }

    public Boolean isReserveEligible(Player p) {
        return p.getMaximumSkill() < 22.5 || (p.isGk() && p.getMaximumSkill() < 26.5);
    }

     public Value getPlayerValue() {
         return EflValue.create();
     }

    public Potential getPlayerPotential() {
        return JaflPotential.create();
    }

    public Optional<Double> getSeniorSkillsCap() {
        return Optional.absent();
    }

    public Optional<Double> getYouthSkillsCap() {
        return Optional.absent();
    }

    public static EliteFootballLeague create() {
        return new EliteFootballLeague();
    }

}
