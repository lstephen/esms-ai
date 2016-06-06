package com.ljs.ifootballmanager.ai;

import java.util.Optional;

import com.ljs.ifootballmanager.ai.league.League;
import com.ljs.ifootballmanager.ai.player.Squad;
import com.ljs.ifootballmanager.ai.selection.FirstXI;

public final class Context {

  private Optional<League> league;

  private Optional<Squad> squad;

  private Optional<FirstXI> firstXI;

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

}
