package com.github.xmaiax

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.context.annotation.Bean

@org.springframework.context.annotation.Configuration open class GameConfigurations {
  @Bean open fun fpsCounterConfigs() = FPSCounter.FPSCounterConfig(42.0f, java.awt.Color.WHITE, 8)
}

@Component class Game(@Autowired val renderer: Renderer2D): GameLifecycle {

  companion object {
    private val LOGGER = org.slf4j.LoggerFactory.getLogger(Game::class.java)
  }

  @Autowired lateinit var fpsCounter: FPSCounter

  val genericTTF = TrueTypeFont("fonts/GorgeousPixel.ttf")

  var isMoving = false
  var isFacingBack = false
  val warriorScale = 5.0
  val warriorMirrorWidthCorrection = 12

  val warriorWalk = Animation2D("textures/character/warrior/walk/")
  val warriorWalkIndex = Animation2DIndex(80)

  val warriorIdle = Animation2D("textures/character/warrior/idle/")
  val warriorIdleIndex = Animation2DIndex(90)

  val moveLeftKeys = listOf(InputedKey._LEFT, InputedKey._A, InputedKey._GAMEPAD_DPAD_LEFT)
  val moveRightKeys = listOf(InputedKey._RIGHT, InputedKey._D, InputedKey._GAMEPAD_DPAD_RIGHT)
  val lastPressedKeys = mutableListOf<InputedKey>()

  val SDF = java.text.SimpleDateFormat("HH:mm:ss")
  var oldExampleText = "?"
  var timeRenderableText: TrueTypeFont? = null
  private fun updateExampleText() = SDF.format(java.util.Calendar.getInstance().getTime()).let { curStrTime ->
    if(!curStrTime.contentEquals(this.oldExampleText)) {
      this.oldExampleText = "${curStrTime}"
      this.genericTTF.bakeText(this.oldExampleText)
      this.timeRenderableText?.let {
        it.update(this.genericTTF.getData(), this.genericTTF.getDimension())
      }
    }
  }

  override fun load() {
    this.genericTTF.load()
    this.fpsCounter.load(this.genericTTF)
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
        this.lastPressedKeys.any { it in this.moveRightKeys }) this.isMoving = false

    if(this.isMoving)
      this.isFacingBack = this.lastPressedKeys.any { it in this.moveLeftKeys }

    if(this.isMoving)
      this.warriorWalk.update(msSinceLastUpdate, this.warriorWalkIndex)
    else
      this.warriorIdle.update(msSinceLastUpdate, this.warriorIdleIndex)

    this.updateExampleText()

    return !inputKeys.contains(InputedAction(InputedKey._ESCAPE, InputEvent.RELEASE))
  }

  override fun render() {
    (if(this.isMoving) this.warriorWalk else this.warriorIdle).let { currentAnimation ->
      currentAnimation.bind()
      this.renderer.render2DQuad(Position(currentAnimation.getDimension(this.warriorScale).width / 10
          -(if(this.isFacingBack) (this.warriorMirrorWidthCorrection * this.warriorScale).toInt() else 0) +
        this.renderer.videoSettings.width  / 2 - currentAnimation.getDimension(this.warriorScale).width / 2,
        this.renderer.videoSettings.height * 3 / 4 - currentAnimation.getDimension(this.warriorScale).height / 2),
          currentAnimation.getDimension(this.warriorScale), this.isFacingBack)
    }

    this.timeRenderableText?.let { trt -> trt.getData()?.let { trt.bind()
      this.renderer.render2DQuad(Position(this.renderer.videoSettings.width / 2 - trt.getDimension().width / 2,
        this.renderer.videoSettings.height / 2 - trt.getDimension().height / 2), trt.getDimension())
      }} ?: run { this.timeRenderableText = this.genericTTF.copy() }

    this.fpsCounter.render()
  }

  override fun shutdown() {
    this.genericTTF.free()
    this.warriorWalk.free()
    this.warriorIdle.free()
  }

}
