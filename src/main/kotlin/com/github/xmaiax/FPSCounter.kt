package com.github.xmaiax

@org.springframework.stereotype.Component
class FPSCounter {

  companion object {
    val ONE_SECOND_IN_MS = 1000L
  }

  private var lastTime = System.currentTimeMillis()

  private var currentText: String = "FPS: ?"
  private var frameCounter: Int = 0
  private var msCounter: Long = 0

  fun update(): Boolean {
    this.msCounter += System.currentTimeMillis() - this.lastTime
    this.lastTime = System.currentTimeMillis()
    this.frameCounter++
    if(this.msCounter >= ONE_SECOND_IN_MS) {
      this.currentText = "FPS: ${this.frameCounter}"
      this.frameCounter = 0
      this.msCounter = 0L
      return true
    }
    return false
  }

  fun getText() = this.currentText

}
