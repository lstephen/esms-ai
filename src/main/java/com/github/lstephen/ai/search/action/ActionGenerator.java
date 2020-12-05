package com.github.lstephen.ai.search.action;

import java.util.function.Function;
import java.util.stream.Stream;

/** @author lstephen */
public interface ActionGenerator<S> extends Function<S, Stream<? extends Action<S>>> {}
