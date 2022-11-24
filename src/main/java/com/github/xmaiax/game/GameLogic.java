package com.github.xmaiax.game;

import java.awt.Color;
import java.util.List;
import com.github.xmaiax.input.InputEvent;
import com.github.xmaiax.input.InputedAction;
import com.github.xmaiax.input.InputedKey;
import com.github.xmaiax.renderer.Position;
import com.github.xmaiax.renderer.Renderer2D;
import com.github.xmaiax.renderer.TrueTypeFont;
import com.github.xmaiax.renderer.TrueTypeFont.BakedText;

@org.springframework.stereotype.Component
public class GameLogic implements com.github.xmaiax.renderer.GameLifecycle {

  private static final InputedAction ESCAPE_BUTTON_WAS_PRESSED = new InputedAction(InputedKey._ESCAPE, InputEvent.RELEASE);

  private Renderer2D renderer2D;

  @org.springframework.beans.factory.annotation.Autowired
  public GameLogic(Renderer2D renderer2D) { this.renderer2D = renderer2D; }

  private TrueTypeFont genericTTF = new TrueTypeFont("fonts/GorgeousPixel.ttf");
  private FPSCounter fpsCounter = new FPSCounter(42.0f, java.awt.Color.WHITE, 8);
  private DarkTownParallax parallaxBackground = new DarkTownParallax();
  private Player player = new Player();
  private BakedText debugDisplay = null;

  @Override public void load() {
    this.genericTTF.load();
    this.fpsCounter.load(this.genericTTF);
    this.parallaxBackground.load();
    this.player.load();
  }

  @Override public boolean loop(long msSinceLastUpdate, List<InputedAction> inputKeys) {
    this.player.update(msSinceLastUpdate, inputKeys);
    this.parallaxBackground.update(msSinceLastUpdate, this.player.isMoving(), this.player.isFacingBack());

    this.debugDisplay = this.genericTTF.bakeText(
      String.format("Position: %d", this.parallaxBackground.getPosition()),
      80.0f, Color.ORANGE, this.debugDisplay);

    return !inputKeys.contains(ESCAPE_BUTTON_WAS_PRESSED);
  }

  @Override public void render() {
    this.parallaxBackground.render(this.renderer2D);
    this.player.render(this.renderer2D);

    this.debugDisplay.bind();
    this.renderer2D.render2DQuad(new Position(
      this.renderer2D.getVideoSettings().getWidth() / 2 - this.debugDisplay.getDimension().getWidth() / 2,
      this.renderer2D.getVideoSettings().getHeight() / 2 - this.debugDisplay.getDimension().getHeight() / 2),
        this.debugDisplay.getDimension(), false, 1.0d);

    this.fpsCounter.render(this.renderer2D);
  }

  @Override public void shutdown() {
    this.genericTTF.free();
    this.fpsCounter.free();
    this.parallaxBackground.free();
    this.player.free();
    this.debugDisplay.free();
  }

}
