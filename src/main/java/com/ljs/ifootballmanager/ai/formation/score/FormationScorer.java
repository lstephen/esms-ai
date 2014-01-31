package com.ljs.ifootballmanager.ai.formation.score;

import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.formation.Formation;
import java.io.PrintWriter;

/**
 *
 * @author lstephen
 */
public interface FormationScorer {

    Double score(Formation f, Tactic t);

    Double scoring(Formation f, Tactic t);

    Double defending(Formation f, Tactic t);

    void print(Formation f, PrintWriter w);

}
