/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ljs.ifootballmanager.ai;

/** @author lstephen */
public enum Role {
  GK,
  DF,
  DM,
  MF,
  AM,
  FW;

  public boolean isOneOf(Role... rs) {
    for (Role r : rs) {
      if (r == this) {
        return true;
      }
    }
    return false;
  }
}
