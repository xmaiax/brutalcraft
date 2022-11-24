package com.github.xmaiax.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import com.github.xmaiax.input.InputEvent;
import com.github.xmaiax.input.InputedAction;
import com.github.xmaiax.input.InputedKey;
import com.github.xmaiax.renderer.Animation2D;
import com.github.xmaiax.renderer.Position;
import com.github.xmaiax.renderer.Renderer2D;
import com.github.xmaiax.renderer.Animation2D.Animation2DIndex;

public class Player {

  private static final String TEXTURES_PREFIX = "textures/character/player/";

  private Animation2D walkAnimation = new Animation2D(TEXTURES_PREFIX + "walk/");
  private Animation2DIndex walkIndex = new Animation2DIndex(80);

  private Animation2D idleAnimation = new Animation2D(TEXTURES_PREFIX + "idle/");
  private Animation2DIndex idleIndex = new Animation2DIndex(90);

  private static final List<InputedKey> MOVE_LEFT_KEYS = Arrays.asList(new InputedKey[] {
    InputedKey._LEFT, InputedKey._A, InputedKey._GAMEPAD_DPAD_LEFT,
  });
  private static final List<InputedKey> MOVE_RIGHT_KEYS = Arrays.asList(new InputedKey[] {
    InputedKey._RIGHT, InputedKey._D, InputedKey._GAMEPAD_DPAD_RIGHT,
  });
  private final List<InputedKey> lastPressedKeys = new ArrayList<>();

  private boolean moving = false;
  public boolean isMoving() { return this.moving; }

  private boolean facingBack = false;
  public boolean isFacingBack() { return this.facingBack; }

  private static final double SCALE = 5.0d;
  private static final int MIRROR_WIDTH_CORRECTION = 12;

  public void load() {
    this.walkAnimation.load();
    this.idleAnimation.load();
  }

  public void update(long msSinceLastUpdate, List<InputedAction> inputKeys) {
    final List<InputedAction> keysRange = inputKeys.stream()
      .filter(ik -> MOVE_LEFT_KEYS.contains(ik.getKey()) ||
                    MOVE_RIGHT_KEYS.contains(ik.getKey())).collect(Collectors.toList());
    this.lastPressedKeys.addAll(keysRange.stream()
      .filter(kr -> InputEvent.PRESS.equals(kr.getEvent()) || InputEvent.REPEAT.equals(kr.getEvent()))
      .map(kr -> kr.getKey()).collect(Collectors.toList()));
    this.lastPressedKeys.removeAll(keysRange.stream()
      .filter(kr -> InputEvent.RELEASE.equals(kr.getEvent()))
      .map(kr -> kr.getKey()).collect(Collectors.toList()));
    this.moving = !this.moving && !this.lastPressedKeys.isEmpty() ? true :
      (this.moving && this.lastPressedKeys.isEmpty() ? false : this.moving);
    if(this.moving &&
       this.lastPressedKeys.stream().anyMatch(lpk -> MOVE_LEFT_KEYS.contains(lpk)) &&
       this.lastPressedKeys.stream().anyMatch(lpk -> MOVE_RIGHT_KEYS.contains(lpk)))
      this.moving = false;
    if(this.moving) this.facingBack = this.lastPressedKeys.stream()
      .anyMatch(lpk -> MOVE_LEFT_KEYS.contains(lpk));
    if(this.moving) this.walkAnimation.update(msSinceLastUpdate, this.walkIndex);
    else this.idleAnimation.update(msSinceLastUpdate, this.idleIndex);
  }

  public void render(Renderer2D renderer2D) {
    final Animation2D currentAnimation = (this.moving ? this.walkAnimation : this.idleAnimation);
    currentAnimation.bind();
    renderer2D.render2DQuad(new Position(
      currentAnimation.getDimension(SCALE).getWidth() / 10
     -(this.facingBack ? Double.valueOf(Double.valueOf(MIRROR_WIDTH_CORRECTION) * SCALE).intValue() : 10)
     + renderer2D.getVideoSettings().getWidth() / 2 - currentAnimation.getDimension(SCALE).getWidth() / 2,
       renderer2D.getVideoSettings().getHeight() * 3 / 4 - currentAnimation.getDimension(SCALE).getHeight() / 2),
    currentAnimation.getDimension(SCALE), this.facingBack, 1.0d);
  }

  public void free() {
    this.walkAnimation.free();
    this.idleAnimation.free();
  }

}
