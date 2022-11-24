package com.github.xmaiax.config;

import org.springframework.beans.factory.annotation.Value;

@org.springframework.stereotype.Component public class AppInfo {

  private String releaseVersion;
  public String getReleaseVersion() { return this.releaseVersion; }

  private String springBootVersion;
  public String getSpringBootVersion() { return this.springBootVersion; }

  private String lwjglVersion;
  public String getLwjglVersion() { return this.lwjglVersion; }

  private String welcomeMessage;
  public String getWelcomeMessage() { return this.welcomeMessage; }

  private boolean hideMouseCursor;
  public boolean getHideMouseCursor() { return this.hideMouseCursor; }

  private String windowIconLocation;
  public String getWindowIconLocation() { return this.windowIconLocation; }

  @org.springframework.beans.factory.annotation.Autowired public AppInfo(
      @Value("${app.info.release-version}") String releaseVersion,
      @Value("${app.info.spring-boot-version}") String springBootVersion,
      @Value("${app.info.lwjgl-version}") String lwjglVersion,
      @Value("${app.info.welcome-message}") String welcomeMessage,
      @Value("${app.info.hide-mouse-cursor}") boolean hideMouseCursor,
      @Value("${app.info.window-icon-location}") String windowIconLocation) {
    this.releaseVersion = releaseVersion;
    this.springBootVersion = springBootVersion;
    this.lwjglVersion = lwjglVersion;
    this.welcomeMessage = welcomeMessage;
    this.hideMouseCursor = hideMouseCursor;
    this.windowIconLocation = windowIconLocation;
  }

}
