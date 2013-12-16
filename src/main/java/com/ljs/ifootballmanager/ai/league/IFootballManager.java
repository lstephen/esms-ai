package com.ljs.ifootballmanager.ai.league;

import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.formation.CountingFormationValidator;
import com.ljs.ifootballmanager.ai.formation.FormationValidator;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.rating.Weightings;
import com.ljs.ifootballmanager.ai.rating.weighting.WeightingsFactory;
import java.util.Collections;

/**
 http://ifootballmanager.webs.com/liv.txt*
 * @author lstephen
 */
public class IFootballManager implements League {

    private static final IFootballManager INSTANCE = new IFootballManager();

    private IFootballManager() { }

    public String getTeam() {
        return "liv";
    }

    public FormationValidator getFormationValidator() {
        return CountingFormationValidator
            .builder()
            .exactly(1, Role.GK)
            .range(3, 6, Role.DF)
            .min(2, Role.MF)
            .max(3, Role.DM)
            .max(3, Role.AM)
            .max(5, Role.DM, Role.MF, Role.AM)
            .max(5, Role.FW)
            .build();

    }

    public Iterable<String> getAdditionalPlayerFiles() {
        return Collections.emptySet();
    }

    public Weightings getWeightings() {
        return WeightingsFactory.ssl();
    }

    public Boolean isReserveEligible(Player p) {
        return Boolean.FALSE;
    }

    public static IFootballManager get() {
        return INSTANCE;
    }


}
