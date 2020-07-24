package com.ljs.ifootballmanager.ai.league;

import com.google.common.base.Optional;
import com.ljs.ifootballmanager.ai.formation.validate.FormationValidator;
import com.ljs.ifootballmanager.ai.formation.validate.PlayerValidator;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.rating.Weightings;
import com.ljs.ifootballmanager.ai.value.Value;

/** @author lstephen */
public interface League {

  String getTeam();

  Optional<String> getReserveTeam();

  Iterable<String> getForcedPlay();

  FormationValidator getFormationValidator();

  PlayerValidator getPlayerValidator();

  Iterable<String> getAdditionalPlayerFiles();

  Weightings getWeightings();

  Boolean isReserveEligible(Player p);

  Value getPlayerValue();

  Value getAgeValue();

  Optional<Double> getSeniorSkillsCap();

  Optional<Double> getYouthSkillsCap();

  default boolean isAllowedChangePos() {
    return true;
  }
}
