package com.github.xmaiax

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL30C.*

data class Dimension(var width: Int, var height: Int)
data class Position(var x: Int = 0, var y: Int = 0)
data class Texture2D(val resource: String) {
  private var glIdentifier = 0
  val dimension = Dimension(-1, -1)
  private var data: java.nio.ByteBuffer? = null
  fun load() {
    this.glIdentifier = glGenTextures()
    val widthBuffer = BufferUtils.createIntBuffer(1)
    val heightBuffer = BufferUtils.createIntBuffer(1)
    this.data = org.lwjgl.stb.STBImage.stbi_load_from_memory(
      App.getBufferFromResource(this.resource),
      widthBuffer, heightBuffer, BufferUtils.createIntBuffer(1), 4)
    this.dimension.width = widthBuffer.get()
    this.dimension.height = heightBuffer.get()
  }
  fun free() = this.data?.let { org.lwjgl.stb.STBImage.stbi_image_free(it) }
  fun bind() = this.data?.let {
    glBindTexture(GL_TEXTURE_2D, this.glIdentifier)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
    glTexImage2D(
      GL_TEXTURE_2D, 0, GL_RGBA, this.dimension.width, this.dimension.height,
      0, GL_RGBA, GL_UNSIGNED_BYTE, it)
  } ?: run {
    throw App.exitWithError("Please load the texture before binding.")
  }
}

data class AnimationIndex(var index: Int = 0, var msUntilNextFrame: Long = 0,
  var lastAnimation: AnimationIdentifier = AnimationIdentifier.values().first())
data class Animation2D(val directory: String, val framesPerSecond: Int) {
  companion object {
    val JAR_FILE_ENTRY_PREFFIX_URL = "BOOT-INF/classes/"
  }
  private var textures2D: List<Texture2D> = listOf()
  private var lastTimestamp = java.util.Calendar.getInstance().getTimeInMillis()
  private val resetMsUntilNextFrame = 1000L / this.framesPerSecond
  val dimension = Dimension(-1, -1)
  fun load() {
    Thread.currentThread().getContextClassLoader()
        .getResource(this.directory)?.let { url ->
      fun loadAllTexturesWithFileNames(files: List<String>) {
        this.textures2D = files.map { Texture2D("${this.directory}${it}") }
        this.textures2D.forEach { it.load() }
      }
      val dir = java.io.File(url.getFile())
      if(dir.isDirectory()) loadAllTexturesWithFileNames(
        dir.listFiles().map { it.getName() })
      else {
        val zipIS = java.util.zip.ZipInputStream(App::class.java.getProtectionDomain()
          .getCodeSource().getLocation().openStream())
        val files = mutableListOf<String>()
        while(true) if(zipIS.getNextEntry()?.let { entry ->
          files.add(entry.getName())
          false
        } ?: run { true }) break
        loadAllTexturesWithFileNames(files.filter {
          it.startsWith(JAR_FILE_ENTRY_PREFFIX_URL + this.directory) && !it.endsWith("/") }.map {
            it.substring(JAR_FILE_ENTRY_PREFFIX_URL.length).split("/").last() })
      }
      this.dimension.width = this.textures2D.first().dimension.width
      this.dimension.height = this.textures2D.first().dimension.height
    } ?: run {
      throw App.exitWithError("Unable to load animation directory: ${this.directory}")
    }
  }
  fun free() = this.textures2D.forEach { it.free() }
  fun bind(animationIndex: AnimationIndex) {
    if(animationIndex.index > -1 && this.textures2D.isNotEmpty()) {
      this.textures2D.get(animationIndex.index).bind()
      animationIndex.msUntilNextFrame -= java.util.Calendar.getInstance().getTimeInMillis() - this.lastTimestamp
      if(animationIndex.msUntilNextFrame < 1) {
        animationIndex.msUntilNextFrame = this.resetMsUntilNextFrame.toLong()
        if(animationIndex.index < this.textures2D.size - 1) animationIndex.index++
        else animationIndex.index = 0
      }
      this.lastTimestamp = java.util.Calendar.getInstance().getTimeInMillis()
    }
    else throw App.exitWithError("Please load the animation before binding.")
  }
}

enum class AnimationIdentifier(val description: String) {
  IDLE("idle"),
  RUN("run"),
  LIGHT_ATTACK("light-attack"),
  HEAVY_ATTACK("heavy-attack"),
  JUMP("jump"),
  FALL("fall"),
  DAMAGE("damage"),
  DEATH("death"),
  ; override fun toString() = this.description
}

open class UniqueAnimation(val species: String, val _class: String,
  val animationsFPS: Map<AnimationIdentifier, Int>,
  val horizontalInvertFactor: Int = 1, val _scale: Double = 1.0, val _centered: Boolean = true) {
  private val animations = mutableMapOf<AnimationIdentifier, Animation2D>()
  private var mirroredDiff = -1
  fun load() {
    this.animationsFPS.map { animationFPS ->
      val animation = Animation2D(Renderer2D.TEXTURES_RELATIVE_PATH_PREFFIX +
        "${this.species}/${this._class}/${animationFPS.key}/", animationFPS.value)
      animation.load()
      this.animations.put(animationFPS.key, animation)
    }
    this.mirroredDiff = (this.dimension(this.animations.toList().first().component1())
      .width.toDouble() * this._scale).toInt() / this.horizontalInvertFactor
  }
  fun free() = this.animations.forEach { it.value.free() }
  fun dimension(identifier: AnimationIdentifier) = this.animations.get(identifier)?.let {
    it.dimension } ?: run { Dimension(-1, -1) }
  fun render(renderer: Renderer2D, position: Position, animationIdentifier: AnimationIdentifier,
    animationIndex: AnimationIndex, horizontalInvert: Boolean = false,
      alpha: Double = 1.0) = this.animations.get(animationIdentifier)?.let { currentAnimation ->
    if(animationIndex.lastAnimation != animationIdentifier) {
      animationIndex.index = 0
      animationIndex.msUntilNextFrame = 0L
    }
    animationIndex.lastAnimation = animationIdentifier
    currentAnimation.bind(animationIndex)
    renderer.render2DQuad(if(this._centered) Position(position.x + (
      if(horizontalInvert) (this.dimension(animationIdentifier).width.toDouble() * this._scale).toInt() / 16 else 0
    ) -((currentAnimation.dimension.width * this._scale).toInt() / 2),
      position.y - ((currentAnimation.dimension.height * this._scale).toInt() / 2)
    ) else position, currentAnimation.dimension, this._scale, horizontalInvert, alpha)
  }
}

open class RenderableEntity(
  val animation: UniqueAnimation,
  val animationIndex: AnimationIndex = AnimationIndex(),
  val position: Position = Position(),
  var horizontalInvert: Boolean = false,
  var isMoving: Boolean = false,
  var onTheFloor: Boolean = true,
  var isFalling: Boolean = false) {
    fun render(renderer: Renderer2D, animationIdentifier: AnimationIdentifier,
      alpha: Double = 1.0) = this.animation.render(renderer, this.position,
        animationIdentifier, this.animationIndex, this.horizontalInvert, alpha)
}

@org.springframework.stereotype.Component
class Renderer2D(
  @org.springframework.beans.factory.annotation.Autowired
  private val videoSettings: VideoSettings
) {
  companion object {
    private val LOGGER = org.slf4j.LoggerFactory.getLogger(Renderer2D::class.java)
    val TEXTURES_RELATIVE_PATH_PREFFIX = "textures/"
    val SHADER_ERROR_MESSAGE = "Could not compile shader"
  }
  var window = -1L
  private var program = -1
  private var programInputPosition = -1
  private var programInputTextureCoordinates = -1
  private var programInputAlpha = -1
  fun initialize(): Int? {
    if(this.program != -1) return null
    this.program = glCreateProgram()
    fun createShader(resource: String, type: Int): Int {
      val source = App.getBufferFromResource(resource)
      val shader = glCreateShader(type)
      val strings = BufferUtils.createPointerBuffer(1)
      strings.put(0, source)
      val lengths = BufferUtils.createIntBuffer(1)
      lengths.put(0, source.remaining())
      glShaderSource(shader, strings, lengths)
      glCompileShader(shader)
      val compiled = glGetShaderi(shader, GL_COMPILE_STATUS)
      val shaderLog = glGetShaderInfoLog(shader)
      LOGGER.info("Shader '${resource}' log: ${
        if(shaderLog.isBlank()) "No errors or warnings found." else shaderLog}")
      if (compiled == 0) throw App.exitWithError(SHADER_ERROR_MESSAGE)
      return shader
    }
    val vShader = createShader("shaders/basic2d.vs", GL_VERTEX_SHADER)
    val fShader = createShader("shaders/basic2d.fs", GL_FRAGMENT_SHADER)
    glAttachShader(this.program, vShader)
    glAttachShader(this.program, fShader)
    glLinkProgram(this.program)
    val linked = glGetProgrami(this.program, GL_LINK_STATUS)
    val programInfoLog = glGetProgramInfoLog(this.program)
    if (programInfoLog.isNotBlank()) LOGGER.warn(programInfoLog)
    if (linked == 0) throw App.exitWithError("Could not link program")
    glUseProgram(this.program)
    val texLocation = glGetUniformLocation(this.program, "_texture")
    glUniform1i(texLocation, 0)
    this.programInputPosition = glGetAttribLocation(this.program, "position")
    this.programInputTextureCoordinates = glGetAttribLocation(this.program, "textureCoordinates")
    this.programInputAlpha = glGetAttribLocation(this.program, "alpha")
    return this.program
  }
  private fun setFloatVariableToShader(value: FloatArray, programAttributeLocation: Int) {
    glBindBuffer(GL_ARRAY_BUFFER, glGenBuffers());
    glBufferData(GL_ARRAY_BUFFER,
      BufferUtils.createFloatBuffer(value.size).put(value).flip() as java.nio.FloatBuffer,
      GL_STATIC_DRAW)
    glVertexAttribPointer(programAttributeLocation, 2, GL_FLOAT, true, 0, 0L);
    glEnableVertexAttribArray(programAttributeLocation)
  }
  fun render2DQuad(position: Position, dimension: Dimension,
      scale: Double, horizontalInvert: Boolean, alpha: Double) {
    glBindVertexArray(glGenVertexArrays())
    val sizeX = (dimension.width.toDouble() * 2.0 * scale.toDouble() /
      this.videoSettings.width.toDouble()).toFloat()
    val sizeY = (dimension.height.toDouble() * 2.0 * scale.toDouble() /
      this.videoSettings.height.toDouble()).toFloat()
    val startingPositionX: Float = ((2.0 * position.x.toDouble() - this.videoSettings.width.toDouble()) /
      this.videoSettings.width.toDouble()).toFloat()
    val startingPositionY: Float = ((2.0 * position.y.toDouble() - this.videoSettings.height.toDouble()) /
      this.videoSettings.height.toDouble()).toFloat() + sizeY
    this.setFloatVariableToShader(
      if (horizontalInvert) floatArrayOf(
        startingPositionX + sizeX, -startingPositionY,
        startingPositionX, -startingPositionY,
        startingPositionX, -startingPositionY + sizeY,
        startingPositionX, -startingPositionY + sizeY,
        startingPositionX + sizeX, -startingPositionY + sizeY,
        startingPositionX + sizeX, -startingPositionY
      ) else floatArrayOf(
        startingPositionX, -startingPositionY,
        startingPositionX + sizeX, -startingPositionY,
        startingPositionX + sizeX, -startingPositionY + sizeY,
        startingPositionX + sizeX, -startingPositionY + sizeY,
        startingPositionX, -startingPositionY + sizeY,
        startingPositionX, -startingPositionY
      ), this.programInputPosition)
    this.setFloatVariableToShader(floatArrayOf(
      0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f,
      1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f
    ), this.programInputTextureCoordinates)
    this.setFloatVariableToShader(floatArrayOf(
      alpha.toFloat(), alpha.toFloat(),
      alpha.toFloat(), alpha.toFloat(),
      alpha.toFloat(), alpha.toFloat(),
      alpha.toFloat(), alpha.toFloat(),
      alpha.toFloat(), alpha.toFloat(),
      alpha.toFloat(), alpha.toFloat()
    ), this.programInputAlpha)
    glBindBuffer(GL_ARRAY_BUFFER, 0)
    glDrawArrays(GL_TRIANGLES, 0, 6)
    glBindVertexArray(0)
  }
}
