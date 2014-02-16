package com.ljs.ifootballmanager.ai.league;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.formation.validate.CountingFormationValidator;
import com.ljs.ifootballmanager.ai.formation.validate.FormationValidator;
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
public class Ssl implements League {

    private static final Ssl INSTANCE = new Ssl();

    private Ssl() { }

    public String getTeam() {
        return "mis";
    }

    public Optional<String> getVs() {
        return Optional.absent();
    }

    public Optional<String> getReserveTeam() {
        return Optional.of("msy");
    }

    public Optional<Double> getYouthSkillsCap() {
        return Optional.of(17.0);
    }

    public Iterable<String> getForcedPlay() {
        return ImmutableList.<String>of();
    }

    public FormationValidator getFormationValidator() {
        return CountingFormationValidator
            .builder()
            .exactly(1, Role.GK)
            .range(3, 5, Role.DF)
            .range(2, 6, Role.DM, Role.MF, Role.AM)
            .max(3, Role.DM)
            .max(3, Role.AM)
            .range(1, 4, Role.FW)
            .build();
    }

    public Iterable<String> getAdditionalPlayerFiles() {
        return ImmutableList.of("/for_sale.txt", "/free_agents.txt");
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

    public Potential getPlayerPotential() {
        return SslPotential.create();
    }

    public Optional<InfoValue> getInfoValue() {
        return Optional.<InfoValue>of(SslInfoValue.get());
    }

    public static Ssl get() {
        return INSTANCE;
    }

}
