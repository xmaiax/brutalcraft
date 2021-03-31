package com.github.xmaiax

data class Player(val entity: RenderableEntity
    ,private var level: Short = 1
    ,private val experience: Int = 0) {
  fun updateMovement(inputKeys: List<InputedAction>) {
    if(!this.entity.isMoving && inputKeys.filter { (it.key == InputedKey._LEFT || it.key == InputedKey._RIGHT ||
      it.key == InputedKey._GAMEPAD_DPAD_LEFT || it.key == InputedKey._GAMEPAD_DPAD_RIGHT)
        && (it.event == InputEvent.PRESS || it.event == InputEvent.REPEAT) }.isNotEmpty()) this.entity.isMoving = true
    if(this.entity.isMoving && inputKeys.filter { (it.key == InputedKey._LEFT || it.key == InputedKey._RIGHT ||
      it.key == InputedKey._GAMEPAD_DPAD_LEFT || it.key == InputedKey._GAMEPAD_DPAD_RIGHT)
        && it.event == InputEvent.RELEASE }.isNotEmpty()) this.entity.isMoving = false
    if(inputKeys.contains(InputedAction(InputedKey._LEFT, InputEvent.PRESS)) ||
       inputKeys.contains(InputedAction(InputedKey._GAMEPAD_DPAD_LEFT, InputEvent.PRESS)))
      this.entity.horizontalInvert = true
    if(inputKeys.contains(InputedAction(InputedKey._RIGHT, InputEvent.PRESS)) ||
       inputKeys.contains(InputedAction(InputedKey._GAMEPAD_DPAD_RIGHT, InputEvent.PRESS)))
      this.entity.horizontalInvert = false
  }
  fun render(renderer: Renderer2D) = this.entity.render(renderer,
    if(this.entity.isMoving) AnimationIdentifier.RUN else AnimationIdentifier.IDLE, 1.0)
}

fun Player(uniqueAnimation: UniqueAnimation, screenWidth: Int, screenHeight: Int) =
  Player(RenderableEntity(uniqueAnimation, position = Position(screenWidth / 2, screenHeight / 2)))
