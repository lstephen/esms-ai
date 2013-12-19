package com.ljs.ifootballmanager.ai.rating;

import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.Tactic;

/**
 *
 * @author lstephen
 */
public interface TacticWeightings {

    Weighting inRole(Role r);

    TacticWeightings vs(Tactic t);

}
