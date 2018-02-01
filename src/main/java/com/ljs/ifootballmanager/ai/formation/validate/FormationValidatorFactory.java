package com.ljs.ifootballmanager.ai.formation.validate;

import com.ljs.ifootballmanager.ai.Role;

/** @author lstephen */
public final class FormationValidatorFactory {

  private FormationValidatorFactory() {}

  public static FormationValidator ssl() {
    return CountingFormationValidator.builder()
        .exactly(1, Role.GK)
        .range(3, 5, Role.DF)
        .range(2, 6, Role.DM, Role.MF, Role.AM)
        .max(3, Role.DM)
        .max(3, Role.AM)
        .range(1, 4, Role.FW)
        .build();
  }

  public static FormationValidator efl() {
    return CountingFormationValidator.builder()
        .exactly(1, Role.GK)
        .range(3, 6, Role.DF)
        .range(2, 6, Role.DM, Role.MF, Role.AM)
        .max(3, Role.DM)
        .max(3, Role.AM)
        .range(1, 4, Role.FW)
        .build();
  }

  public static FormationValidator ffo() {
    return CountingFormationValidator.builder()
        .exactly(1, Role.GK)
        .range(3, 5, Role.DF)
        .range(1, 6, Role.DM, Role.MF, Role.AM)
        .max(2, Role.DM)
        .max(5, Role.MF)
        .max(3, Role.AM)
        .range(0, 4, Role.FW)
        .build();
  }
}
