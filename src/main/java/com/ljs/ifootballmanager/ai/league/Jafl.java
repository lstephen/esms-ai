package com.ljs.ifootballmanager.ai.league;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.ljs.ifootballmanager.ai.formation.validate.FormationValidator;
import com.ljs.ifootballmanager.ai.formation.validate.FormationValidatorFactory;
import com.ljs.ifootballmanager.ai.formation.validate.PlayerValidator;
import com.ljs.ifootballmanager.ai.formation.validate.PlayerValidatorFactory;
import com.ljs.ifootballmanager.ai.info.InfoValue;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.rating.Weightings;
import com.ljs.ifootballmanager.ai.rating.weighting.WeightingsFactory;
import com.ljs.ifootballmanager.ai.value.Potential;
import com.ljs.ifootballmanager.ai.value.Value;
import com.ljs.ifootballmanager.ai.value.impl.JaflPotential;
import com.ljs.ifootballmanager.ai.value.impl.JaflValue;

/**
 *
 * @author lstephen
 */
public class Jafl implements League {

    private static final Jafl INSTANCE = new Jafl();

    private Jafl() { }

    public String getTeam() {
        return "gli";
    }

    public Optional<String> getVs() {
        return Optional.absent();
    }

    public Optional<String> getReserveTeam() {
        return Optional.of("gly");
    }

    public Optional<Double> getSeniorSkillsCap() {
        return Optional.absent();
    }

    public Optional<Double> getYouthSkillsCap() {
        // 18 for cup games, 16 for league games
        return Optional.of(16.0);
    }

    public Iterable<String> getForcedPlay() {
        return ImmutableList.of();
    }

    public FormationValidator getFormationValidator() {
        return FormationValidatorFactory.jafl();
        //return FormationValidatorFactory.jusCup();
    }

    public PlayerValidator getPlayerValidator() {
        return PlayerValidatorFactory.anyRole();
    }

    public Iterable<String> getAdditionalPlayerFiles() {
        return ImmutableList.of("/for_sale.txt", "/for_loan.txt", "/for_draft.txt", "/fre.txt");
    }

    public Weightings getWeightings() {
        return WeightingsFactory.ssl();
    }

    @Override
    public Boolean isReserveEligible(Player p) {
        return p.getAge() <= 19;
    }

    public Value getPlayerValue() {
        return JaflValue.create();
    }

    public Potential getPlayerPotential() {
        return JaflPotential.create();
    }

    public Optional<InfoValue> getInfoValue() {
        return Optional.absent();
    }

    public static Jafl get() {
        return INSTANCE;
    }

}
