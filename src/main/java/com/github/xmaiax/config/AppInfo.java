package com.github.xmaiax.config;

import org.springframework.beans.factory.annotation.Value;

@org.springframework.stereotype.Component public class AppInfo {
  @org.springframework.beans.factory.annotation.Autowired public AppInfo(
      @Value("${app.info.release-version}") final String releaseVersion,
      @Value("${app.info.spring-boot-version}") final String springBootVersion,
      @Value("${app.info.lwjgl-version}") final String lwjglVersion,
      @Value("${app.info.welcome-message}") final String welcomeMessage,
      @Value("${app.info.hide-mouse-cursor}") final Boolean hideMouseCursor,
      @Value("${app.info.window-icon-location}") final String windowIconLocation) {
    this.releaseVersion = releaseVersion;
    this.springBootVersion = springBootVersion;
    this.lwjglVersion = lwjglVersion;
    this.welcomeMessage = welcomeMessage;
    this.hideMouseCursor = hideMouseCursor;
    this.windowIconLocation = windowIconLocation;
  }
  private final String releaseVersion;
  public String getReleaseVersion() { return this.releaseVersion; }
  private final String springBootVersion;
  public String getSpringBootVersion() { return this.springBootVersion; }
  private final String lwjglVersion;
  public String getLwjglVersion() { return this.lwjglVersion; }
  private final String welcomeMessage;
  public String getWelcomeMessage() { return this.welcomeMessage; }
  private final Boolean hideMouseCursor;
  public Boolean getHideMouseCursor() { return this.hideMouseCursor; }
  private final String windowIconLocation;
  public String getWindowIconLocation() { return this.windowIconLocation; }
}
