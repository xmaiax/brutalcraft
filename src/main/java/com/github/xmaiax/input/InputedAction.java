package com.github.xmaiax.input;

public class InputedAction {

  private InputedKey key;
  public InputedKey getKey() { return this.key; }

  private InputEvent event;
  public InputEvent getEvent() { return this.event; }

  public InputedAction(InputedKey key, InputEvent event) {
    this.key = key;
    this.event = event;
  }

  public InputedAction(int inputKey, int keyEvent) {
    this.key = InputedKey.fromCode(inputKey);
    this.event = InputEvent.fromCode(keyEvent);
  }

  @Override public String toString() { return String.format("%s -> %s", this.event, this.key); }

  @Override public int hashCode() {
    return (this.getKey() != null ? this.getKey().hashCode() : 0) +
           (this.getEvent() != null ? this.getEvent().hashCode() : 0);
  }

  @Override public boolean equals(Object _obj) {
    if(!(_obj instanceof InputedAction)) return false;
    final InputedAction obj = (InputedAction) _obj;
    return ((obj.getEvent() == null && this.getEvent() == null) || (this.getEvent() != null && this.getEvent().equals(obj.getEvent()))) &&
           ((obj.getKey() == null && this.getKey() == null) || (this.getKey() != null && this.getKey().equals(obj.getKey())));
  }

}
