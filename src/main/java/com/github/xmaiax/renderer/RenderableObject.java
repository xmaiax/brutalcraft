package com.github.xmaiax.renderer;

import static org.lwjgl.opengl.GL30C.*;

public abstract class RenderableObject {

  public static final String LOAD_RESOURCE_BEFORE_USING = "Please load the resource before using it.";

  private int glIdentifier = Integer.MIN_VALUE;
  public int getGLidentifier() { return this.glIdentifier; }

  private java.nio.ByteBuffer data = null;
  public java.nio.ByteBuffer getData() { return this.data; }

  public static void throwResourceNotLoadedException() {
    throw com.github.xmaiax.App.exitWithError(LOAD_RESOURCE_BEFORE_USING);
  }

  private Dimension dimension = new Dimension();
  public Dimension getDimension() {
    if(this.dimension == null || !this.dimension.isValid())
      throwResourceNotLoadedException();
    return this.dimension;
  }
  protected void setDimension(Dimension dimension) { this.dimension = dimension; }

  public Dimension getDimension(Double scale) {
    return this.getDimension().scaleUp(scale);
  }

  abstract public void load();

  public void free() {
    if(this.data != null) {
      org.lwjgl.stb.STBImage.stbi_image_free(this.data);
    }
  }

  public void bind() {
    if(this.data != null && this.dimension.isValid()) {
      glBindTexture(GL_TEXTURE_2D, this.glIdentifier);
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
      glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA,
        this.dimension.getWidth(), this.dimension.getHeight(), 0,
          GL_RGBA, GL_UNSIGNED_BYTE, this.data);
    }
    else throwResourceNotLoadedException();
  }

  protected RenderableObject update(java.nio.ByteBuffer data, Dimension dimension) {
    this.glIdentifier = this.glIdentifier == Integer.MIN_VALUE ? glGenTextures() : this.glIdentifier;
    this.data = data;
    this.dimension = dimension;
    return this;
  }

}
