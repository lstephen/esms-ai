package com.ljs.ifootballmanager.ai.formation.score;

import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.formation.Formation;
import java.io.PrintWriter;

/**
 *
 * @author lstephen
 */
public interface FormationScorer {

    double score(Formation f, Tactic t);

    double scoring(Formation f, Tactic t);

    double defending(Formation f, Tactic t);

    void print(Formation f, PrintWriter w);

}
