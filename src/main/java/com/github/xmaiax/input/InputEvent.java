package com.github.xmaiax.input;

public enum InputEvent {

  RELEASE("Release"), PRESS("Press"), REPEAT("Repeat"), INVALID("?");

  private String alias;
  @Override public String toString() { return this.alias; }
  private InputEvent(String alias) { this.alias = alias; }

  public static InputEvent fromCode(int code) {
    switch(code) {
      case org.lwjgl.glfw.GLFW.GLFW_RELEASE: return RELEASE;
      case org.lwjgl.glfw.GLFW.GLFW_PRESS:   return PRESS;
      case org.lwjgl.glfw.GLFW.GLFW_REPEAT:  return REPEAT;
      default:           return INVALID;
    }
  }

}
