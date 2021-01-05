package com.ljs.ifootballmanager.ai.value;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.ljs.ifootballmanager.ai.Context;
import com.ljs.ifootballmanager.ai.Role;
import com.ljs.ifootballmanager.ai.Tactic;
import com.ljs.ifootballmanager.ai.formation.Formation;
import com.ljs.ifootballmanager.ai.player.Player;
import com.ljs.ifootballmanager.ai.player.Squad;
import com.ljs.ifootballmanager.ai.rating.Rating;
import com.ljs.ifootballmanager.ai.selection.FirstXI;
import java.util.List;

/** @author lstephen */
public class ReplacementLevel {

  private Squad squad;

  private FirstXI firstXI;

  private ReplacementLevel(Squad squad, FirstXI firstXI) {
    this.squad = squad;
    this.firstXI = firstXI;
  }

  public Double getValueVsReplacement(Player p) {
    List<Double> values = Lists.newArrayList();

    for (Role r : Role.values()) {
      values.add(getValueVsReplecement(p, r));
    }

    return Ordering.natural().max(values);
  }

  private Double getValueVsReplecement(Player p, Role r) {
    Tactic t = firstXI.best().getTactic();

    return p.evaluate(r, t).getRating() - getReplacementLevel(r, t);
  }

  public Double getReplacementLevel(Role role, Tactic t) {
    return Player.byRating(role, t).max(getReplacements(t)).getRating(role, t);
  }

  public Double getReplacementLevel(Rating skill, Tactic t) {
    return Player.bySkill(skill).max(getReplacements(t)).getSkill(skill);
  }

  private ImmutableSet getReplacements(Tactic t) {
    Formation f =
        firstXI
            .getFormation(t)
            .orElseThrow(() -> new IllegalStateException("No formation for tactic " + t));

    return ImmutableSet.copyOf(Sets.difference(squad.players(), ImmutableSet.copyOf(f.players())));
  }

  public static ReplacementLevel create(Squad squad, FirstXI firstXI) {
    return new ReplacementLevel(squad, firstXI);
  }

  public static ReplacementLevel create(Context ctx) {
    return create(ctx.getSquad(), ctx.getFirstXI());
  }
}
