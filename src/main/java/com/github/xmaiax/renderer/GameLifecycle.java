package com.github.xmaiax.renderer;

public interface GameLifecycle {
  void load();
  boolean loop(long msSinceLastUpdate, java.util.List<com.github.xmaiax.input.InputedAction> inputKeys);
  void render();
  void shutdown();
}
