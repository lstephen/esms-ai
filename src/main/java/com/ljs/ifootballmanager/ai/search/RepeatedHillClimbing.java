package com.ljs.ifootballmanager.ai.search;

import aima.core.search.framework.HeuristicFunction;
import aima.core.search.framework.Problem;
import aima.core.search.local.HillClimbingSearch;
import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

/**
 *
 * @author lstephen
 */
public class RepeatedHillClimbing<S extends State> {

    private static final Integer REPEATS = 5;

    private final Class<S> stateClass;

    private final HeuristicFunction heuristic;

    private final Callable<S> initialStateFactory;

    private final ActionsFunction<S> actionsFunction;

    public RepeatedHillClimbing(
        Class<S> stateClass,
        HeuristicFunction heuristic,
        Callable<S> initialStateFactory,
        ActionsFunction<S> actionsFunction) {

        this.stateClass = stateClass;
        this.heuristic = heuristic;
        this.initialStateFactory = initialStateFactory;
        this.actionsFunction = actionsFunction;
    }

    public S search() {
        System.out.print("Searching...");

        ListeningExecutorService executor = MoreExecutors.listeningDecorator(
            Executors.newFixedThreadPool(REPEATS));

        Set<ListenableFuture<S>> candidatesF = Sets.newHashSet();

        for (int i = 0; i < REPEATS; i++) {
            candidatesF.add(executor.submit(doSearch()));
        }

        try {
            S result = byHeuristic().min(Futures.allAsList(candidatesF).get());
            System.out.println(" Done.");
            return result;
        } catch (InterruptedException e) {
            throw Throwables.propagate(e);
        } catch (ExecutionException e) {
            throw Throwables.propagate(e);
        } finally {
            executor.shutdown();
        }
    }

    private Callable<S> doSearch() {
        return new Callable<S>() {
            public S call() {
                System.out.print(">-");
                HillClimbingSearch search = new HillClimbingSearch(heuristic);

                try {
                    search.search(problem());
                } catch (Exception e) {
                    throw Throwables.propagate(e);
                }

                System.out.print("-|");

                return stateClass.cast(search.getLastSearchState());
            }
        };
    }

    private Problem problem() {
        try {
            return new Problem(
                initialStateFactory.call(),
                actionsFunction,
                new ResultFunction(),
                GoalTest.INSTANCE);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private Ordering<S> byHeuristic() {
        return Ordering
            .natural()
            .onResultOf(new Function<S, Double>() {
                public Double apply(S state) {
                    return heuristic.h(state);
                }
            });
    }

    private class ResultFunction
        implements aima.core.search.framework.ResultFunction {

        @Override
        public Object result(Object state, aima.core.agent.Action a) {
            return getResult(stateClass.cast(state), Action.class.cast(a));
        }

        public S getResult(S state, Action<S> a) {
            return a.apply(state);
        }
    }

    private static final class GoalTest
        implements aima.core.search.framework.GoalTest {

        public static final GoalTest INSTANCE = new GoalTest();

        private GoalTest() { }

        @Override
        public boolean isGoalState(Object o) {
            return true;
        }
    }

}
