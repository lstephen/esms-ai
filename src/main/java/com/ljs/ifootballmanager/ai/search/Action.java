package com.ljs.ifootballmanager.ai.search;

/**
 *
 * @author lstephen
 */
public abstract class Action<S extends State> implements aima.core.agent.Action {

    @Override
    public boolean isNoOp() {
        return false;
    }

    public Boolean isValid(S state) {
        return apply(state).isValid();
    }

    public abstract S apply(S state);

}
