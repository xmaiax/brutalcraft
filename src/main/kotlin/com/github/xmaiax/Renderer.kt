package com.github.xmaiax

import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.nio.ByteBuffer
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL30C.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

data class Position(var x: Int = 0, var y: Int = 0)
data class Dimension(val width: Int = -1, val height: Int = -1) {
  fun isValid() = this.width > 0 && this.height > 0
  fun scaleUp(scale: Double) = Dimension(
    (this.width.toDouble() * scale).toInt(),
    (this.height.toDouble() * scale).toInt()) }

abstract class RenderableObject(
  private var glIdentifier: Int = -1,
  private var data: java.nio.ByteBuffer? = null,
  private var dimension: Dimension = Dimension()) {
  companion object {
    val LOAD_RESOURCE_BEFORE_USING = "Please load the resource before using it."
    fun throwResourceNotLoadedException(): Nothing = throw App.exitWithError(
      RenderableObject.LOAD_RESOURCE_BEFORE_USING)
  }
  fun getGLid() = this.glIdentifier
  fun getData() = this.data
  fun getDimension(scale: Double? = null) = if(this.dimension.isValid())
      scale?.let { this.dimension.scaleUp(it) } ?: run { this.dimension }
    else RenderableObject.throwResourceNotLoadedException()
  abstract fun load(): Unit
  open fun free(): Unit = this.data?.let {
    org.lwjgl.stb.STBImage.stbi_image_free(it) } ?: run { Unit }
  fun bind() = this.data?.let {
    glBindTexture(GL_TEXTURE_2D, this.glIdentifier)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
    glTexImage2D(
      GL_TEXTURE_2D, 0, GL_RGBA, this.dimension.width, this.dimension.height,
      0, GL_RGBA, GL_UNSIGNED_BYTE, it)
  } ?: run { RenderableObject.throwResourceNotLoadedException() }
  fun update(data: java.nio.ByteBuffer?, dimension: Dimension): RenderableObject {
    if(this.glIdentifier == -1) this.glIdentifier = glGenTextures()
    this.data = data
    this.dimension = dimension
    return this }
  fun copyAsRenderableObject() = object: RenderableObject(
    glIdentifier, data, dimension) { override fun load() = Unit }
}

data class TrueTypeFont(val resource: String): RenderableObject() {
  private var font: Font? = null
  override fun load() {
    this.font = Font.createFont(Font.TRUETYPE_FONT,
      App.getUrlFromResource(this.resource).openStream()) }
  fun bakeText(text: String, fontSize: Float = 40.0f,
      color: Color = Color.WHITE): RenderableObject {
    var input = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
    var graphics2d = input.createGraphics()
    fun setFontSize() = this.font?.let {
      graphics2d.setFont(it.deriveFont(Font.PLAIN, fontSize)) } ?: run {
        RenderableObject.throwResourceNotLoadedException() }
    setFontSize()
    var metrics = graphics2d.getFontMetrics()
    val dimension = Dimension(metrics.stringWidth(text), metrics.getHeight())
    graphics2d.dispose()
    input = BufferedImage(dimension.width, dimension.height, BufferedImage.TYPE_INT_ARGB)
    graphics2d = input.createGraphics()
    setFontSize()
    graphics2d.setColor(color)
    graphics2d.drawString(text, 0, graphics2d.getFontMetrics().getMaxAscent())
    graphics2d.dispose()
    val baos = java.io.ByteArrayOutputStream()
    javax.imageio.ImageIO.write(input, ResourcesExtension.PNG.extension, baos)
    return this.update(org.lwjgl.stb.STBImage.stbi_load_from_memory(
      App.createBufferFromInputStream(java.io.ByteArrayInputStream(baos.toByteArray())),
      BufferUtils.createIntBuffer(1), BufferUtils.createIntBuffer(1),
        BufferUtils.createIntBuffer(1), 4), dimension).copyAsRenderableObject() } }

data class Texture2D(val resource: String): RenderableObject() {
  override fun load() {
    val widthBuffer = BufferUtils.createIntBuffer(1)
    val heightBuffer = BufferUtils.createIntBuffer(1)
    this.update(org.lwjgl.stb.STBImage.stbi_load_from_memory(
      App.getBufferFromResource(this.resource),
      widthBuffer, heightBuffer, BufferUtils.createIntBuffer(1), 4),
        Dimension(widthBuffer.get(), heightBuffer.get())) } }

data class Animation2D(val resourceFolder: String): RenderableObject() {
  companion object { val JAR_FILE_ENTRY_PREFFIX_URL = "BOOT-INF/classes/" }
  private var textures2D: List<Texture2D> = listOf()
  override fun load(): Unit = Thread.currentThread().getContextClassLoader()
      .getResource(this.resourceFolder)?.let { url ->
    fun loadAllTexturesWithFileNames(files: List<String>) {
      this.textures2D = files.map { Texture2D("${this.resourceFolder}${it}") }
      this.textures2D.forEach { it.load() }}
    val dir = java.io.File(url.getFile())
    if(dir.isDirectory()) loadAllTexturesWithFileNames(dir.listFiles().map { it.getName() })
    else {
      val zipIS = java.util.zip.ZipInputStream(App::class.java.getProtectionDomain()
        .getCodeSource().getLocation().openStream())
      val files = mutableListOf<String>()
      while(true)
        if(zipIS.getNextEntry()?.let { !files.add(it.getName())
          } ?: run { true }) break
      loadAllTexturesWithFileNames(files.filter {
        it.startsWith(JAR_FILE_ENTRY_PREFFIX_URL + this.resourceFolder) &&
          !it.endsWith("/") }.map { it.substring(JAR_FILE_ENTRY_PREFFIX_URL.length).split("/").last() })
    }
    this.textures2D.first().let { this.update(it.getData(), it.getDimension()) }
    return Unit
  } ?: run { throw App.exitWithError("Animation resource directory '${
    this.resourceFolder}' doesn't exists or is empty!") }
  fun update(msSinceLastUpdate: Long, animationIndex: Animation2DIndex) {
    animationIndex.msCounter += msSinceLastUpdate
    if(animationIndex.msCounter >= animationIndex.msUntilNextFrame) {
      animationIndex.msCounter -= animationIndex.msUntilNextFrame
      animationIndex.index += 1
    }
    if(animationIndex.index >= this.textures2D.size) animationIndex.index = 0
    this.textures2D.get(animationIndex.index).let { text2D ->
      this.update(text2D.getData(), text2D.getDimension()) }}
  override fun free() { this.textures2D.forEach { it.free() }
    this.textures2D = emptyList() } }

data class Animation2DIndex(val msUntilNextFrame: Int,
    var msCounter: Long = 0, var index: Int = 0) {
  fun reset() { this.msCounter = 0
    this.index = 0 } }

@Component
class Renderer2D(@Autowired val videoSettings: VideoSettings) {
  companion object {
    private val LOGGER = org.slf4j.LoggerFactory.getLogger(Renderer2D::class.java)
    val SHADER_ERROR_MESSAGE = "Couldn't compile shader"
    val DEFAULT_VERTEX_SHADER = "shaders/basic2d.vs"
    val DEFAULT_FRAGMENT_SHADER = "shaders/basic2d.fs" }
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
  fun render2DQuad(position: Position, dimension: Dimension,
      horizontalInvert: Boolean = false, alpha: Double = 1.0) {
    glBindVertexArray(glGenVertexArrays())
    val sizeX = (dimension.width.toDouble() * 2.0 /
      this.videoSettings.width.toDouble()).toFloat()
    val sizeY = (dimension.height.toDouble() * 2.0 /
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

@Component
class FPSCounter(
    @Autowired val renderer: Renderer2D,
    @Autowired val config: FPSCounterConfig): RenderableObject() {
  data class FPSCounterConfig(val size: Float,
    val color: Color, val topRightMargin: Int)
  private var lastTime = System.currentTimeMillis()
  private var currentText: String = "-"
  private var frameCounter: Int = 0
  private var msCounter: Long = 0
  override fun load() = Unit
  lateinit var font: TrueTypeFont
  fun load(font: TrueTypeFont) { this.font = font }
  private fun update(): Boolean {
    this.msCounter += System.currentTimeMillis() - this.lastTime
    this.lastTime = System.currentTimeMillis()
    this.frameCounter++
    if(this.msCounter >= 1000L) {
      val newText = "FPS: ${this.frameCounter}"
      this.frameCounter = 0
      this.msCounter = 0L
      if(!this.currentText.equals(newText)) {
        this.currentText = newText
        return true }}
    return false }
  fun render() {
    if(this.update()) {
      val bakedText = this.font.bakeText(this.currentText, this.config.size, this.config.color)
      this.update(bakedText.getData(), bakedText.getDimension()) }
    this.getData()?.let { this.bind()
      renderer.render2DQuad(Position(
        renderer.videoSettings.width - this.getDimension().width - this.config.topRightMargin,
          this.config.topRightMargin), this.getDimension())}}}
