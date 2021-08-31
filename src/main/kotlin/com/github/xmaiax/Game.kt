package com.github.xmaiax

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component open class Game(
  @Autowired val renderer: Renderer2D,
  @Autowired val videoSettings: VideoSettings
): GameLifecycle {

  companion object {
    private val LOGGER = org.slf4j.LoggerFactory.getLogger(Game::class.java)
  }

  val beholderTexture2D = Texture2D("textures/monster/beholder.png")

  override fun load() {
    this.beholderTexture2D.load()
  }

  override fun loop(msSinceLastUpdate: Long, inputKeys: List<InputedAction>): Boolean {
    return !inputKeys.contains(InputedAction(InputedKey._ESCAPE, InputEvent.RELEASE))
  }

  override fun render() {
    this.beholderTexture2D.bind()
    this.renderer.render2DQuad(Position(300, 300), this.beholderTexture2D.getDimension())
  }

  override fun shutdown() {
    this.beholderTexture2D.free()
  }

}
