package com.github.xmaiax

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL30C.*
import org.lwjgl.system.MemoryUtil.NULL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value

@org.springframework.stereotype.Component
open class VideoSettings(
  @Value("\${settings.video.title}") val title: String,
  @Value("\${settings.video.width}") val width: Int,
  @Value("\${settings.video.height}") val height: Int,
  @Value("\${settings.video.fullscreen}") val fullscreen: Boolean,
  @Value("\${settings.video.vsync}") val vsync: Boolean,
  @Value("\${settings.video.clear-color}") val clearColor: String
) {
  companion object {
    val OPENGL_VERSION = "3.0"
    var STATIC_GAME_NAME = ""
  }
}

@org.springframework.stereotype.Component
open class AppInfo(
  @Value("\${app.info.release-version}") val releaseVersion: String,
  @Value("\${app.info.kotlin-version}") val kotlinVersion: String,
  @Value("\${app.info.spring-boot-version}") val springBootVersion: String,
  @Value("\${app.info.lwjgl-version}") val lwjglVersion: String,
  @Value("\${app.info.welcome-message}") val welcomeMessage: String
)

interface GameLifecycle {
  fun load()
  fun loop(msSinceLastUpdate: Long, inputKeys: List<InputedAction>): Boolean
  fun render()
  fun shutdown()
}

class Slf4jErrorCallback(): org.lwjgl.glfw.GLFWErrorCallback() {
  companion object {
    private val LOGGER = org.slf4j.LoggerFactory.getLogger(Slf4jErrorCallback::class.java)
    val ERROR_CODES = org.lwjgl.system.APIUtil.apiClassTokens(
        java.util.function.BiPredicate<java.lang.reflect.Field, Int> { _, v ->
      0x100000 < v && v < 0x20000 }, null, org.lwjgl.glfw.GLFW::class.java) }
  override fun invoke(error: Int, description: Long) {
    LOGGER.error("LWJGL ${ERROR_CODES.get(error)} error: ${
      org.lwjgl.glfw.GLFWErrorCallback.getDescription(description)}")
    Thread.currentThread().getStackTrace().forEachIndexed { index, stack ->
      if(index >= 4) LOGGER.error("${stack.toString()}") }
  }
}

enum class ResourcesExtension(private val extension: String,
    private val description: String) {
  PNG("png", "PNG"),
  VERTEX_SHADER("vs", "Vertex Shader"),
  FRAGMENT_SHADER("fs", "Fragment Shader"),
  CONFIGURATION("properties", "Configuration"),
  UNKNOWN("unknown", "Unknown");
  companion object {
    fun fromUrl(url: java.net.URL) = ResourcesExtension.values()
      .filter { it.extension == url.toString().split(".").last() }.first() }
  override fun toString() = this.description
}

@org.springframework.boot.autoconfigure.SpringBootApplication
open class App(
  @Autowired private val videoSettings: VideoSettings,
  @Autowired private val appInfo: AppInfo,
  @Autowired private val game: GameLifecycle,
  @Autowired private val renderer2D: Renderer2D
) : org.springframework.boot.CommandLineRunner {
  companion object {
    private val LOGGER = org.slf4j.LoggerFactory.getLogger(App::class.java)
    @JvmStatic
    fun main(args: Array<String>) {
      initConfig(org.springframework.boot.SpringApplication(App::class.java), *args)
    }
    fun exitWithError(message: String): Exception {
      LOGGER.error(message)
      LOGGER.error("Forcing exit with error...")
      javax.swing.JOptionPane.showConfirmDialog(null,
        "Sorry, an unspecified error occurred.\n${message}${
          if(LOGGER.isDebugEnabled()) "\n\nCheck the logs for more information."
          else ""}\n\nClick OK to terminate the program.", VideoSettings.STATIC_GAME_NAME,
          javax.swing.JOptionPane.DEFAULT_OPTION, javax.swing.JOptionPane.ERROR_MESSAGE)
      return Exception(message)
    }
    fun getBufferFromResource(resource: String): java.nio.ByteBuffer =
      Thread.currentThread().getContextClassLoader()
          .getResource(resource)?.let { url ->
        val type = try { ResourcesExtension.fromUrl(url) }
        catch(e: NoSuchElementException) { ResourcesExtension.UNKNOWN }
        if(type != ResourcesExtension.CONFIGURATION)
          LOGGER.debug("Loading ${type} resource '${resource.split("/").last()}': ${url}")
        val file = java.io.File(url.getFile())
        if (file.isFile()) {
          val fis = java.io.FileInputStream(file)
          val fc = fis.getChannel()
          val buffer = fc.map(java.nio.channels.FileChannel.MapMode.READ_ONLY, 0, fc.size())
          fc.close()
          fis.close()
          return buffer
        } else {
          var output = org.lwjgl.BufferUtils.createByteBuffer(8)
          val source = url.openStream()
          var buffer = ByteArray(8192)
          while (true) {
            val bytes = source.read(buffer, 0, buffer.size)
            if (bytes == -1) break
            if (output.remaining() < bytes) {
              val resizedOutput = org.lwjgl.BufferUtils.createByteBuffer(
                Math.max(output.capacity() * 2,
                output.capacity() - output.remaining() + bytes))
              output.flip()
              resizedOutput.put(output)
              output = resizedOutput
            }
            output.put(buffer, 0, bytes)
          }
          output.flip()
          source.close()
          return output
        }
      } ?: run { throw App.exitWithError("Resource not found: ${resource}") }
  }
  private var window: Long = -1
  private val pressedKeys = mutableListOf<InputedAction>()
  override fun run(vararg args: String) {
    VideoSettings.STATIC_GAME_NAME = "${this.videoSettings.title}"
    LOGGER.info("${this.videoSettings.title}: ${this.appInfo.welcomeMessage}")
    LOGGER.info("Release version: ${this.appInfo.releaseVersion}")
    LOGGER.info("Kotlin version: ${this.appInfo.kotlinVersion}")
    LOGGER.info("Spring-Boot version: ${this.appInfo.springBootVersion}")
    LOGGER.info("LWJGL version: ${this.appInfo.lwjglVersion}")
    Slf4jErrorCallback().set()
    if (!glfwInit()) throw App.exitWithError("Unable to initialize GLFW")
    glfwDefaultWindowHints()
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, Integer.parseInt(VideoSettings.OPENGL_VERSION.split(".")[0]))
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, Integer.parseInt(VideoSettings.OPENGL_VERSION.split(".")[1]))
    glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE)
    glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE)
    glfwWindowHint(GLFW_FOCUS_ON_SHOW, GLFW_TRUE)
    LOGGER.info("Initializing engine...")
    this.window = glfwCreateWindow(
      this.videoSettings.width, this.videoSettings.height, this.videoSettings.title,
      if (this.videoSettings.fullscreen) glfwGetPrimaryMonitor() else NULL, NULL
    )
    if (this.window == NULL) throw App.exitWithError("Failed to create the GLFW window")
    glfwSetInputMode(this.window, GLFW_CURSOR, GLFW_CURSOR_HIDDEN)
    glfwSetKeyCallback(this.window, org.lwjgl.glfw.GLFWKeyCallbackI(
      fun(window: Long, key: Int, _: Int, action: Int, _: Int) {
        this.renderer2D.window = window
        try { this.pressedKeys.add(InputedAction(key, action)) }
        catch(e: NoSuchElementException) {
          LOGGER.warn("Unable to capture key with code '${key}' (${InputEvent.fromCode(action)})") }
      }))
    var keepRunning = true
    val stack = org.lwjgl.system.MemoryStack.stackPush()
    val pWidth = stack.mallocInt(1)
    val pHeight = stack.mallocInt(1)
    glfwGetWindowSize(this.window, pWidth, pHeight)
    glfwGetVideoMode(glfwGetPrimaryMonitor())?.let { vidMode ->
      glfwSetWindowPos(this.window,
        (vidMode.width() - pWidth.get(0)) / 2,
        (vidMode.height() - pHeight.get(0)) / 2) }
    glfwMakeContextCurrent(this.window)
    glfwShowWindow(this.window)
    glfwSwapInterval(if(this.videoSettings.vsync) 1 else 0)
    if(glfwJoystickPresent(GLFW_JOYSTICK_1))
      glfwGetJoystickName(GLFW_JOYSTICK_1)?.let { LOGGER.info("Gamepad found: ${it}") }
    org.lwjgl.opengl.GL.createCapabilities()
    glEnable(GL_BLEND)
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
    val program = this.renderer2D.initialize()
    glUseProgram(0)
    LOGGER.info("Loading assets...")
    this.game.load()
    LOGGER.info("Assets loaded!")
    fun getFloatColorValue(c1: Char, c2: Char) =
      java.lang.Long.parseLong("$c1$c2", 16).toFloat() / 255.0f
    if(this.videoSettings.clearColor.startsWith("#")) glClearColor(
      getFloatColorValue(this.videoSettings.clearColor.get(1), this.videoSettings.clearColor.get(2)),
      getFloatColorValue(this.videoSettings.clearColor.get(3), this.videoSettings.clearColor.get(4)),
      getFloatColorValue(this.videoSettings.clearColor.get(5), this.videoSettings.clearColor.get(6)),
      java.math.BigDecimal.ZERO.toFloat())
    LOGGER.info("Engine is running!!")
    var currentMs = java.util.Calendar.getInstance().getTimeInMillis()
    var previousPressedControllerKeys = listOf<InputedControllerKey>()
    while (keepRunning) {
      glfwGetJoystickButtons(GLFW_JOYSTICK_1)?.let { controllerInput ->
        val currentPressedControllerKeys = InputedControllerKey.values().filter {
          controllerInput.get(it.position) > 0 }
        InputedControllerKey.values().map {
          if(previousPressedControllerKeys.contains(it) &&
              !currentPressedControllerKeys.contains(it))
            it.getInputedAction(InputEvent.RELEASE)
          else if(previousPressedControllerKeys.contains(it) &&
              currentPressedControllerKeys.contains(it))
            it.getInputedAction(InputEvent.REPEAT)
          else if(!previousPressedControllerKeys.contains(it) &&
              currentPressedControllerKeys.contains(it))
            it.getInputedAction(InputEvent.PRESS)
          else it.getInputedAction(InputEvent.INVALID)
        }.forEach { if(it.event != InputEvent.INVALID) this.pressedKeys.add(it) }
        previousPressedControllerKeys = currentPressedControllerKeys
      }
      if(LOGGER.isDebugEnabled())
        this.pressedKeys.forEach { if(it.event != InputEvent.REPEAT)
          LOGGER.debug("Detected key action: ${it}") }
      keepRunning = this.game.loop(java.util.Calendar.getInstance().getTimeInMillis() - currentMs,
        this.pressedKeys) && !glfwWindowShouldClose(this.window)
      this.pressedKeys.clear()
      glfwPollEvents()
      glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
      glViewport(0, 0, this.videoSettings.width, this.videoSettings.height)
      program?.let { glUseProgram(it) }
      this.game.render()
      currentMs = java.util.Calendar.getInstance().getTimeInMillis()
      glUseProgram(0)
      glfwSwapBuffers(this.window)
      if(!keepRunning) {
        LOGGER.info("Shutting down engine...")
        org.lwjgl.glfw.Callbacks.glfwFreeCallbacks(this.window)
        glfwDestroyWindow(this.window)
        glfwTerminate()
        this.game.shutdown()
        glfwSetErrorCallback(null)?.let { it.free() }
      }
    }
    LOGGER.info("Thanks for playing! We hope to see you again!")
  }
}
