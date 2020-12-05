package com.github.lstephen.ai.search.action;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

/** @author lstephen */
public final class SequencedAction<S> implements Action<S> {

  private final ImmutableList<Action<S>> actions;

  private SequencedAction(Iterable<Action<S>> actions) {
    this.actions = ImmutableList.copyOf(actions);
  }

  @Override
  public S apply(S initial) {
    AtomicReference<S> state = new AtomicReference<>(initial);

    actions.stream().forEach((a) -> state.getAndUpdate(a::apply));

    return state.get();
  }

  @SafeVarargs
  @SuppressWarnings("varargs")
  public static <S> SequencedAction<S> create(Action<S>... as) {
    return new SequencedAction<>(Arrays.asList(as));
  }

  public static <S> Stream<SequencedAction<S>> allPairs(Collection<? extends Action<S>> actions) {
    return merged(actions, actions);
  }

  public static <S> Stream<SequencedAction<S>> merged(
      Collection<? extends Action<S>> firsts, Collection<? extends Action<S>> seconds) {

    return merged(firsts.stream(), seconds);
  }

  public static <S> Stream<SequencedAction<S>> merged(
      Stream<? extends Action<S>> firsts, Collection<? extends Action<S>> seconds) {

    return firsts.flatMap((f) -> seconds.stream().map((s) -> SequencedAction.create(f, s)));
  }
}
