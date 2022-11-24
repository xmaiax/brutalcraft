package com.github.xmaiax.renderer;

public class Position {

  public Position() {
    this.x = 0;
    this.y = 0;
  }

  public Position(int x, int y) {
    this.x = x;
    this.y = y;
  }

  private int x;
  public int getX() { return this.x; }
  public Position setX(int x) {
    this.x = x; return this; }

  private int y;
  public int getY() { return this.y; }
  public Position setY(int y) {
    this.y = y; return this; }

}
