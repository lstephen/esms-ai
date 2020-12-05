package com.github.lstephen.ai.search;

import com.github.lstephen.ai.search.action.ActionGenerator;
import com.google.common.base.Preconditions;
import com.google.common.collect.Ordering;
import java.util.Optional;

/**
 * Hillclimbing search to optimize a solution for a given {@link Heuristic}.
 *
 * <p>The following are provided to enable the search:
 *
 * <ul>
 *   <li>{@link Heuristic} - To determine which solutions are better than another.
 *   <li>{@link ActionGenerator} - Provides the possible {@link
 *       com.github.lstephen.ai.search.action.Action}s that transform the current solution into
 *       another solution.
 *   <li>{@link Validator} - Determines if a given solution is valid. This allows the {@link
 *       ActionGenerator} to provide {@link com.github.lstephen.ai.search.action.Action}s that may
 *       lead to invalid solutions. Sometimes this is useful for performance reasons.
 * </ul>
 *
 * <p>The {@link Validator} is optional and if not provided it's assumed that all solutions are
 * valid.
 *
 * @param <S> type of solution to optimize
 */
public final class HillClimbing<S> {

  private final Validator<S> validator;

  private final Heuristic<S> heuristic;

  private final ActionGenerator<S> actionGenerator;

  private HillClimbing(Builder<S> builder) {
    Preconditions.checkNotNull(builder.validator);
    Preconditions.checkNotNull(builder.heuristic);
    Preconditions.checkNotNull(builder.actionGenerator);

    this.validator = builder.validator;
    this.heuristic = builder.heuristic;
    this.actionGenerator = builder.actionGenerator;
  }

  /**
   * The {@link Heuristic} being used for the search.
   *
   * @return heuristic used for search.
   */
  public Heuristic<S> getHeuristic() {
    return heuristic;
  }

  /**
   * Search the solution space starting from the given starting point.
   *
   * @param initial the starting point
   * @return the optimized solution
   */
  public S search(S initial) {
    S current = initial;

    Optional<S> next = next(current);

    while (next.isPresent()) {
      current = next.get();

      next = next(current);
    }

    return current;
  }

  private Optional<S> next(S current) {
    return actionGenerator
        .apply(current)
        .map((a) -> a.apply(current))
        .filter(validator)
        .filter((n) -> heuristic.compare(current, n) < 0)
        .findFirst();
  }

  /**
   * Get a {@link Builder} that can be used to create a {@link HillClimbing} instance.
   *
   * @param <S> type of solution to optimize
   * @return a builder for constructing an instance
   */
  public static <S> Builder<S> builder() {
    return Builder.create();
  }

  private static <S> HillClimbing<S> build(Builder<S> builder) {
    return new HillClimbing<>(builder);
  }

  /**
   * Builder for creating {@link HillClimbing} instances.
   *
   * <p>Example:
   *
   * <blockquote>
   *
   * <pre>{@code
   * HillClimbing<MyClass> search = Hillclimbing
   *   .builder()
   *   .validator((s) -> s.isValid())
   *   .heuristic((s) -> s.getScore())
   *   .actionGenerator((s) -> s.getTransformations())
   *   .build();
   * }</pre>
   *
   * </blockquote>
   *
   * @param <S> solution type
   */
  public static final class Builder<S> {

    private Validator<S> validator = (s) -> true;

    private Heuristic<S> heuristic;

    private ActionGenerator<S> actionGenerator;

    private Builder() {}

    public Builder<S> validator(Validator<S> validator) {
      this.validator = validator;
      return this;
    }

    public Builder<S> heuristic(Heuristic<S> heurisitc) {
      this.heuristic = heurisitc;
      return this;
    }

    public Builder<S> heuristic(final Ordering<? super S> ordering) {
      return heuristic(ordering::compare);
    }

    public Builder<S> actionGenerator(ActionGenerator<S> actionGenerator) {
      this.actionGenerator = actionGenerator;
      return this;
    }

    /**
     * Build the {@link HillClimbing} instance.
     *
     * @return the constructed instance
     */
    public HillClimbing<S> build() {
      return HillClimbing.build(this);
    }

    private static <S> Builder<S> create() {
      return new Builder<S>();
    }
  }
}
