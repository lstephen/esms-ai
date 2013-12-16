package com.ljs.ifootballmanager.ai.rating;

import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.Tactic;

/**
 *
 * @author lstephen
 */
public interface Weightings {

    Weighting get(Role r, Tactic t);

}
