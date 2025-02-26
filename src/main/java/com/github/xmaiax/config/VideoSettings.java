package com.github.xmaiax.config;

import org.springframework.beans.factory.annotation.Value;

@org.springframework.stereotype.Component public class VideoSettings {

  public static final String OPENGL_VERSION = "3.0";
  public static String STATIC_GAME_NAME = "null";

  private final String title;
  public String getTitle() { return new String(this.title); }

  private final int width;
  public int getWidth() { return this.width; }

  private final int height;
  public int getHeight() { return this.height; }

  private final Boolean fullscreen;
  public Boolean getFullscreen() { return this.fullscreen; }

  private final Boolean vsync;
  public Boolean getVsync() { return this.vsync; }

  private final String clearColor;
  public String getClearColor() { return this.clearColor; }

  @org.springframework.beans.factory.annotation.Autowired public VideoSettings(
      @Value("${settings.video.title}") final String title,
      @Value("${settings.video.width}") final int width,
      @Value("${settings.video.height}") final int height,
      @Value("${settings.video.fullscreen}") final Boolean fullscreen,
      @Value("${settings.video.vsync}") final Boolean vsync,
      @Value("${settings.video.clear-color}") final String clearColor) {
    this.title = title;
    this.width = width;
    this.height = height;
    this.fullscreen = fullscreen;
    this.vsync = vsync;
    this.clearColor = clearColor;
  }

}
