package com.github.xmaiax

import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.nio.ByteBuffer
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL30C.*

data class Position(var x: Int = 0, var y: Int = 0)
data class Dimension(val width: Int = -1, val height: Int = -1) {
  fun isValid() = this.width > 0 && this.height > 0
  fun scaleUp(scale: Double) = Dimension(
    (this.width.toDouble() * scale).toInt(),
    (this.height.toDouble() * scale).toInt())
}

interface RenderableObject {
  companion object {
    val LOAD_RESOURCE_BEFORE_USING = "Please load the resource before using it."
  }
  var glIdentifier: Int
  var data: java.nio.ByteBuffer?
  var dimension: Dimension
  fun load(): Unit
  fun getDimension(scale: Double? = null) = if(this.dimension.isValid())
      scale?.let { this.dimension.scaleUp(it) } ?: run { this.dimension }
    else this.throwResourceNotLoadedException()
  fun bind() = this.data?.let {
    glBindTexture(GL_TEXTURE_2D, this.glIdentifier)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
    glTexImage2D(
      GL_TEXTURE_2D, 0, GL_RGBA, this.dimension.width, this.dimension.height,
      0, GL_RGBA, GL_UNSIGNED_BYTE, it)
  } ?: run { this.throwResourceNotLoadedException() }
  fun free(): Unit = this.data?.let {
    org.lwjgl.stb.STBImage.stbi_image_free(it) } ?: run { Unit }
  fun throwResourceNotLoadedException(): Nothing = throw App.exitWithError(
    RenderableObject.LOAD_RESOURCE_BEFORE_USING)
}

data class TrueTypeFont(val resource: String): RenderableObject {
  override var glIdentifier: Int = -1
  override var data: java.nio.ByteBuffer? = null
  override var dimension = Dimension()
  private var font: Font? = null
  override fun load() {
    this.font = Font.createFont(Font.TRUETYPE_FONT,
      App.getUrlFromResource(this.resource).openStream())
  }
  fun bakeText(text: String, fontSize: Float, color: java.awt.Color) {
    var input = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
    var graphics2d = input.createGraphics()
    fun setFontSize() = this.font?.let {
      graphics2d.setFont(it.deriveFont(Font.PLAIN, fontSize)) } ?: run {
        super.throwResourceNotLoadedException()
      }
    setFontSize()
    var metrics = graphics2d.getFontMetrics()
    this.dimension = Dimension(metrics.stringWidth(text), metrics.getHeight())
    graphics2d.dispose()
    input = BufferedImage(this.dimension.width, this.dimension.height, BufferedImage.TYPE_INT_ARGB)
    graphics2d = input.createGraphics()
    graphics2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY)
    graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    graphics2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY)
    graphics2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE)
    graphics2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
    graphics2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
    graphics2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
    graphics2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)
    setFontSize()
    graphics2d.setColor(color)
    graphics2d.drawString(text, 0, graphics2d.getFontMetrics().getMaxAscent())
    graphics2d.dispose()
    this.glIdentifier = glGenTextures()
    val baos = java.io.ByteArrayOutputStream()
    javax.imageio.ImageIO.write(input, ResourcesExtension.PNG.extension, baos)
    this.data = org.lwjgl.stb.STBImage.stbi_load_from_memory(
      App.createBufferFromInputStream(java.io.ByteArrayInputStream(baos.toByteArray())),
      BufferUtils.createIntBuffer(1), BufferUtils.createIntBuffer(1),
        BufferUtils.createIntBuffer(1), 4)
  }
}

data class Texture2D(val resource: String): RenderableObject {
  override var glIdentifier: Int = -1
  override var data: java.nio.ByteBuffer? = null
  override var dimension = Dimension()
  override fun load() {
    this.glIdentifier = glGenTextures()
    val widthBuffer = BufferUtils.createIntBuffer(1)
    val heightBuffer = BufferUtils.createIntBuffer(1)
    this.data = org.lwjgl.stb.STBImage.stbi_load_from_memory(
      App.getBufferFromResource(this.resource),
      widthBuffer, heightBuffer, BufferUtils.createIntBuffer(1), 4)
    this.dimension = Dimension(widthBuffer.get(), heightBuffer.get())
  }
}

@org.springframework.stereotype.Component
class Renderer2D(
  @org.springframework.beans.factory.annotation.Autowired
  private val videoSettings: VideoSettings
) {
  companion object {
    private val LOGGER = org.slf4j.LoggerFactory.getLogger(Renderer2D::class.java)
    val SHADER_ERROR_MESSAGE = "Couldn't compile shader"
    val DEFAULT_VERTEX_SHADER = "shaders/basic2d.vs"
    val DEFAULT_FRAGMENT_SHADER = "shaders/basic2d.fs"
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
    val vShader = createShader(DEFAULT_VERTEX_SHADER, GL_VERTEX_SHADER)
    val fShader = createShader(DEFAULT_FRAGMENT_SHADER, GL_FRAGMENT_SHADER)
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
