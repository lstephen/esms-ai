package com.ljs.ifootballmanager.ai.selection;

import com.ljs.ifootballmanager.ai.Tactic;
import java.io.PrintWriter;

/**
 *
 * @author lstephen
 */
public class TacticChange {

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
        w.format("TACTIC %s IF MIN = %d%n", tactic.getCode(), minute);
    }

    public static TacticChange create(Tactic t, Integer m) {
        return new TacticChange(t, m);
    }


}
