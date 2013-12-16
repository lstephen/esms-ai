package com.ljs.ifootballmanager.ai.league;

import com.ljs.ifootballmanager.ai.formation.FormationValidator;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.rating.Weightings;

/**
 *
 * @author lstephen
 */
public interface League {

    String getTeam();

    FormationValidator getFormationValidator();

    Iterable<String> getAdditionalPlayerFiles();

    Weightings getWeightings();

    Boolean isReserveEligible(Player p);

}
