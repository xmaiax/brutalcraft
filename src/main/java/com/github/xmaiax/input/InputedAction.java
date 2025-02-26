package com.github.xmaiax.input;

import java.math.BigInteger;

public class InputedAction {

  private final InputedKey key;
  public InputedKey getKey() { return this.key; }

  private final InputEvent event;
  public InputEvent getEvent() { return this.event; }

  public InputedAction(final InputedKey key, final InputEvent event) {
    this.key = key;
    this.event = event;
  }

  public InputedAction( final int inputKey, final int keyEvent) {
    this.key = InputedKey.fromCode(inputKey);
    this.event = InputEvent.fromCode(keyEvent);
  }

  @Override public String toString() { return String.format("%s -> %s", this.event, this.key); }

  @Override public int hashCode() {
    return (this.getKey() != null ? this.getKey().hashCode() : BigInteger.ZERO.intValue()) +
           (this.getEvent() != null ? this.getEvent().hashCode() : BigInteger.ZERO.intValue());
  }

  @Override public boolean equals(final Object _obj) {
    if(!(_obj instanceof InputedAction)) return Boolean.FALSE;
    final InputedAction obj = (InputedAction) _obj;
    return ((obj.getEvent() == null && this.getEvent() == null) ||
           (this.getEvent() != null && this.getEvent().equals(obj.getEvent()))) &&
           ((obj.getKey() == null && this.getKey() == null) || 
           (this.getKey() != null && this.getKey().equals(obj.getKey())));
  }

}
