package com.github.xmaiax.game;

import java.awt.Color;
import java.math.BigInteger;

import com.github.xmaiax.renderer.Position;
import com.github.xmaiax.renderer.RenderableObject;
import com.github.xmaiax.renderer.Renderer2D;
import com.github.xmaiax.renderer.TrueTypeFont;
import com.github.xmaiax.renderer.TrueTypeFont.BakedText;

public class FPSCounter extends RenderableObject {

  private final float size;
  private final Color color;
  private final int topRightMargin;

  public FPSCounter(final float size, final Color color, final int topRightMargin) {
    this.size = size;
    this.color = color;
    this.topRightMargin = topRightMargin;
  }

  private TrueTypeFont font;
  private String currentText = "?";

  private int frameCounter;
  private long lastTime;
  private long msCounter;
  
  private void resetCounter() {
    this.frameCounter = BigInteger.ZERO.intValue();
    this.lastTime = System.currentTimeMillis();
    this.msCounter = BigInteger.ZERO.longValue();
  }

  @Override public void load() { throw new UnsupportedOperationException(
    "Use the `public void load(TrueTypeFont)` when loading the FPSCounter class!"); }

  public void load(final TrueTypeFont font) {
    this.font = font;
    this.resetCounter();
  }

  private BakedText currentBakedText = null;

  @Override public void free() { this.currentBakedText.free(); }

  public void render(final Renderer2D renderer2D) {
    this.msCounter += System.currentTimeMillis() - this.lastTime;
    this.lastTime = System.currentTimeMillis();
    this.frameCounter++;
    if(this.msCounter >= 1_000L) {
      this.currentText = String.format("FPS: %d", this.frameCounter);
      this.resetCounter();
    }
    this.currentBakedText = this.font.bakeText(
      this.currentText, this.size, this.color, this.currentBakedText);
    this.currentBakedText.bind();
    renderer2D.render2DQuad(new Position(renderer2D.getVideoSettings().getWidth() -
      this.currentBakedText.getDimension().getWidth() - this.topRightMargin, this.topRightMargin),
        this.currentBakedText.getDimension(), Boolean.FALSE, BigInteger.ONE.doubleValue());
  }

}
