package com.ljs.ifootballmanager.ai;

import java.io.File;

import com.google.common.base.StandardSystemProperty;

public class Config {

  private static Config INSTANCE = new Config();

  private Config() { }

  public File getDataDirectory() {
    String userHome = StandardSystemProperty.USER_HOME.value();

    return new File(userHome + "/Google Drive/esms");
  }

  public static Config get() {
    return INSTANCE;
  }
}
