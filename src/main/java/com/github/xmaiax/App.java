package com.github.xmaiax;

import static com.github.xmaiax.config.InitConfigs.initConfig;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.system.MemoryStack;
import com.github.xmaiax.config.AppInfo;
import com.github.xmaiax.config.VideoSettings;
import com.github.xmaiax.error.EngineExpectedException;
import com.github.xmaiax.input.InputEvent;
import com.github.xmaiax.input.InputedAction;
import com.github.xmaiax.input.InputedControllerKey;
import com.github.xmaiax.renderer.GameLifecycle;
import com.github.xmaiax.renderer.Renderer2D;
import com.github.xmaiax.renderer.Texture2D;

@org.springframework.boot.autoconfigure.SpringBootApplication
public class App implements org.springframework.boot.CommandLineRunner {

  private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(App.class);

  private VideoSettings videoSettings;
  private AppInfo appInfo;
  private GameLifecycle game;
  private Renderer2D renderer2D;

  @org.springframework.beans.factory.annotation.Autowired public App(
      VideoSettings videoSettings, AppInfo appInfo,
      GameLifecycle game, Renderer2D renderer2D) {
    this.videoSettings = videoSettings;
    this.appInfo = appInfo;
    this.game = game;
    this.renderer2D = renderer2D;
  }

  public static void main(String[] args) throws Exception {
    initConfig(new org.springframework.boot.SpringApplication(App.class), args);
  }

  private Long window = NULL;
  private List<InputedAction> pressedKeys = new ArrayList<>();

  private Float getFloatColorValue(String input, Integer position) {
    return Float.parseFloat(String.valueOf(Integer.parseInt(input.substring(
      position, position + 2), 16))) / 255.0f;
  }

  @Override @SuppressWarnings({ "resource", })
  public void run(String... args) throws Exception {
    VideoSettings.STATIC_GAME_NAME = this.videoSettings.getTitle();
    LOGGER.info("{}: {}", this.videoSettings.getTitle(), this.appInfo.getWelcomeMessage());
    LOGGER.info("Release version: {}", this.appInfo.getReleaseVersion());
    LOGGER.info("Spring-Boot version: {}", this.appInfo.getSpringBootVersion());
    LOGGER.info("LWJGL version: {}", this.appInfo.getLwjglVersion());
    new com.github.xmaiax.error.Slf4jErrorCallback().set();
    if(!glfwInit()) throw exitWithError("Unable to initialize GLFW");
    glfwDefaultWindowHints();
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, Integer.parseInt(VideoSettings.OPENGL_VERSION.split("[.]")[0]));
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, Integer.parseInt(VideoSettings.OPENGL_VERSION.split("[.]")[1]));
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
    glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
    glfwWindowHint(GLFW_FOCUS_ON_SHOW, GLFW_TRUE);
    LOGGER.info("Initializing engine...");
    this.window = glfwCreateWindow(this.videoSettings.getWidth(), this.videoSettings.getHeight(), this.videoSettings.getTitle(),
      this.videoSettings.getFullscreen() ? glfwGetPrimaryMonitor() : NULL, NULL);
    if(this.window == NULL) throw exitWithError("Failed to create the GLFW window");
    if(this.appInfo.getHideMouseCursor()) glfwSetInputMode(this.window, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
    glfwSetKeyCallback(this.window, (window, key, scancode, action, mods) -> {
      final InputedAction inputedAction = new InputedAction(key, action);
      if(inputedAction.getKey() != null) this.pressedKeys.add(inputedAction);
    });
    boolean keepRunning = true;
    final MemoryStack stack = MemoryStack.stackPush();
    final IntBuffer pWidth = stack.mallocInt(1);
    final IntBuffer pHeight = stack.mallocInt(1);
    glfwGetWindowSize(this.window, pWidth, pHeight);
    final org.lwjgl.glfw.GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
    glfwSetWindowPos(this.window,
      (vidMode.width() - pWidth.get(0)) / 2,
      (vidMode.height() - pHeight.get(0)) / 2);
    glfwMakeContextCurrent(this.window);
    glfwShowWindow(this.window);
    glfwSwapInterval(this.videoSettings.getVsync() ? 1 : 0);
    if(glfwJoystickPresent(GLFW_JOYSTICK_1)) {
      final String gamepad = glfwGetJoystickName(GLFW_JOYSTICK_1);
      if(gamepad != null && !gamepad.isBlank()) LOGGER.info("Gamepad found: {}", gamepad);
    }
    org.lwjgl.opengl.GL.createCapabilities();
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    final int program = this.renderer2D.initialize();
    glUseProgram(0);
    LOGGER.info("Loading assets...");
    final Texture2D appIcon = new Texture2D(this.appInfo.getWindowIconLocation());
    appIcon.load();
    final GLFWImage appIconImage = org.lwjgl.glfw.GLFWImage.malloc();
    appIconImage.set(appIcon.getDimension().getWidth(), appIcon.getDimension().getHeight(), appIcon.getData());
    final GLFWImage.Buffer appIconBuffer = org.lwjgl.glfw.GLFWImage.malloc(1);
    appIconBuffer.put(0, appIconImage);
    glfwSetWindowIcon(this.window, appIconBuffer);
    appIconBuffer.free();
    appIconImage.free();
    appIcon.free();
    this.game.load();
    LOGGER.info("Assets loaded!");
    if(this.videoSettings.getClearColor() != null && this.videoSettings.getClearColor().startsWith("#"))
      glClearColor(
        getFloatColorValue(this.videoSettings.getClearColor(), Integer.valueOf(1)),
        getFloatColorValue(this.videoSettings.getClearColor(), Integer.valueOf(3)),
        getFloatColorValue(this.videoSettings.getClearColor(), Integer.valueOf(5)),
        java.math.BigDecimal.ZERO.floatValue());
    LOGGER.info("Engine is running.");
    final List<InputedControllerKey> previousPressedControllerKeys = new ArrayList<>();
    long currentMs = System.currentTimeMillis();
    while(keepRunning) {
      try {
        final ByteBuffer controllerInput = glfwGetJoystickButtons(GLFW_JOYSTICK_1);
        final List<InputedControllerKey> currentPressedControllerKeys = controllerInput == null ? java.util.Collections.emptyList() :
          Arrays.asList(InputedControllerKey.values())
            .stream().filter(ick -> controllerInput.get(ick.getPosition()) > 0)
            .collect(java.util.stream.Collectors.toList());
        Arrays.asList(InputedControllerKey.values()).stream().map(ick -> {
          final boolean isPreviouslyPressed = previousPressedControllerKeys.contains(ick);
          final boolean isCurrentlyPressed = currentPressedControllerKeys.contains(ick);
               if(isPreviouslyPressed && !isCurrentlyPressed) return ick.getInputedAction(InputEvent.RELEASE);
          else if(isPreviouslyPressed && isCurrentlyPressed)  return ick.getInputedAction(InputEvent.REPEAT);
          else if(!isPreviouslyPressed && isCurrentlyPressed)  return ick.getInputedAction(InputEvent.PRESS);
          else return ick.getInputedAction(InputEvent.INVALID);
        }).filter(ia -> !InputEvent.INVALID.equals(ia.getEvent())).forEach(ia -> this.pressedKeys.add(ia));
        previousPressedControllerKeys.clear();
        previousPressedControllerKeys.addAll(currentPressedControllerKeys);
        if(LOGGER.isDebugEnabled()) this.pressedKeys.stream()
          .filter(pk -> !InputEvent.REPEAT.equals(pk.getEvent()) && pk.getKey() != null)
          .forEach(pk -> LOGGER.debug("Detected key action: {}", pk));
        keepRunning = this.game.loop(System.currentTimeMillis() - currentMs,
          this.pressedKeys) && !glfwWindowShouldClose(this.window);
        currentMs = System.currentTimeMillis();
        this.pressedKeys.clear();
        glfwPollEvents();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, this.videoSettings.getWidth(), this.videoSettings.getHeight());
        if(program != Integer.MIN_VALUE) glUseProgram(program);
        this.game.render();
        glUseProgram(0);
        glfwSwapBuffers(this.window);
        if(!keepRunning) {
          this.game.shutdown();
          LOGGER.info("Shutting down engine...");
          org.lwjgl.glfw.Callbacks.glfwFreeCallbacks(this.window);
          glfwDestroyWindow(this.window);
          glfwTerminate();
          final org.lwjgl.glfw.GLFWErrorCallback eCallback = glfwSetErrorCallback(null);
          if(eCallback != null) eCallback.free();
        }
      }
      catch(Exception e) {
        throw exitWithError(e.getMessage(), e instanceof EngineExpectedException);
      }
    }
    LOGGER.info("Thanks for playing! We hope to see you again!");
  }

  public static enum ResourcesExtension {

    PNG("png", "PNG"),
    TTF("ttf", "TrueType Font"),
    VERTEX_SHADER("vs", "Vertex Shader"),
    FRAGMENT_SHADER("fs", "Fragment Shader"),
    CONFIGURATION("properties", "Configuration"),
    UNKNOWN("unknown", "Unknown"),

    ;

    private String extension;
    public String getExtension() { return this.extension; }

    private String description;
    @Override public String toString() { return this.description; }

    private ResourcesExtension(String extension, String description) {
      this.extension = extension;
      this.description = description;
    }

    public static final ResourcesExtension fromFileName(String fileName) {
      if(fileName == null || fileName.isBlank() || !fileName.contains(".")) return UNKNOWN;
      return Arrays.asList(ResourcesExtension.values()).stream().filter(re -> re.extension.equalsIgnoreCase(
        fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()))).findFirst().orElse(UNKNOWN);
    }

  }

  public static IllegalStateException exitWithError(String message, boolean unexpected) {
    LOGGER.error(message);
    LOGGER.error("Forcing exit with error...");
    javax.swing.JOptionPane.showConfirmDialog(null, String.format(
      "Sorry, an%s error occurred.\n%s%s\n\nClick OK to terminate the program.",
      unexpected ? " unexpected" : "", message, LOGGER.isDebugEnabled() ? "\n\nCheck the logs for more information." : ""),
      VideoSettings.STATIC_GAME_NAME, javax.swing.JOptionPane.DEFAULT_OPTION, javax.swing.JOptionPane.ERROR_MESSAGE);
    return unexpected ? new IllegalStateException(message) : new EngineExpectedException(message);
  }

  public static IllegalStateException exitWithError(String message) {
    return exitWithError(message, false);
  }

  public static URL getUrlFromResource(String resource) {
    if(resource == null || resource.isBlank()) throw exitWithError("Invalid resource given!");
    final URL url = Thread.currentThread().getContextClassLoader().getResource(resource.trim());
    if(url == null) throw exitWithError("Resource not found: ".concat(resource));
    final ResourcesExtension type = ResourcesExtension.fromFileName(resource);
    if(!ResourcesExtension.CONFIGURATION.equals(type))
      LOGGER.debug("Loading {} resource '{}': {}", type, resource.contains("/") ? resource
        .substring(resource.lastIndexOf("/") + 1, resource.length()) : resource, url);
    return url;
  }

  public static ByteBuffer createBufferFromInputStream(java.io.InputStream inputStream) throws IOException {
    ByteBuffer output = org.lwjgl.BufferUtils.createByteBuffer(8);
    byte[] buffer = new byte[8192];
    while(true) {
      final int bytes = inputStream.read(buffer, 0, buffer.length);
      if(bytes == -1) break;
      if(output.remaining() < bytes) {
        final ByteBuffer resizedOutput = org.lwjgl.BufferUtils.createByteBuffer(
          Math.max(output.capacity() * 2,
          output.capacity() - output.remaining() + bytes));
        output.flip();
        resizedOutput.put(output);
        output = resizedOutput;
      }
      output.put(buffer, 0, bytes);
    }
    output.flip();
    inputStream.close();
    return output;
  }

  public static ByteBuffer getBufferFromResource(String resource) {
    final URL url = getUrlFromResource(resource);
    final File file = new File(url.getFile());
    try {
      if(file.isFile()) {
        final FileInputStream fis = new FileInputStream(file);
        final FileChannel fc = fis.getChannel();
        final ByteBuffer buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
        fc.close();
        fis.close();
        return buffer;
      }
      return createBufferFromInputStream(url.openStream());
    }
    catch(IOException e) {
      throw exitWithError(e.getMessage());
    }
  }

}
