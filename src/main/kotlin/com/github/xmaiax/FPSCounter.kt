package com.github.xmaiax

@org.springframework.stereotype.Component
class FPSCounter: RenderableObject {

  companion object {
    val TOP_RIGHT_MARGIN = 10
  }

  override var glIdentifier: Int = -1
  override var data: java.nio.ByteBuffer? = null
  override var dimension = Dimension()

  private var lastTime = System.currentTimeMillis()

  private var currentText: String = " "
  private var frameCounter: Int = 0
  private var msCounter: Long = 0

  override fun load() = Unit

  private fun update(): Boolean {
    this.msCounter += System.currentTimeMillis() - this.lastTime
    this.lastTime = System.currentTimeMillis()
    this.frameCounter++
    if(this.msCounter >= 1000L) {
      this.currentText = "FPS: ${this.frameCounter}"
      this.frameCounter = 0
      this.msCounter = 0L
      return true
    }
    return false
  }

  fun render(font: TrueTypeFont, renderer: Renderer2D) {
    if(this.update()) {
      font.bakeText(this.currentText)
      this.glIdentifier = font.glIdentifier
      this.data = font.data
      this.dimension = font.dimension
    }
    this.data?.let { this.bind()
      renderer.render2DQuad(Position(
        renderer.videoSettings.width - this.getDimension().width - TOP_RIGHT_MARGIN,
          TOP_RIGHT_MARGIN), this.getDimension())
    }
  }

}
