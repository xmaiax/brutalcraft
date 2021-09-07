package com.github.xmaiax

import org.springframework.beans.factory.annotation.Autowired

@org.springframework.stereotype.Component class Game(
  @Autowired val renderer: Renderer2D,
  @Autowired val videoSettings: VideoSettings
): GameLifecycle {

  companion object {
    private val LOGGER = org.slf4j.LoggerFactory.getLogger(Game::class.java)
  }

  var isMoving = false
  var isFacingBack = false
  val warriorScale = 4.0
  val warriorMirrorWidthCorrection = 12

  val warriorWalk = Animation2D("textures/character/warrior/walk/")
  val warriorWalkIndex = Animation2DIndex(90)

  val warriorIdle = Animation2D("textures/character/warrior/idle/")
  val warriorIdleIndex = Animation2DIndex(90)

  val moveLeftKeys = listOf(InputedKey._LEFT, InputedKey._A, InputedKey._GAMEPAD_DPAD_LEFT)
  val moveRightKeys = listOf(InputedKey._RIGHT, InputedKey._D, InputedKey._GAMEPAD_DPAD_RIGHT)
  val lastPressedKeys = mutableListOf<InputedKey>()

  override fun load() {
    this.warriorWalk.load()
    this.warriorIdle.load()
  }

  override fun loop(msSinceLastUpdate: Long, inputKeys: List<InputedAction>): Boolean {

    val keysRange = inputKeys.filter {
      this.moveLeftKeys.contains(it.key) || this.moveRightKeys.contains(it.key) }

    this.lastPressedKeys.addAll(keysRange.filter {
      it.event == InputEvent.PRESS || it.event == InputEvent.REPEAT
    }.map { it.key })
    this.lastPressedKeys.removeAll(keysRange.filter {
      it.event == InputEvent.RELEASE
    }.map { it.key })

    this.isMoving =
      if(!this.isMoving && lastPressedKeys.isNotEmpty()) true
      else if(this.isMoving && lastPressedKeys.isEmpty()) false
      else this.isMoving

    if(this.isMoving && this.lastPressedKeys.any { it in this.moveLeftKeys } &&
        this.lastPressedKeys.any { it in this.moveRightKeys }) {
      this.isMoving = false
    }

    if(this.isMoving)
      this.isFacingBack = this.lastPressedKeys.any { it in this.moveLeftKeys }

    if(this.isMoving)
      this.warriorWalk.update(msSinceLastUpdate, this.warriorWalkIndex)
    else
      this.warriorIdle.update(msSinceLastUpdate, this.warriorIdleIndex)

    return !inputKeys.contains(InputedAction(InputedKey._ESCAPE, InputEvent.RELEASE))
  }

  override fun render() {

    if(this.isMoving)
      this.warriorWalk.bind()
    else
      this.warriorIdle.bind()

    this.renderer.render2DQuad(Position(
        -(if(this.isFacingBack) (this.warriorMirrorWidthCorrection * this.warriorScale).toInt() else 0) +
      this.videoSettings.width  / 2 - this.warriorWalk.getDimension(this.warriorScale).width / 2,
      this.videoSettings.height / 2 - this.warriorWalk.getDimension(this.warriorScale).height / 2),
        this.warriorWalk.getDimension(this.warriorScale), this.isFacingBack)

  }

  override fun shutdown() {
    this.warriorWalk.free()
    this.warriorIdle.free()
  }

}
