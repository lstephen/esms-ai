package com.ljs.ifootballmanager.ai.selection;

import com.google.common.base.Function;
import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.formation.Formation;
import com.ljs.ifootballmanager.ai.player.Player;
import java.io.PrintWriter;

/**
 *
 * @author lstephen
 */
public final class TacticChange implements Change {

    private final Tactic tactic;

    private final Integer minute;

    private TacticChange(Tactic t, Integer m) {
        this.tactic = t;
        this.minute = m;
    }

    public Tactic getTactic() {
        return tactic;
    }

    public Integer getMinute() {
        return minute;
    }

    public void print(PrintWriter w) {
        w.format("TACTIC %s IF MIN >= %d AND SCORE = 0%n", tactic.getCode(), minute);
    }

    public void print(PrintWriter w, Function<Player, Integer> playerIdx) {
        print(w);
    }

    public Boolean isValid(ChangePlan cp) {
        return true;
    }

    public Formation apply(Formation f, Integer minute) {
        return f.withTactic(tactic);
    }

    public static TacticChange create(Tactic t, Integer m) {
        return new TacticChange(t, m);
    }


}
