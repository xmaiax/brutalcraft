package com.github.xmaiax

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL30C.*

data class Position(var x: Int = 0, var y: Int = 0)
data class Dimension(val width: Int = -1, val height: Int = -1) {
  fun isValid() = this.width > 0 && this.height > 0
}
interface RenderableObject {
  fun load(): Unit
  fun getDimension(): Dimension
  fun bind(): Unit
  fun free(): Unit
}

data class Texture2D(val resource: String): RenderableObject {
  companion object {
    val LOAD_TEXTURE_FIRST_ERROR_MESSAGE = "Please load the texture before using it."
  }
  private var glIdentifier = 0
  private var dimension = Dimension()
  private var data: java.nio.ByteBuffer? = null
  override fun load() {
    this.glIdentifier = glGenTextures()
    val widthBuffer = BufferUtils.createIntBuffer(1)
    val heightBuffer = BufferUtils.createIntBuffer(1)
    this.data = org.lwjgl.stb.STBImage.stbi_load_from_memory(
      App.getBufferFromResource(this.resource),
      widthBuffer, heightBuffer, BufferUtils.createIntBuffer(1), 4)
    this.dimension = Dimension(widthBuffer.get(), heightBuffer.get())
  }
  override fun getDimension() = if(this.dimension.isValid()) this.dimension
    else throw App.exitWithError(LOAD_TEXTURE_FIRST_ERROR_MESSAGE)
  override fun bind() = this.data?.let {
    glBindTexture(GL_TEXTURE_2D, this.glIdentifier)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
    glTexImage2D(
      GL_TEXTURE_2D, 0, GL_RGBA, this.dimension.width, this.dimension.height,
      0, GL_RGBA, GL_UNSIGNED_BYTE, it)
  } ?: run {
    throw App.exitWithError(LOAD_TEXTURE_FIRST_ERROR_MESSAGE)
  }
  override fun free(): Unit = this.data?.let {
    org.lwjgl.stb.STBImage.stbi_image_free(it) } ?: run { Unit }
}

@org.springframework.stereotype.Component
class Renderer2D(
  @org.springframework.beans.factory.annotation.Autowired
  private val videoSettings: VideoSettings
) {
  companion object {
    private val LOGGER = org.slf4j.LoggerFactory.getLogger(Renderer2D::class.java)
    val SHADER_ERROR_MESSAGE = "Couldn't compile shader"
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
    if (linked == 0) throw App.exitWithError("Couldn't link program")
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
  fun render2DQuad(position: Position, dimension: Dimension, scale: Double = 1.0,
      horizontalInvert: Boolean = false, alpha: Double = 1.0) {
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
