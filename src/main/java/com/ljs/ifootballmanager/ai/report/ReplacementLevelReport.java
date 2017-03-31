package com.ljs.ifootballmanager.ai.report;

import com.ljs.ifootballmanager.ai.Context;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.formation.Formation;

import java.io.PrintWriter;

public class ReplacementLevelReport implements Report {

  private final Context ctx;

  private ReplacementLevelReport(Context ctx) {
    this.ctx = ctx;
  }

  public void print(PrintWriter w) {
    ctx.getFirstXI().getFormations().forEach(f -> {
      Tactic t = f.getTactic();

      w.format("%-15s ", t);

      for (Role r : Role.values()) {
        w.format("%3d ", Math.round(ctx.getReplacementLevel().getReplacementLevel(r, t)));
      }
      w.println();
    });
  }

  public static ReplacementLevelReport create(Context ctx) {
    return new ReplacementLevelReport(ctx);
  }

}
