package com.ljs.ifootballmanager.ai.league;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.formation.validate.CountingFormationValidator;
import com.ljs.ifootballmanager.ai.formation.validate.FormationValidator;
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

    private final Optional<String> vs;

    private IFootballManager(String team, Optional<String> vs) {
        this.team = team;
        this.vs = vs;
    }

    public String getTeam() {
        return team;
    }

    public Optional<String> getVs() {
        return vs;
    }

    public Optional<String> getReserveTeam() {
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

    public Iterable<String> getAdditionalPlayerFiles() {
        return ImmutableList.<String>of();
    }

    public Weightings getWeightings() {
        return WeightingsFactory.ssl();
    }

    public Boolean isReserveEligible(Player p) {
        return Boolean.FALSE;
    }

    public static IFootballManager create(String team) {
        return create(team, Optional.<String>absent());
    }

    public static IFootballManager create(String team, String vs) {
        return create(team, Optional.of(vs));
    }

    private static IFootballManager create(String team, Optional<String> vs) {
        return new IFootballManager(team, vs);
    }

    public Value getPlayerValue() {
        return IFootballManagerValue.create();
    }

    public Potential getPlayerPotential() {
        return NullPotential.create();
    }


}
