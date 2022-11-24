package com.github.xmaiax.renderer;

import static org.lwjgl.opengl.GL30C.*;

import com.github.xmaiax.App;
import com.github.xmaiax.config.VideoSettings;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;

@org.springframework.stereotype.Component public class Renderer2D {

  private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(Renderer2D.class);

  private static final String SHADER_ERROR_MESSAGE = "Couldn't compile shader";
  private static final String  DEFAULT_VERTEX_SHADER = "shaders/basic2d.vs";
  private static final String  DEFAULT_FRAGMENT_SHADER = "shaders/basic2d.fs";

  private VideoSettings videoSettings;
  public VideoSettings getVideoSettings() { return this.videoSettings; }

  @org.springframework.beans.factory.annotation.Autowired
  public Renderer2D(VideoSettings videoSettings) {
    this.videoSettings = videoSettings;
  }

  private int program = Integer.MIN_VALUE;
  private int  programInputPosition;
  private int  programInputTextureCoordinates;
  private int  programInputAlpha;

  private int createShader(String resource, int type) {
    final ByteBuffer source = App.getBufferFromResource(resource);
    final int shader = glCreateShader(type);
    final PointerBuffer strings = BufferUtils.createPointerBuffer(1);
    strings.put(0, source);
    final IntBuffer lengths = BufferUtils.createIntBuffer(1);
    lengths.put(0, source.remaining());
    glShaderSource(shader, strings, lengths);
    glCompileShader(shader);
    final int compiled = glGetShaderi(shader, GL_COMPILE_STATUS);
    final String shaderLog = glGetShaderInfoLog(shader);
    LOGGER.info("Shader '{}' log: {}", resource, shaderLog == null || shaderLog.isBlank() ?
      "No errors or warnings found." : shaderLog);
    if(compiled == 0) throw App.exitWithError(SHADER_ERROR_MESSAGE);
    return shader;
  }

  public int initialize() {
    this.program = glCreateProgram();
    final int vShader = this.createShader(DEFAULT_VERTEX_SHADER, GL_VERTEX_SHADER);
    final int fShader = this.createShader(DEFAULT_FRAGMENT_SHADER, GL_FRAGMENT_SHADER);
    glAttachShader(this.program, vShader);
    glAttachShader(this.program, fShader);
    glLinkProgram(this.program);
    final int linked = glGetProgrami(this.program, GL_LINK_STATUS);
    final String programInfoLog = glGetProgramInfoLog(this.program);
    if(programInfoLog != null && !programInfoLog.isBlank()) LOGGER.warn(programInfoLog);
    if(linked == 0) throw App.exitWithError("Couldn't link program");
    glUseProgram(this.program);
    final int texLocation = glGetUniformLocation(this.program, "_texture");
    glUniform1i(texLocation, 0);
    this.programInputPosition = glGetAttribLocation(this.program, "position");
    this.programInputTextureCoordinates = glGetAttribLocation(this.program, "textureCoordinates");
    this.programInputAlpha = glGetAttribLocation(this.program, "alpha");
    return this.program;
  }

  private void setFloatVariableToShader(float[] value, int programAttributeLocation) {
    glBindBuffer(GL_ARRAY_BUFFER, glGenBuffers());
    glBufferData(GL_ARRAY_BUFFER, BufferUtils.createFloatBuffer(value.length).put(value).flip(), GL_STATIC_DRAW);
    glVertexAttribPointer(programAttributeLocation, 2, GL_FLOAT, true, 0, 0);
    glEnableVertexAttribArray(programAttributeLocation);
  }

  private static final float[] INPUT_TEXT_COORD_VERTEX = new float[] {
    0.0f, 1.0f, 1.0f,
    1.0f, 1.0f, 0.0f,
    1.0f, 0.0f, 0.0f,
    0.0f, 0.0f, 1.0f,
  };

  public void render2DQuad(Position position, Dimension dimension, boolean horizontalInvert, Double alpha) {
    glBindVertexArray(glGenVertexArrays());
    final Float sizeX = (dimension.getWidth() * 2.0f) / this.videoSettings.getWidth();
    final Float sizeY = (dimension.getHeight() * 2.0f) / this.videoSettings.getHeight();
    final Float startingPositionX = (2.0f * position.getX() - this.videoSettings.getWidth()) / this.videoSettings.getWidth();
    final Float startingPositionY = (2.0f * position.getY() - this.videoSettings.getHeight()) / this.videoSettings.getHeight() + sizeY;
    this.setFloatVariableToShader(horizontalInvert ? new float[] {
      startingPositionX + sizeX, -startingPositionY, startingPositionX,
      -startingPositionY, startingPositionX, -startingPositionY + sizeY,
      startingPositionX, -startingPositionY + sizeY, startingPositionX + sizeX,
      -startingPositionY + sizeY, startingPositionX + sizeX, -startingPositionY,
    } : new float[] {
      startingPositionX, -startingPositionY, startingPositionX + sizeX,
      -startingPositionY, startingPositionX + sizeX, -startingPositionY + sizeY,
      startingPositionX + sizeX, -startingPositionY + sizeY, startingPositionX,
      -startingPositionY + sizeY, startingPositionX, -startingPositionY
    }, this.programInputPosition);
    this.setFloatVariableToShader(INPUT_TEXT_COORD_VERTEX, this.programInputTextureCoordinates);
    this.setFloatVariableToShader(new float[] {
      alpha.floatValue(), alpha.floatValue(), alpha.floatValue(),
      alpha.floatValue(), alpha.floatValue(), alpha.floatValue(),
      alpha.floatValue(), alpha.floatValue(), alpha.floatValue(),
      alpha.floatValue(), alpha.floatValue(), alpha.floatValue(),
    }, this.programInputAlpha);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glDrawArrays(GL_TRIANGLES, 0, 6);
    glBindVertexArray(0);
  }

}
