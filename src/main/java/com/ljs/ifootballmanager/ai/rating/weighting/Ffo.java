package com.ljs.ifootballmanager.ai.rating.weighting;

import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.rating.TacticWeightings;
import com.ljs.ifootballmanager.ai.rating.Weighting;
import com.ljs.ifootballmanager.ai.rating.Weightings;

/**
 * http://www.pbemff.co.uk/general/numhelp.txt
 *
 * @author lstephen
 */
public class Ffo implements Weightings {

  private static final Ffo INSTANCE = new Ffo();

  private static final Table TABLE =
      Table.builder()
          .forAllTactics(Role.GK, Weighting.builder().st(150).build())
          .add(Role.DF, Tactic.NORMAL, Weighting.create(100, 50, 30))
          .add(Role.MF, Tactic.NORMAL, Weighting.create(30, 100, 30))
          .add(Role.FW, Tactic.NORMAL, Weighting.create(30, 30, 100))
          .add(Role.DM, Tactic.NORMAL, Weighting.create(67, 87, 30))
          .add(Role.AM, Tactic.NORMAL, Weighting.create(30, 87, 67))
          .add(Role.DF, Tactic.DEFENSIVE, Weighting.create(115, 25, 0))
          .add(Role.MF, Tactic.DEFENSIVE, Weighting.create(100, 75, 20))
          .add(Role.FW, Tactic.DEFENSIVE, Weighting.create(20, 25, 65))
          .add(Role.DM, Tactic.DEFENSIVE, Weighting.create(100, 65, 0))
          .add(Role.AM, Tactic.DEFENSIVE, Weighting.create(55, 65, 50))
          .add(Role.DF, Tactic.ATTACKING, Weighting.create(100, 50, 50))
          .add(Role.MF, Tactic.ATTACKING, Weighting.create(0, 100, 75))
          .add(Role.FW, Tactic.ATTACKING, Weighting.create(0, 65, 150))
          .add(Role.DM, Tactic.ATTACKING, Weighting.create(50, 90, 50))
          .add(Role.AM, Tactic.ATTACKING, Weighting.create(0, 90, 115))
          .add(Role.DF, Tactic.COUNTER_ATTACK, Weighting.create(100, 50, 25))
          .add(Role.MF, Tactic.COUNTER_ATTACK, Weighting.create(50, 100, 25))
          .add(Role.FW, Tactic.COUNTER_ATTACK, Weighting.create(50, 50, 100))
          .add(Role.DM, Tactic.COUNTER_ATTACK, Weighting.create(75, 85, 25))
          .add(Role.AM, Tactic.COUNTER_ATTACK, Weighting.create(25, 85, 75))
          .add(Role.DF, Tactic.LONG_BALL, Weighting.create(110, 60, 10))
          .add(Role.MF, Tactic.LONG_BALL, Weighting.create(50, 70, 65))
          .add(Role.FW, Tactic.LONG_BALL, Weighting.create(10, 30, 130))
          .add(Role.DM, Tactic.LONG_BALL, Weighting.create(75, 60, 50))
          .add(Role.AM, Tactic.LONG_BALL, Weighting.create(30, 50, 100))
          .add(Role.DF, Tactic.PASSING, Weighting.create(100, 75, 30))
          .add(Role.MF, Tactic.PASSING, Weighting.create(25, 100, 25))
          .add(Role.FW, Tactic.PASSING, Weighting.create(25, 75, 100))
          .add(Role.DM, Tactic.PASSING, Weighting.create(65, 85, 20))
          .add(Role.AM, Tactic.PASSING, Weighting.create(25, 85, 65))
          .add(Role.DF, Tactic.EUROPEAN, Weighting.create(100, 30, 40))
          .add(Role.MF, Tactic.EUROPEAN, Weighting.create(25, 125, 40))
          .add(Role.FW, Tactic.EUROPEAN, Weighting.create(20, 40, 95))
          .add(Role.DM, Tactic.EUROPEAN, Weighting.create(75, 100, 15))
          .add(Role.AM, Tactic.EUROPEAN, Weighting.create(15, 100, 75))
          .build();

  private Ffo() {}

  public TacticWeightings forTactic(Tactic t) {
    return TABLE.forTactic(t);
  }

  public static Ffo get() {
    return INSTANCE;
  }
}
