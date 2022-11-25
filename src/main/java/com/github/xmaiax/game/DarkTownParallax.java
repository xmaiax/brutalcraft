package com.github.xmaiax.game;

import java.util.Arrays;
import java.util.List;

import com.github.xmaiax.config.VideoSettings;
import com.github.xmaiax.renderer.Dimension;
import com.github.xmaiax.renderer.Position;
import com.github.xmaiax.renderer.Renderer2D;
import com.github.xmaiax.renderer.Texture2D;

public class DarkTownParallax {

  private static final String TEXTURES_PREFIX = "textures/scenarios/parallax/darktown/";

  private Texture2D staticSky = new Texture2D(TEXTURES_PREFIX + "sky1.png");
  private Texture2D staticClouds = new Texture2D(TEXTURES_PREFIX + "clouds.png");
  private Texture2D dynamicBrightMountain = new Texture2D(TEXTURES_PREFIX + "dynamicBrightMountain.png");
  private Texture2D dynamicMountain = new Texture2D(TEXTURES_PREFIX + "dynamicMountain.png");
  private Texture2D dynamicForest = new Texture2D(TEXTURES_PREFIX + "dynamicForest.png");
  private Texture2D dynamicDetails1 = new Texture2D(TEXTURES_PREFIX + "dynamicDetails1.png");
  private Texture2D dynamicDetails2 = new Texture2D(TEXTURES_PREFIX + "dynamicDetails2.png");
  private Texture2D plainTile = new Texture2D(TEXTURES_PREFIX + "plainTile.png");
  private Texture2D lowerLeftTile = new Texture2D(TEXTURES_PREFIX + "lowerLeftTile.png");
  private Texture2D lowerRightTile = new Texture2D(TEXTURES_PREFIX + "lowerRightTile.png");
  private Texture2D pillar = new Texture2D(TEXTURES_PREFIX + "pillar.png");

  private final List<Texture2D> allTextures = Arrays.asList(new Texture2D[] {
    this.staticSky, this.staticClouds, this.dynamicBrightMountain, this.dynamicMountain,
    this.dynamicForest, this.dynamicDetails1, this.dynamicDetails2, this.plainTile,
    this.lowerLeftTile, this.lowerRightTile, this.pillar,
  });

  private double scale = 1.0d;
  private int staticSkyQuantity = 1;
  private int dynamicMountainQuantity = 1;
  private int dynamicForestQuantity = 1;
  private int screenCenterX = 0;

  public void load(VideoSettings videoSettings) {
    this.allTextures.forEach(t2d -> t2d.load());
    this.scale = Double.valueOf(videoSettings.getHeight()) / Double.valueOf(this.staticSky.getDimension().getHeight()) / 2.0d;
    this.staticSkyQuantity = ((int) Math.ceil(Double.valueOf(videoSettings.getWidth()) / Double.valueOf(this.staticSky.getDimension(this.scale).getWidth()))) / 2 + 1;
    this.dynamicMountainQuantity = ((int) Math.ceil(Double.valueOf(videoSettings.getWidth()) / Double.valueOf(this.dynamicMountain.getDimension(this.scale).getWidth()))) / 2 + 1;
    this.dynamicForestQuantity = ((int) Math.ceil(Double.valueOf(videoSettings.getWidth()) / Double.valueOf(this.dynamicForest.getDimension(this.scale).getWidth()))) / 2 + 1;
    this.screenCenterX = videoSettings.getWidth() / 2;
  }

  public void free() { this.allTextures.forEach(t2d -> t2d.free()); }

  private static final double SPEED = 0.025d;

  private double skyPosition = 0;
  private double mountainsPosition = 0;
  private double forestPosition = 0;

  public int getPosition() { return Double.valueOf(this.skyPosition).intValue(); }

  public void update(long msSinceLastUpdate, boolean moving, boolean backwards) {
    if(moving) {
      this.skyPosition += backwards ? -SPEED : SPEED;
      this.mountainsPosition += 2.0d * (backwards ? -SPEED : SPEED);
      this.forestPosition += 4.0d * (backwards ? -SPEED : SPEED);
    }
  }

  public void render(Renderer2D renderer2D) {
    this.staticSky.bind();
    Dimension scaledDimension = this.staticSky.getDimension(this.scale);
    double fix = this.skyPosition / Double.valueOf(scaledDimension.getWidth());
    int quantityFix = this.staticSkyQuantity + (
      (int) (fix < 0.0d ? -Math.floor(fix) : Math.ceil(fix)));
    for(int i = -quantityFix; i < quantityFix; i++)
      renderer2D.render2DQuad(new Position(
        this.screenCenterX - ((int) this.skyPosition) + (scaledDimension.getWidth() * i), 0),
          scaledDimension, false, 1.0d);

    this.dynamicMountain.bind();
    scaledDimension = this.dynamicMountain.getDimension(this.scale);
    fix = this.mountainsPosition / Double.valueOf(scaledDimension.getWidth());
    int y = renderer2D.getVideoSettings().getHeight() - (scaledDimension.getHeight() * 3 / 2);
    quantityFix = this.dynamicMountainQuantity + ((int) (fix < 0.0d ? -Math.floor(fix) : Math.ceil(fix)));
    for(int i = -quantityFix; i < quantityFix; i++)
      renderer2D.render2DQuad(new Position(this.screenCenterX - (
        (int) this.mountainsPosition) + (scaledDimension.getWidth() * i), y), scaledDimension, false, 1.0d);

    this.dynamicForest.bind();
    scaledDimension = this.dynamicForest.getDimension(this.scale);
    fix = this.forestPosition / Double.valueOf(scaledDimension.getWidth());
    y = renderer2D.getVideoSettings().getHeight() - (scaledDimension.getHeight() * 3 / 2);
    quantityFix = this.dynamicForestQuantity + ((int) (fix < 0.0d ? -Math.floor(fix) : Math.ceil(fix)));
    for(int i = -quantityFix; i < quantityFix; i++)
      renderer2D.render2DQuad(new Position(this.screenCenterX - (
        (int) this.forestPosition) + (scaledDimension.getWidth() * i), y), scaledDimension, false, 1.0d);

  }

}
