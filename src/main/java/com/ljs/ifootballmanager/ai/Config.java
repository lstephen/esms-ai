package com.ljs.ifootballmanager.ai;

import java.io.File;

public class Config {

  private static Config INSTANCE = new Config();

  private Config() {}

  public File getDataDirectory() {
    return new File(System.getenv("ESMSAI_DATA"));
  }

  public static Config get() {
    return INSTANCE;
  }
}
