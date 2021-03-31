package com.github.xmaiax

enum class ComplexAnimation(val animation: UniqueAnimation) {
  KNIGHTESS(UniqueAnimation("human", "knightess", mapOf(
    AnimationIdentifier.IDLE         to 15
   ,AnimationIdentifier.RUN          to 15
   ,AnimationIdentifier.LIGHT_ATTACK to 1
   ,AnimationIdentifier.HEAVY_ATTACK to 1
   ,AnimationIdentifier.JUMP         to 1
   ,AnimationIdentifier.FALL         to 1
   ,AnimationIdentifier.DAMAGE       to 1
   ,AnimationIdentifier.DEATH        to 15
  ), 16, 3.5, true)),
}
