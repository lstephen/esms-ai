package com.ljs.ifootballmanager.ai;

import com.ljs.ifootballmanager.ai.league.League;
import com.ljs.ifootballmanager.ai.player.Squad;
import com.ljs.ifootballmanager.ai.selection.FirstXI;
import com.ljs.ifootballmanager.ai.value.ReplacementLevel;
import java.util.Optional;

public final class Context {

  private Optional<League> league;

  private Optional<Squad> squad;

  private Optional<FirstXI> firstXI;

  private Optional<ReplacementLevel> replacementLevel;

  public League getLeague() {
    return league.get();
  }

  public void setLeague(League league) {
    this.league = Optional.of(league);
  }

  public Squad getSquad() {
    return squad.get();
  }

  public void setSquad(Squad squad) {
    this.squad = Optional.of(squad);
  }

  public FirstXI getFirstXI() {
    return firstXI.get();
  }

  public void setFirstXI(FirstXI firstXI) {
    this.firstXI = Optional.of(firstXI);
  }

  public ReplacementLevel getReplacementLevel() {
    return replacementLevel.get();
  }

  public void setReplacementLevel(ReplacementLevel rl) {
    this.replacementLevel = Optional.of(rl);
  }
}
