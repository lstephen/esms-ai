package com.ljs.ifootballmanager.ai.rating.weighting;

import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.rating.TacticWeightings;
import com.ljs.ifootballmanager.ai.rating.Weighting;
import com.ljs.ifootballmanager.ai.rating.Weightings;

/**
 *http://www.ssl2001.ukhome.net/ssl_numbers_help.txt
 * @author lstephen
 */
public final class Ssl implements Weightings {

    private static final Ssl INSTANCE = new Ssl();

    private static final Table TABLE = Table
        .builder()
        .forAllTactics(Role.GK, Weighting.builder().st(150).build())
        .add(Role.DF, Tactic.NORMAL, Weighting.create(100, 43, 43))
        .add(Role.MF, Tactic.NORMAL, Weighting.create(43, 100, 43))
        .add(Role.FW, Tactic.NORMAL, Weighting.create(43, 43, 100))
        .add(Role.DM, Tactic.NORMAL, Weighting.create(67, 87, 30))
        .add(Role.AM, Tactic.NORMAL, Weighting.create(30, 87, 67))

        .bonus(Role.MF, Tactic.NORMAL, Weighting.create(25, 0, 25), Tactic.EUROPEAN, Tactic.ATTACKING)
        .bonus(Role.FW, Tactic.NORMAL, Weighting.create(0, 25, 0), Tactic.EUROPEAN, Tactic.ATTACKING)
        .bonus(Role.AM, Tactic.NORMAL, Weighting.create(0, 13, 23), Tactic.EUROPEAN, Tactic.ATTACKING)
        .bonus(Role.DM, Tactic.NORMAL, Weighting.create(23, 0, 0), Tactic.EUROPEAN, Tactic.ATTACKING)

        .add(Role.DF, Tactic.DEFENSIVE, Weighting.create(120, 25, 0))
        .add(Role.MF, Tactic.DEFENSIVE, Weighting.create(80, 75, 25))
        .add(Role.FW, Tactic.DEFENSIVE, Weighting.create(50, 25, 75))
        .add(Role.DM, Tactic.DEFENSIVE, Weighting.create(90, 65, 13))
        .add(Role.AM, Tactic.DEFENSIVE, Weighting.create(65, 65, 50))

        .bonus(Role.DF, Tactic.DEFENSIVE, Weighting.create(25, 0, 0), Tactic.LONG_BALL, Tactic.PASSING)
        .bonus(Role.DM, Tactic.DEFENSIVE, Weighting.create(25, 0, 0), Tactic.LONG_BALL, Tactic.PASSING)

        .add(Role.DF, Tactic.ATTACKING, Weighting.create(95, 50, 75))
        .add(Role.MF, Tactic.ATTACKING, Weighting.create(0, 100, 75))
        .add(Role.FW, Tactic.ATTACKING, Weighting.create(0, 65, 150))
        .add(Role.DM, Tactic.ATTACKING, Weighting.create(25, 90, 65))
        .add(Role.AM, Tactic.ATTACKING, Weighting.create(0, 85, 115))

        .bonus(Role.FW, Tactic.ATTACKING, Weighting.create(0, 0, 50), Tactic.DEFENSIVE, Tactic.EUROPEAN)
        .bonus(Role.AM, Tactic.ATTACKING, Weighting.create(0, 0, 25), Tactic.DEFENSIVE, Tactic.EUROPEAN)

        .add(Role.DF, Tactic.COUNTER_ATTACK, Weighting.create(100, 50, 25))
        .add(Role.MF, Tactic.COUNTER_ATTACK, Weighting.create(50, 100, 25))
        .add(Role.FW, Tactic.COUNTER_ATTACK, Weighting.create(50, 50, 100))
        .add(Role.DM, Tactic.COUNTER_ATTACK, Weighting.create(75, 85, 25))
        .add(Role.AM, Tactic.COUNTER_ATTACK, Weighting.create(60, 85, 60))

        .bonus(Role.MF, Tactic.COUNTER_ATTACK, Weighting.create(0, 0, 50), Tactic.PASSING, Tactic.ATTACKING)
        .bonus(Role.DF, Tactic.COUNTER_ATTACK, Weighting.create(0, 25, 25), Tactic.PASSING, Tactic.ATTACKING)
        .bonus(Role.AM, Tactic.COUNTER_ATTACK, Weighting.create(0, 0, 40), Tactic.PASSING, Tactic.ATTACKING)
        .bonus(Role.DM, Tactic.COUNTER_ATTACK, Weighting.create(10, 0, 0), Tactic.PASSING, Tactic.ATTACKING)

        .add(Role.DF, Tactic.LONG_BALL, Weighting.create(113, 25, 80))
        .add(Role.MF, Tactic.LONG_BALL, Weighting.create(70, 50, 80))
        .add(Role.FW, Tactic.LONG_BALL, Weighting.create(25, 25, 130))
        .add(Role.DM, Tactic.LONG_BALL, Weighting.create(90, 40, 50))
        .add(Role.AM, Tactic.LONG_BALL, Weighting.create(30, 40, 110))

        .bonus(Role.DF, Tactic.LONG_BALL, Weighting.create(20, 25, 0), Tactic.COUNTER_ATTACK, Tactic.NORMAL)
        .bonus(Role.DM, Tactic.LONG_BALL, Weighting.create(10, 0, 0), Tactic.COUNTER_ATTACK, Tactic.NORMAL)

        .add(Role.DF, Tactic.PASSING, Weighting.create(100, 75, 30))
        .add(Role.MF, Tactic.PASSING, Weighting.create(25, 100, 25))
        .add(Role.FW, Tactic.PASSING, Weighting.create(25, 75, 100))
        .add(Role.DM, Tactic.PASSING, Weighting.create(50, 90, 20))
        .add(Role.AM, Tactic.PASSING, Weighting.create(20, 90, 50))

        .bonus(Role.MF, Tactic.PASSING, Weighting.create(25, 0, 25), Tactic.LONG_BALL, Tactic.NORMAL)
        .bonus(Role.FW, Tactic.PASSING, Weighting.create(0, 0, 25), Tactic.LONG_BALL, Tactic.NORMAL)
        .bonus(Role.DM, Tactic.PASSING, Weighting.create(25, 0, 0), Tactic.LONG_BALL, Tactic.NORMAL)
        .bonus(Role.AM, Tactic.PASSING, Weighting.create(0, 0, 25), Tactic.LONG_BALL, Tactic.NORMAL)

        .add(Role.DF, Tactic.EUROPEAN, Weighting.create(100, 30, 40))
        .add(Role.MF, Tactic.EUROPEAN, Weighting.create(25, 125, 40))
        .add(Role.FW, Tactic.EUROPEAN, Weighting.create(20, 40, 100))
        .add(Role.DM, Tactic.EUROPEAN, Weighting.create(70, 100, 15))
        .add(Role.AM, Tactic.EUROPEAN, Weighting.create(15, 100, 70))

        .bonus(Role.DF, Tactic.EUROPEAN, Weighting.create(25, 25, 0), Tactic.COUNTER_ATTACK, Tactic.DEFENSIVE)
        .bonus(Role.FW, Tactic.EUROPEAN, Weighting.create(0, 25, 0), Tactic.COUNTER_ATTACK, Tactic.DEFENSIVE)
        .bonus(Role.AM, Tactic.EUROPEAN, Weighting.create(0, 10, 0), Tactic.COUNTER_ATTACK, Tactic.DEFENSIVE)
        .bonus(Role.DM, Tactic.EUROPEAN, Weighting.create(0, 10, 0), Tactic.COUNTER_ATTACK, Tactic.DEFENSIVE)

        .build();

    private Ssl() { }

    public TacticWeightings forTactic(Tactic t) {
        return TABLE.forTactic(t);
    }

    public static Ssl get() {
        return INSTANCE;
    }

}
