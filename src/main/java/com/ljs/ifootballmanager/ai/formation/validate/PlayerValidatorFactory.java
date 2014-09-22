package com.ljs.ifootballmanager.ai.formation.validate;

import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.rating.Ratings;

/**
 *
 * @author lstephen
 */
public final class PlayerValidatorFactory {

    private PlayerValidatorFactory() { }

    public static PlayerValidator anyRole() {
        return new PlayerValidator() {

            @Override
            public boolean isAllowedInRole(Ratings rt, Role rl) {
                return true;
            }
        };
    }

    public static PlayerValidator inPrimaryRole() {
        return InPrimaryRoleValidator.create();
    }

}
