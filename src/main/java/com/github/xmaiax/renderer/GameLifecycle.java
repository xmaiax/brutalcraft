package com.github.xmaiax.renderer;

public interface GameLifecycle {
  void load();
  boolean loop(final long msSinceLastUpdate, final java.util.List<com.github.xmaiax.input.InputedAction> inputKeys);
  void render();
  void shutdown();
}
