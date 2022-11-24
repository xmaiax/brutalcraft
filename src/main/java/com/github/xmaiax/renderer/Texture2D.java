package com.github.xmaiax.renderer;

import static org.lwjgl.BufferUtils.createIntBuffer;

import java.nio.IntBuffer;

public class Texture2D extends RenderableObject {

  private String resource;
  public Texture2D(String resource) { this.resource = new String(resource); }

  @Override public void load() {
    final IntBuffer widthBuffer = createIntBuffer(1);
    final IntBuffer heightBuffer = createIntBuffer(1);
    this.update(org.lwjgl.stb.STBImage.stbi_load_from_memory(
      com.github.xmaiax.App.getBufferFromResource(this.resource),
        widthBuffer, heightBuffer, createIntBuffer(1), 4),
          new Dimension(widthBuffer.get(), heightBuffer.get()));
  }

}
