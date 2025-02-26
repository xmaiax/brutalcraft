package com.github.xmaiax.game;

import java.util.List;

import com.github.xmaiax.input.InputEvent;
import com.github.xmaiax.input.InputedAction;
import com.github.xmaiax.input.InputedKey;
import com.github.xmaiax.renderer.Renderer2D;
import com.github.xmaiax.renderer.TrueTypeFont;

@org.springframework.stereotype.Component
public class GameLogic implements com.github.xmaiax.renderer.GameLifecycle {

  private static final InputedAction ESCAPE_BUTTON_WAS_PRESSED = new InputedAction(InputedKey._ESCAPE, InputEvent.RELEASE);

  private Renderer2D renderer2D;

  @org.springframework.beans.factory.annotation.Autowired
  public GameLogic(final Renderer2D renderer2D) { this.renderer2D = renderer2D; }

  private TrueTypeFont genericTTF = new TrueTypeFont("fonts/GorgeousPixel.ttf");
  private FPSCounter fpsCounter = new FPSCounter(42.0f, java.awt.Color.WHITE, 8);

  @Override public void load() {
    this.genericTTF.load();
    this.fpsCounter.load(this.genericTTF);
  }

  @Override public boolean loop(final long msSinceLastUpdate, final List<InputedAction> inputKeys) {
    return !inputKeys.contains(ESCAPE_BUTTON_WAS_PRESSED);
  }

  @Override public void render() {
    this.fpsCounter.render(this.renderer2D);
  }

  @Override public void shutdown() {
    this.genericTTF.free();
    this.fpsCounter.free();
  }

}
