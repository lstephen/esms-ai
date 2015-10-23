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

/**
 *
 * @author lstephen
 */
public class Ucfl implements League {

    private final String team;

    private final Optional<String> reserveTeam;

    private Ucfl(String team, Optional<String> reserveTeam) {
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
        return Optional.absent();
    }

    public Optional<Double> getYouthSkillsCap() {
        return Optional.of(15.0);
    }

    public Iterable<String> getForcedPlay() {
        return ImmutableList.<String>of();
    }

    public FormationValidator getFormationValidator() {
        return FormationValidatorFactory.ucfl();
    }

    public PlayerValidator getPlayerValidator() {
        return PlayerValidatorFactory.anyRole();
    }

    public Iterable<String> getAdditionalPlayerFiles() {
      return ImmutableList.of();
      //return ImmutableList.of("/free_agents.txt", "/for_auction.txt", "/for_sale.txt");
    }

    public Weightings getWeightings() {
        return WeightingsFactory.ssl();
    }

    @Override
    public Boolean isReserveEligible(Player p) {
        return p.getAge() <= 20;
    }

    public Value getPlayerValue() {
        return SslValue.create();
    }

    public Potential getPlayerPotential() {
        return SslPotential.create();
    }

    public Optional<InfoValue> getInfoValue() {
        return Optional.<InfoValue>of(SslInfoValue.get());
    }

    public static Ucfl create(String team) {
        return new Ucfl(team, Optional.<String>absent());
    }

    public static Ucfl create(String team, String reserveTeam) {
        return new Ucfl(team, Optional.of(reserveTeam));
    }

}

