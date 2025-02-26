package com.github.xmaiax.error;

public class Slf4jErrorCallback extends org.lwjgl.glfw.GLFWErrorCallback {
  private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(Slf4jErrorCallback.class);
  public static final int LWJGL_STACK_HEIGHT = 4;
  public static final java.util.Map<Integer, String> ERROR_CODES = org.lwjgl.system.APIUtil
    .apiClassTokens((_null, v) -> 0x100000<v && v<0x20000, null, org.lwjgl.glfw.GLFW.class);
  @Override public void invoke(final int error, final long description) {
    LOGGER.error("LWJGL {} error: {}", ERROR_CODES.get(error), org.lwjgl.glfw.GLFWErrorCallback.getDescription(description));
    java.util.stream.IntStream.range(0, Thread.currentThread().getStackTrace().length).forEach(i -> {
      if(i >= LWJGL_STACK_HEIGHT) LOGGER.error(Thread.currentThread().getStackTrace()[i].toString());
    });
  }
}
