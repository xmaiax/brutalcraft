package com.github.xmaiax.input;

public enum InputedControllerKey {

  CROSS(0, InputedKey._GAMEPAD_CROSS),
  CIRCLE(1, InputedKey._GAMEPAD_CIRCLE),
  SQUARE(2, InputedKey._GAMEPAD_SQUARE),
  TRIANGLE(3, InputedKey._GAMEPAD_TRIANGLE),
  L1(4, InputedKey._GAMEPAD_L1),
  R1(5, InputedKey._GAMEPAD_R2),
  SELECT(6, InputedKey._GAMEPAD_SELECT),
  START(7, InputedKey._GAMEPAD_START),
  L3(8, InputedKey._GAMEPAD_L3),
  R3(9, InputedKey._GAMEPAD_R3),
  UP(10, InputedKey._GAMEPAD_DPAD_UP),
  RIGHT(11, InputedKey._GAMEPAD_DPAD_RIGHT),
  DOWN(12, InputedKey._GAMEPAD_DPAD_DOWN),
  LEFT(13, InputedKey._GAMEPAD_DPAD_LEFT),

  ;

  private int position;
  public int getPosition() { return this.position; }

  private InputedKey key;
  public InputedKey getKey() { return this.key; }

  private InputedControllerKey(int position, InputedKey key) {
    this.position = position;
    this.key = key;
  }

  public InputedAction getInputedAction(InputEvent event) {
    return new InputedAction(this.key, event);
  }

}
