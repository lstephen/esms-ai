package com.ljs.ifootballmanager.ai.search;

/**
 *
 * @author lstephen
 */
public interface State {

    Boolean isValid();

    Integer score();

}
