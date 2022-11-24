package com.github.xmaiax.renderer;

public class Dimension {

  public Dimension() {
    this.width = -1;
    this.height = -1;
  }

  public Dimension(int width, int height) {
    this.width = width;
    this.height = height;
  }

  private int width;
  public int getWidth() { return this.width; }
  public Dimension setWidth(int width) {
    this.width = width; return this; }

  private int height;
  public int getHeight() { return this.height; }
  public Dimension setHeight(int height) {
    this.height = height; return this; }

  public boolean isValid() {
    return this.width > 0 && this.height > 0;
  }

  public Dimension scaleUp(Double scale) {
    return scale == null ? this : new Dimension()
      .setWidth((int)(this.width * scale)).setHeight((int)(this.height * scale));
  }

}
