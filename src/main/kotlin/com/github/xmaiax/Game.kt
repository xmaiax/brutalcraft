package com.github.xmaiax

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.awt.Color
import java.text.SimpleDateFormat
import java.util.Date

@Component open class Game(
  @Autowired val renderer: Renderer2D,
  @Autowired val videoSettings: VideoSettings
): GameLifecycle {

  companion object {
    private val LOGGER = org.slf4j.LoggerFactory.getLogger(Game::class.java)
  }

  val monstroTexture2D = Texture2D("textures/monster/beholder.png")
  val trueTypeFont = TrueTypeFont("fonts/FFF_Tusj.ttf")

  override fun load() {
    this.monstroTexture2D.load()
    this.trueTypeFont.load()
  }

  private val sdf = SimpleDateFormat("HH:mm:ss")
  private var hora = ""

  override fun loop(msSinceLastUpdate: Long, inputKeys: List<InputedAction>): Boolean {
    val horaAtual = this.sdf.format(Date())
    if(hora != horaAtual) {
      hora = horaAtual
      this.trueTypeFont.bakeText(horaAtual, 120.0f, Color.ORANGE)
    }
    return !inputKeys.contains(InputedAction(InputedKey._ESCAPE, InputEvent.RELEASE))
  }

  override fun render() {
    this.monstroTexture2D.bind()
    this.renderer.render2DQuad(Position(this.videoSettings.width / 2 - this.monstroTexture2D.getDimension().width,
      this.videoSettings.height / 2 - this.monstroTexture2D.getDimension().height), this.monstroTexture2D.getDimension(), 2.0)
    this.trueTypeFont.bind()
    this.renderer.render2DQuad(Position(this.videoSettings.width / 2 - this.trueTypeFont.getDimension().width / 2,
      this.videoSettings.height / 2 - this.trueTypeFont.getDimension().height / 2), this.trueTypeFont.getDimension())
  }

  override fun shutdown() {
    this.trueTypeFont.free()
  }

}
