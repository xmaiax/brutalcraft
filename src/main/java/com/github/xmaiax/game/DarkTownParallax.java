package com.github.xmaiax.game;

import java.util.Arrays;
import java.util.List;

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

  public void load() { this.allTextures.forEach(t2d -> t2d.load()); }
  public void free() { this.allTextures.forEach(t2d -> t2d.free()); }

  private static final double SPEED = 0.025d;

  private double position = 0;
  public int getPosition() { return Double.valueOf(this.position).intValue(); }

  public void update(long msSinceLastUpdate, boolean moving, boolean backwards) {
    if(moving) this.position += backwards ? -SPEED : SPEED; //FIXME Tá bugado quando desliga o V-Sync, relativizar com tempo.
    //...
  }

  public void render(Renderer2D renderer2D) {
    //...
    this.staticSky.bind();
    for(int i = -32; i < 32; i++)
      renderer2D.render2DQuad(new Position(-this.getPosition() + (this.staticSky.getDimension().getWidth() * i), 0), this.staticSky.getDimension(), false, 1.0d);
  }

}
