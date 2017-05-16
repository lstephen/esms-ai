package com.ljs.ifootballmanager.ai.formation.validate;

import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.rating.Ratings;

/** @author lstephen */
public interface PlayerValidator {

  boolean isAllowedInRole(Ratings rt, Role rl);
}
