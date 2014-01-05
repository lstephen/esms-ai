package com.ljs.ifootballmanager.ai.league;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.formation.validate.CountingFormationValidator;
import com.ljs.ifootballmanager.ai.formation.validate.FormationValidator;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.rating.Weightings;
import com.ljs.ifootballmanager.ai.rating.weighting.WeightingsFactory;
import com.ljs.ifootballmanager.ai.value.PlayerValue;
import com.ljs.ifootballmanager.ai.value.player.JaflPlayerValue;

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

    public Optional<String> getReserveTeam() {
        return Optional.of("gly");
    }

    public Iterable<String> getForcedPlay() {
        return ImmutableList.of("Luigi", "H_Byakkotai", "Ramazinho");
    }

    public FormationValidator getFormationValidator() {
        return CountingFormationValidator
            .builder()
            .exactly(1, Role.GK)
            .range(3, 5, Role.DF)
            .range(2, 5, Role.DM, Role.MF, Role.AM)
            .range(1, 5, Role.FW)
            .build();
    }

    public Iterable<String> getAdditionalPlayerFiles() {
        return ImmutableList.of("/for_loan.txt", "/for_sale.txt", "/fre.txt");
    }

    public Weightings getWeightings() {
        return WeightingsFactory.ssl();
    }

    @Override
    public Boolean isReserveEligible(Player p) {
        return p.getAge() <= 19 && p.getMaximumSkill() <= 18;
    }

    public PlayerValue getPlayerValue() {
        return JaflPlayerValue.create();
    }

    public static Jafl get() {
        return INSTANCE;
    }

}
