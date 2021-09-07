package com.github.xmaiax

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class Game(
  @Autowired val renderer: Renderer2D,
  @Autowired val videoSettings: VideoSettings
): GameLifecycle {

  companion object {
    private val LOGGER = org.slf4j.LoggerFactory.getLogger(Game::class.java)
  }

  var isWalking = false
  val warriorScale = 1.75

  val warriorIdle = Animation2D("textures/character/warrior/idle/")
  val warriorIdleIndex = Animation2DIndex(90)

  val warriorWalk = Animation2D("textures/character/warrior/walk/")
  val warriorWalkIndex = Animation2DIndex(90)

  override fun load() {
    this.warriorIdle.load()
    this.warriorWalk.load()
  }

  override fun loop(msSinceLastUpdate: Long, inputKeys: List<InputedAction>): Boolean {
    val currentIsWalking =
      inputKeys.contains(InputedAction(InputedKey._GAMEPAD_DPAD_RIGHT, InputEvent.PRESS)) ||
      inputKeys.contains(InputedAction(InputedKey._GAMEPAD_DPAD_RIGHT, InputEvent.REPEAT))

    if(currentIsWalking != this.isWalking) {
      this.warriorIdleIndex.reset()
      this.warriorWalkIndex.reset()
      this.isWalking = currentIsWalking
    }

    if(this.isWalking)
      this.warriorWalk.update(msSinceLastUpdate, this.warriorWalkIndex)
    else
      this.warriorIdle.update(msSinceLastUpdate, this.warriorIdleIndex)

    return !inputKeys.contains(InputedAction(InputedKey._ESCAPE, InputEvent.RELEASE))
  }

  override fun render() {
    if(this.isWalking) this.warriorWalk.bind()
    else this.warriorIdle.bind()
    this.renderer.render2DQuad(Position(this.videoSettings.width / 2 - this.warriorIdle.getDimension(this.warriorScale).width / 2,
      this.videoSettings.height / 2 - this.warriorIdle.getDimension(this.warriorScale).height / 2), this.warriorIdle.getDimension(this.warriorScale), this.warriorScale)
  }

  override fun shutdown() {
    this.warriorIdle.free()
    this.warriorWalk.free()
  }

}
