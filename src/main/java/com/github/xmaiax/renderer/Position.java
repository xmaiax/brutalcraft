package com.github.xmaiax.renderer;

import java.math.BigInteger;

public class Position {

  public Position() {
    this.x = BigInteger.ZERO.intValue();
    this.y = BigInteger.ZERO.intValue();
  }

  public Position(final int x, final int y) {
    this.x = x;
    this.y = y;
  }

  private int x;
  public int getX() { return this.x; }
  public Position setX(int x) {
    this.x = Integer.valueOf(x); return this; }

  private int y;
  public int getY() { return this.y; }
  public Position setY(int y) {
    this.y = Integer.valueOf(y); return this; }

}
