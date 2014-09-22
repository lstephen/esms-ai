package com.ljs.ifootballmanager.ai.formation.validate;

import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.rating.Ratings;

/**
 *
 * @author lstephen
 */
public final class InPrimaryRoleValidator implements PlayerValidator {

    private InPrimaryRoleValidator() { }

    public boolean isAllowedInRole(Ratings rs, Role rl) {
        switch (rs.getPrimarySkill()) {
            case STOPPING:
                return rl.isOneOf(Role.GK);
            case TACKLING:
                return rl.isOneOf(Role.DF, Role.DM);
            case PASSING:
                return rl.isOneOf(Role.DM, Role.MF, Role.AM);
            case SHOOTING:
                return rl.isOneOf(Role.AM, Role.FW);
            default:
                throw new IllegalArgumentException(
                    "Unknown skill: " + rs.getPrimarySkill());
        }
    }

    public static InPrimaryRoleValidator create() {
        return new InPrimaryRoleValidator();
    }


}
