package com.github.xmaiax

@org.springframework.stereotype.Component open class Game(
  @org.springframework.beans.factory.annotation.Autowired val renderer: Renderer2D,
  @org.springframework.beans.factory.annotation.Autowired val videoSettings: VideoSettings
): GameLifecycle {

  companion object {
    private val LOGGER = org.slf4j.LoggerFactory.getLogger(Game::class.java)
  }

  private val player = Player(ComplexAnimation.KNIGHTESS.animation,
    this.videoSettings.width, this.videoSettings.height)

  override fun load() {
    ComplexAnimation.values().forEach { it.animation.load() }
  }

  override fun loop(msSinceLastUpdate: Long, inputKeys: List<InputedAction>): Boolean {
    this.player.updateMovement(inputKeys)
    return !inputKeys.contains(InputedAction(InputedKey._ESCAPE, InputEvent.RELEASE))
  }

  override fun render() {
    this.player.render(this.renderer)
  }

  override fun shutdown() {
    ComplexAnimation.values().forEach { it.animation.free() }
  }

}
