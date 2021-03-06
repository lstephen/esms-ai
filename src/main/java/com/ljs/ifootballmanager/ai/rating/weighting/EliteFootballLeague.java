package com.ljs.ifootballmanager.ai.rating.weighting;

import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.rating.TacticWeightings;
import com.ljs.ifootballmanager.ai.rating.Weighting;
import com.ljs.ifootballmanager.ai.rating.Weightings;

/**
 * @see <a href="http://www.elitefootballleague.co.uk/index.php?action=hp&select=numhelp.txt">
 *     http://www.elitefootballleague.co.uk/index.php?action=hp&amp;select=numhelp.txt</a>
 * @author lstephen
 */
public class EliteFootballLeague implements Weightings {

  private static final EliteFootballLeague INSTANCE = new EliteFootballLeague();

  private static final Table TABLE =
      Table.builder()
          .forAllTactics(Role.GK, Weighting.builder().st(150).build())
          .add(Role.DF, Tactic.ATTACKING, Weighting.create(100, 50, 50))
          .add(Role.DM, Tactic.ATTACKING, Weighting.create(50, 90, 50))
          .add(Role.MF, Tactic.ATTACKING, Weighting.create(0, 100, 75))
          .add(Role.AM, Tactic.ATTACKING, Weighting.create(0, 90, 115))
          .add(Role.FW, Tactic.ATTACKING, Weighting.create(0, 65, 150))
          .add(Role.DF, Tactic.COUNTER_ATTACK, Weighting.create(100, 50, 25))
          .add(Role.DM, Tactic.COUNTER_ATTACK, Weighting.create(75, 85, 25))
          .add(Role.MF, Tactic.COUNTER_ATTACK, Weighting.create(50, 100, 25))
          .add(Role.AM, Tactic.COUNTER_ATTACK, Weighting.create(25, 85, 75))
          .add(Role.FW, Tactic.COUNTER_ATTACK, Weighting.create(50, 50, 100))
          .add(Role.DF, Tactic.DEFENSIVE, Weighting.create(125, 25, 0))
          .add(Role.DM, Tactic.DEFENSIVE, Weighting.create(110, 65, 5))
          .add(Role.MF, Tactic.DEFENSIVE, Weighting.create(100, 75, 25))
          .add(Role.AM, Tactic.DEFENSIVE, Weighting.create(65, 65, 50))
          .add(Role.FW, Tactic.DEFENSIVE, Weighting.create(50, 25, 75))
          .add(Role.DF, Tactic.EUROPEAN, Weighting.create(90, 30, 40))
          .add(Role.DM, Tactic.EUROPEAN, Weighting.create(75, 100, 15))
          .add(Role.MF, Tactic.EUROPEAN, Weighting.create(25, 125, 40))
          .add(Role.AM, Tactic.EUROPEAN, Weighting.create(15, 100, 75))
          .add(Role.FW, Tactic.EUROPEAN, Weighting.create(20, 40, 90))
          .add(Role.DF, Tactic.LONG_BALL, Weighting.create(110, 25, 35))
          .add(Role.DM, Tactic.LONG_BALL, Weighting.create(75, 40, 50))
          .add(Role.MF, Tactic.LONG_BALL, Weighting.create(150, 50, 65))
          .add(Role.AM, Tactic.LONG_BALL, Weighting.create(30, 40, 90))
          .add(Role.FW, Tactic.LONG_BALL, Weighting.create(25, 25, 130))
          .add(Role.DF, Tactic.NORMAL, Weighting.create(100, 50, 30))
          .add(Role.DM, Tactic.NORMAL, Weighting.create(67, 87, 30))
          .add(Role.MF, Tactic.NORMAL, Weighting.create(30, 100, 30))
          .add(Role.AM, Tactic.NORMAL, Weighting.create(30, 87, 67))
          .add(Role.FW, Tactic.NORMAL, Weighting.create(30, 30, 100))
          .add(Role.DF, Tactic.PASSING, Weighting.create(100, 75, 30))
          .add(Role.DM, Tactic.PASSING, Weighting.create(65, 85, 20))
          .add(Role.MF, Tactic.PASSING, Weighting.create(25, 100, 25))
          .add(Role.AM, Tactic.PASSING, Weighting.create(25, 85, 65))
          .add(Role.FW, Tactic.PASSING, Weighting.create(25, 75, 100))
          .build();

  private EliteFootballLeague() {}

  public TacticWeightings forTactic(Tactic t) {
    return TABLE.forTactic(t);
  }

  public static EliteFootballLeague get() {
    return INSTANCE;
  }
}
