package com.ljs.ifootballmanager.ai.report;

import com.ljs.ifootballmanager.ai.Context;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.value.NowValue;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

public class SkillByAge implements Report {

  private Context ctx;

  private SkillByAge(Context ctx) {
    this.ctx = ctx;
  }

  private int getMinAge() {
    return Player.byAge().min(ctx.getSquad().players()).getAge();
  }

  private int getMaxAge() {
    return Player.byAge().max(ctx.getSquad().players()).getAge();
  }

  private OptionalDouble getAvgNowValue(int age) {
    return ctx.getSquad().players().stream()
      .filter(p -> p.getAge() == age)
      .mapToDouble(p -> NowValue.bestVsReplacement(ctx, p).getScore())
      .average();
  }

  private OptionalDouble getThreeYearAverage(int age) {
    if (age == getMinAge() || age == getMaxAge()) {
      return OptionalDouble.empty();
    }

    return ctx.getSquad().players().stream()
      .filter(p -> p.getAge() >= age - 1 && p.getAge() <= age + 1)
      .mapToDouble(p -> NowValue.bestVsReplacement(ctx, p).getScore())
      .average();
  }

  public Double getAverageForComparison(int age) {
    if (age < getMinAge()) {
      return getAverageForComparison(getMinAge());
    }
    if (age > getMaxAge()) {
      return getAverageForComparison(getMaxAge());
    }

    return getThreeYearAverage(age).orElseGet(() -> getAvgNowValue(age).getAsDouble());
  }

  public void print(PrintWriter w) {
    for (int age = getMinAge(); age <= getMaxAge(); age++) {
      w.format("%3d", age);
    }

    w.println();

    for (int age = getMinAge(); age <= getMaxAge(); age++) {
      OptionalDouble avg = getAvgNowValue(age);
      w.format("%3s", avg.isPresent() ? String.format("%3d", Math.round(avg.getAsDouble())) : "");
    }

    w.println();

    for (int age = getMinAge(); age <= getMaxAge(); age++) {
      OptionalDouble avg = getThreeYearAverage(age);
      w.format("%3s", avg.isPresent() ? String.format("%3d", Math.round(avg.getAsDouble())) : "");
    }

    w.println();
  }

  public static SkillByAge create(Context ctx) {
    return new SkillByAge(ctx);
  }

}

