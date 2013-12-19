package com.ljs.ifootballmanager.ai.rating;

import com.ljs.ifootballmanager.ai.Tactic;

/**
 *
 * @author lstephen
 */
public interface Weightings {

    TacticWeightings forTactic(Tactic t);

}
