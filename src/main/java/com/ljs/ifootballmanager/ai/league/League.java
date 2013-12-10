package com.ljs.ifootballmanager.ai.league;

import com.ljs.ifootballmanager.ai.Role;

/**
 *
 * @author lstephen
 */
public interface League {

    String getTeam();

    Integer getMaximum(Role r);
    Integer getMinimum(Role r);

}
