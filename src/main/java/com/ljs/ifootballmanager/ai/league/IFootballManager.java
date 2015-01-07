package com.ljs.ifootballmanager.ai.league;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.formation.validate.CountingFormationValidator;
import com.ljs.ifootballmanager.ai.formation.validate.FormationValidator;
import com.ljs.ifootballmanager.ai.formation.validate.PlayerValidator;
import com.ljs.ifootballmanager.ai.formation.validate.PlayerValidatorFactory;
import com.ljs.ifootballmanager.ai.info.InfoValue;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.rating.Weightings;
import com.ljs.ifootballmanager.ai.rating.weighting.WeightingsFactory;
import com.ljs.ifootballmanager.ai.value.Potential;
import com.ljs.ifootballmanager.ai.value.Value;
import com.ljs.ifootballmanager.ai.value.impl.IFootballManagerValue;
import com.ljs.ifootballmanager.ai.value.impl.NullPotential;
import java.util.Collections;

/**
 http://ifootballmanager.webs.com/liv.txt*
 * @author lstephen
 */
public final class IFootballManager implements League {

    private final String team;

    private IFootballManager(String team) {
        this.team = team;
    }

    public String getTeam() {
        return team;
    }

    public Optional<String> getReserveTeam() {
        return Optional.absent();
    }

    public Optional<Double> getSeniorSkillsCap() {
        return Optional.absent();
    }

    public Optional<Double> getYouthSkillsCap() {
        return Optional.absent();
    }

    public Iterable<String> getForcedPlay() {
        return Collections.emptySet();
    }

    public FormationValidator getFormationValidator() {
        return CountingFormationValidator
            .builder()
            .exactly(1, Role.GK)
            .range(3, 6, Role.DF)
            .range(2, 5, Role.DM, Role.MF, Role.AM)
            .max(3, Role.DM)
            .max(3, Role.AM)
            .max(5, Role.FW)
            .build();

    }

    public PlayerValidator getPlayerValidator() {
        return PlayerValidatorFactory.anyRole();
    }

    public Iterable<String> getAdditionalPlayerFiles() {
        return ImmutableList.of("/for_transfer.txt");
    }

    public Weightings getWeightings() {
        return WeightingsFactory.ssl();
    }

    public Boolean isReserveEligible(Player p) {
        return Boolean.FALSE;
    }

    public static IFootballManager create(String team) {
        return new IFootballManager(team);
    }

    public Value getPlayerValue() {
        return IFootballManagerValue.create();
    }

    public Optional<InfoValue> getInfoValue() {
        return Optional.absent();
    }

    public Potential getPlayerPotential() {
        return NullPotential.create();
    }

}
