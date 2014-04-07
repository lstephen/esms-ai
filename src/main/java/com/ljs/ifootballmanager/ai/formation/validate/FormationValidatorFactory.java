package com.ljs.ifootballmanager.ai.formation.validate;

import com.ljs.ifootballmanager.ai.Role;

/**
 *
 * @author lstephen
 */
public final class FormationValidatorFactory {

    private FormationValidatorFactory() { }

    public static FormationValidator ssl() {
        return CountingFormationValidator
            .builder()
            .exactly(1, Role.GK)
            .range(3, 5, Role.DF)
            .range(2, 6, Role.DM, Role.MF, Role.AM)
            .max(3, Role.DM)
            .max(3, Role.AM)
            .range(1, 4, Role.FW)
            .build();
    }

    public static FormationValidator jusCup() {
        return ssl();
    }

    public static FormationValidator jafl() {
        return CountingFormationValidator
            .builder()
            .exactly(1, Role.GK)
            .range(3, 5, Role.DF)
            .range(2, 5, Role.DM, Role.MF, Role.AM)
            .range(1, 5, Role.FW)
            .build();
    }

}
