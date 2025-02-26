package com.github.xmaiax.input;

public enum InputEvent {

  RELEASE("Release"), PRESS("Press"), REPEAT("Repeat"), INVALID("?");

  private final String alias;
  @Override public String toString() { return this.alias; }
  private InputEvent(final String alias) { this.alias = alias; }

  public static InputEvent fromCode(final int code) {
    switch(code) {
      case org.lwjgl.glfw.GLFW.GLFW_RELEASE: return RELEASE;
      case org.lwjgl.glfw.GLFW.GLFW_PRESS:   return PRESS;
      case org.lwjgl.glfw.GLFW.GLFW_REPEAT:  return REPEAT;
      default:
        return INVALID;
    }
  }

}
