package com.github.xmaiax.config;

import org.springframework.beans.factory.annotation.Value;

@org.springframework.stereotype.Component public class VideoSettings {

  public static final String OPENGL_VERSION = "3.0";
  public static String STATIC_GAME_NAME = "null";

  private String title;
  public String getTitle() { return new String(this.title); }

  private int width;
  public int getWidth() { return this.width; }

  private int height;
  public int getHeight() { return this.height; }

  private boolean fullscreen;
  public boolean getFullscreen() { return this.fullscreen; }

  private boolean vsync;
  public boolean getVsync() { return this.vsync; }

  private String clearColor;
  public String getClearColor() { return this.clearColor; }

  @org.springframework.beans.factory.annotation.Autowired public VideoSettings(
      @Value("${settings.video.title}") String title,
      @Value("${settings.video.width}") int width,
      @Value("${settings.video.height}") int height,
      @Value("${settings.video.fullscreen}") boolean fullscreen,
      @Value("${settings.video.vsync}") boolean vsync,
      @Value("${settings.video.clear-color}") String clearColor) {
    this.title = title;
    this.width = width;
    this.height = height;
    this.fullscreen = fullscreen;
    this.vsync = vsync;
    this.clearColor = clearColor;
  }

}
