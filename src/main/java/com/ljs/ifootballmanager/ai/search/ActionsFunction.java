package com.ljs.ifootballmanager.ai.search;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Set;

/**
 *
 * @author lstephen
 */
public abstract class ActionsFunction<S extends State> implements aima.core.search.framework.ActionsFunction {

    @Override
    public Set<aima.core.agent.Action> actions(Object state) {
        Set<aima.core.agent.Action> actions = Sets.newHashSet();

        Iterable<? extends Action<S>> singles = getActions((S) state);

        for (Action<S> a : singles) {
            if (a.isValid((S) state)) {
                actions.add(a);
            }
        }
        return ImmutableSet.copyOf(actions);
    }

    public abstract Iterable<? extends Action<S>> getActions(S state);

}
