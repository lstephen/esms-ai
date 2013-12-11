package com.ljs.ifootballmanager.ai.search;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import java.util.Set;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

/**
 *
 * @author lstephen
 */
public class PairedAction<S extends State> extends Action<S> {

    private final Action<S> first;
    private final Action<S> second;

    private PairedAction(Action<S> first, Action<S> second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public S apply(S state) {
        return second.apply(first.apply(state));
    }

    private static <S extends State> PairedAction<S> create(Action<S> first, Action<S> second) {
        return new PairedAction<S>(first, second);
    }

    private static <S extends State> PairedAction<S> create(Iterable<Action<S>> actions) {
        return create(Iterables.get(actions, 0), Iterables.get(actions, 1));
    }

    public static <S extends State> ImmutableSet<PairedAction<S>> all(Iterable<? extends Action<S>> actions) {
        ICombinatoricsVector<Action<S>> initial = Factory.createVector(ImmutableSet.copyOf(actions));

        Generator<Action<S>> generator = Factory.createSimpleCombinationGenerator(initial, 2);

        Set<PairedAction<S>> result = Sets.newHashSet();

        for (ICombinatoricsVector<Action<S>> combination : generator) {
            result.add(create(combination.getVector()));
        }

        return ImmutableSet.copyOf(result);
    }

    public static <S extends State> ImmutableSet<PairedAction<S>> merged(
        Iterable<? extends Action<S>> firsts,
        Iterable<? extends Action<S>> seconds) {

        Set<PairedAction<S>> actions = Sets.newHashSet();

        for (Action<S> first : firsts) {
            for (Action<S> second : seconds) {
                actions.add(PairedAction.create(first, second));
            }
        }

        return ImmutableSet.copyOf(actions);
    }


}
