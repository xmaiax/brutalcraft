package com.github.xmaiax;

import java.awt.Color;

import com.github.xmaiax.renderer.Position;
import com.github.xmaiax.renderer.RenderableObject;
import com.github.xmaiax.renderer.Renderer2D;
import com.github.xmaiax.renderer.TrueTypeFont;
import com.github.xmaiax.renderer.TrueTypeFont.BakedText;

public class FPSCounter extends RenderableObject {

  private float size;
  private Color color;
  private int topRightMargin;

  public FPSCounter(float size, Color color, int topRightMargin) {
    this.size = size;
    this.color = color;
    this.topRightMargin = topRightMargin;
  }

  private TrueTypeFont font;

  private String currentText = "?";
  private int frameCounter = 0;
  private long lastTime = System.currentTimeMillis();
  private long msCounter = 0L;

  @Override public void load() { throw new UnsupportedOperationException(
    "Use the `public void load(TrueTypeFont)` when loading the FPSCounter class!"); }

  public void load(TrueTypeFont font) { this.font = font; }

  private BakedText currentBakedText = null;

  @Override public void free() { this.currentBakedText.free(); }

  public void render(Renderer2D renderer2D) {
    this.msCounter += System.currentTimeMillis() - this.lastTime;
    this.lastTime = System.currentTimeMillis();
    this.frameCounter++;
    if(this.msCounter >= 1000L) {
      this.currentText = String.format("FPS: %d", this.frameCounter);
      this.frameCounter = 0;
      this.msCounter = 0L;
    }
    this.currentBakedText = this.font.bakeText(this.currentText, this.size, this.color, this.currentBakedText);
    this.currentBakedText.bind();
    renderer2D.render2DQuad(new Position(renderer2D.getVideoSettings().getWidth() -
      this.currentBakedText.getDimension().getWidth() - this.topRightMargin, this.topRightMargin),
        this.currentBakedText.getDimension(), false, 1.0d);
  }

}
