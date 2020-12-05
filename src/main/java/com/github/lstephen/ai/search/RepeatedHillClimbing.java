package com.github.lstephen.ai.search;

import com.google.common.base.Preconditions;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** */
public final class RepeatedHillClimbing<S> {

  private static final Integer REPEATS = 5;

  private final Supplier<S> initialFactory;

  private final HillClimbing<S> hillClimbing;

  public RepeatedHillClimbing(Supplier<S> initialFactory, HillClimbing<S> hillClimbing) {
    Preconditions.checkNotNull(initialFactory);
    Preconditions.checkNotNull(hillClimbing);

    this.initialFactory = initialFactory;
    this.hillClimbing = hillClimbing;
  }

  public S search() {
    List<S> initial = Stream.generate(initialFactory).limit(REPEATS).collect(Collectors.toList());

    return initial.parallelStream()
        .map(hillClimbing::search)
        .max(hillClimbing.getHeuristic())
        .orElseThrow(() -> new IllegalStateException("Could not find final result"));
  }
}
