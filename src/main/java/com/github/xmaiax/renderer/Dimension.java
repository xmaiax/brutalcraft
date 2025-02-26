package com.github.xmaiax.renderer;

import java.math.BigInteger;

public class Dimension {

  public Dimension() {
    this.width = -BigInteger.ONE.intValue();
    this.height = -BigInteger.ONE.intValue();
  }

  public Dimension(final int width, final int height) {
    this.width = Integer.valueOf(width);
    this.height = Integer.valueOf(height);
  }

  private int width;
  public int getWidth() { return this.width; }
  public Dimension setWidth(final int width) {
    this.width = Integer.valueOf(width); return this; }

  private int height;
  public int getHeight() { return this.height; }
  public Dimension setHeight(final int height) {
    this.height = Integer.valueOf(height); return this; }

  public boolean isValid() {
    return this.width > BigInteger.ZERO.intValue() && this.height > BigInteger.ZERO.intValue();
  }

  public Dimension scaleUp(Double scale) {
    return scale == null ? this : new Dimension()
      .setWidth((int)(this.getWidth() * scale))
      .setHeight((int)(this.getHeight() * scale));
  }

}
