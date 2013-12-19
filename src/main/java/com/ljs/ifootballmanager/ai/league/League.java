package com.ljs.ifootballmanager.ai.league;

import com.google.common.base.Optional;
import com.ljs.ifootballmanager.ai.formation.validate.FormationValidator;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.rating.Weightings;

/**
 *
 * @author lstephen
 */
public interface League {

    String getTeam();

    Optional<String> getReserveTeam();

    Iterable<String> getForcedPlay();

    FormationValidator getFormationValidator();

    Iterable<String> getAdditionalPlayerFiles();

    Weightings getWeightings();

    Boolean isReserveEligible(Player p);

}
