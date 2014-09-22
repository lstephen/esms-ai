package com.ljs.ifootballmanager.ai.formation.validate;

import com.ljs.ifootballmanager.ai.formation.Formation;

/**
 *
 * @author lstephen
 */
public final class FormationValidators {

    private FormationValidators() { }

    public static FormationValidator allOf(final FormationValidator... fvs) {
        return new FormationValidator() {
            @Override
            public Boolean isValid(Formation f) {
                for (FormationValidator fv : fvs) {
                    if (!fv.isValid(f)) {
                        return false;
                    }
                }
                return true;
            }
        };
    }
}
