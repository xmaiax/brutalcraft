package com.github.xmaiax.renderer;

import static org.lwjgl.BufferUtils.createIntBuffer;

import java.math.BigInteger;

public class Texture2D extends RenderableObject {
  private final String resource;
  public Texture2D(final String resource) { this.resource = new String(resource); }
  @Override public void load() {
    final java.nio.IntBuffer widthBuffer = createIntBuffer(BigInteger.ONE.intValue());
    final java.nio.IntBuffer heightBuffer = createIntBuffer(BigInteger.ONE.intValue());
    this.update(org.lwjgl.stb.STBImage.stbi_load_from_memory(
      com.github.xmaiax.App.getBufferFromResource(this.resource),
        widthBuffer, heightBuffer, createIntBuffer(BigInteger.ONE.intValue()),
          BigInteger.TWO.intValue() + BigInteger.TWO.intValue()),
            new Dimension(widthBuffer.get(), heightBuffer.get()));
  }
}
