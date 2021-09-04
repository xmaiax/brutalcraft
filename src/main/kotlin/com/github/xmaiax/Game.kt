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

  val warrior = Animation2D("textures/character/warrior/idle/")

  override fun load() {
    warrior.load()
  }


  override fun loop(msSinceLastUpdate: Long, inputKeys: List<InputedAction>): Boolean {
    this.warrior.update(msSinceLastUpdate)
    return !inputKeys.contains(InputedAction(InputedKey._ESCAPE, InputEvent.RELEASE))
  }

  override fun render() {
    this.warrior.bind()
    this.renderer.render2DQuad(Position(this.videoSettings.width / 2 - this.warrior.getDimension().width,
      this.videoSettings.height / 2 - this.warrior.getDimension().height), this.warrior.getDimension())
  }

  override fun shutdown() {
    warrior.free()
  }

}
