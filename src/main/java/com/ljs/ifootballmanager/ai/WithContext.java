package com.ljs.ifootballmanager.ai;

import com.ljs.ifootballmanager.ai.league.League;
import com.ljs.ifootballmanager.ai.player.Squad;
import com.ljs.ifootballmanager.ai.selection.FirstXI;

public interface WithContext {

  Context getContext();

  default League getLeague() { return getContext().getLeague(); }

  default FirstXI getFirstXI() { return getContext().getFirstXI(); }

  default Squad getSquad() { return getContext().getSquad(); }

}
