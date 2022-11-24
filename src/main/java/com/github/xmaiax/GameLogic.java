package com.github.xmaiax;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;
import com.github.xmaiax.input.InputEvent;
import com.github.xmaiax.input.InputedAction;
import com.github.xmaiax.input.InputedKey;
import com.github.xmaiax.renderer.Animation2D;
import com.github.xmaiax.renderer.Animation2D.Animation2DIndex;
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
  private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
  private BakedText timeDisplay = null;

  private Animation2D warriorWalk = new Animation2D("textures/character/warrior/walk/");
  private Animation2DIndex warriorWalkIndex = new Animation2DIndex(80);

  private Animation2D warriorIdle = new Animation2D("textures/character/warrior/idle/");
  private Animation2DIndex warriorIdleIndex = new Animation2DIndex(90);

  private List<InputedKey> moveLeftKeys = Arrays.asList(new InputedKey[] {
    InputedKey._LEFT, InputedKey._A, InputedKey._GAMEPAD_DPAD_LEFT,
  });
  private List<InputedKey> moveRightKeys = Arrays.asList(new InputedKey[] {
    InputedKey._RIGHT, InputedKey._D, InputedKey._GAMEPAD_DPAD_RIGHT,
  });
  private List<InputedKey> lastPressedKeys = new ArrayList<>();

  private boolean isMoving = true;
  private boolean isFacingBack = false;
  private double warriorScale = 5.0d;
  private int warriorMirrorWidthCorrection = 12;

  @Override public void load() {
    this.genericTTF.load();
    this.fpsCounter.load(this.genericTTF);
    this.warriorWalk.load();
    this.warriorIdle.load();
  }

  @Override public boolean loop(long msSinceLastUpdate, List<InputedAction> inputKeys) {

    final List<InputedAction> keysRange = inputKeys.stream().filter(ik -> this
          .moveLeftKeys.contains(ik.getKey()) ||
      this.moveRightKeys.contains(ik.getKey())).collect(Collectors.toList());

    this.lastPressedKeys.addAll(keysRange.stream()
      .filter(kr -> InputEvent.PRESS.equals(kr.getEvent()) || InputEvent.REPEAT.equals(kr.getEvent()))
      .map(kr -> kr.getKey()).collect(Collectors.toList()));

    this.lastPressedKeys.removeAll(keysRange.stream()
      .filter(kr -> InputEvent.RELEASE.equals(kr.getEvent()))
      .map(kr -> kr.getKey()).collect(Collectors.toList()));

    this.isMoving = !this.isMoving && !this.lastPressedKeys.isEmpty() ? true :
      (this.isMoving && this.lastPressedKeys.isEmpty() ? false : this.isMoving);

    if(this.isMoving &&
       this.lastPressedKeys.stream().anyMatch(lpk -> this.moveLeftKeys.contains(lpk)) &&
       this.lastPressedKeys.stream().anyMatch(lpk -> this.moveRightKeys.contains(lpk)))
      this.isMoving = false;

    if(this.isMoving) this.isFacingBack = this.lastPressedKeys.stream()
      .anyMatch(lpk -> this.moveLeftKeys.contains(lpk));

    if(this.isMoving)
      this.warriorWalk.update(msSinceLastUpdate, this.warriorWalkIndex);
    else
      this.warriorIdle.update(msSinceLastUpdate, this.warriorIdleIndex);

    this.timeDisplay = this.genericTTF.bakeText(
      this.sdf.format(Calendar.getInstance().getTime()),
      80.0f, Color.ORANGE, this.timeDisplay);

    return !inputKeys.contains(ESCAPE_BUTTON_WAS_PRESSED);

  }

  @Override
  public void render() {

    this.timeDisplay.bind();
    this.renderer2D.render2DQuad(new Position(
      this.renderer2D.getVideoSettings().getWidth() / 2 - this.timeDisplay.getDimension().getWidth() / 2,
      this.renderer2D.getVideoSettings().getHeight() / 2 - this.timeDisplay.getDimension().getHeight() / 2),
        this.timeDisplay.getDimension(), false, 1.0d);

    final Animation2D currentAnimation = (this.isMoving ? this.warriorWalk : this.warriorIdle);
    currentAnimation.bind();
    this.renderer2D.render2DQuad(new Position(
        currentAnimation.getDimension(this.warriorScale).getWidth() / 10
       -(this.isFacingBack ? Double.valueOf(Double.valueOf(this.warriorMirrorWidthCorrection) * this.warriorScale).intValue() : 10)
       + this.renderer2D.getVideoSettings().getWidth() / 2 - currentAnimation.getDimension(this.warriorScale).getWidth() / 2,
        this.renderer2D.getVideoSettings().getHeight() * 3 / 4 - currentAnimation.getDimension(this.warriorScale).getHeight() / 2),
      currentAnimation.getDimension(this.warriorScale), isFacingBack, 1.0d);

    this.fpsCounter.render(this.renderer2D);
  }

  @Override
  public void shutdown() {
    this.fpsCounter.free();
    this.genericTTF.free();
    this.warriorWalk.free();
    this.warriorIdle.free();
  }

}
