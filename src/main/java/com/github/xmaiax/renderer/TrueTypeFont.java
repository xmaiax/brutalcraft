package com.github.xmaiax.renderer;

import static com.github.xmaiax.App.*;
import static com.github.xmaiax.App.ResourcesExtension.PNG;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;
import com.github.xmaiax.error.EngineExpectedException;

public class TrueTypeFont extends RenderableObject {

  private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(TrueTypeFont.class);

  private static final String MESSAGE_INVALID_TTF_RESOURCE = "Invalid TTF resource";
  private static final Float DEFAULT_FONT_SIZE = 40.0f;
  private static final Color DEFAULT_FONT_COLOR = Color.WHITE;

  private final String resource;
  private Font font;

  public TrueTypeFont(final String resource) {
    if(resource == null || resource.isBlank())
      throw exitWithError(MESSAGE_INVALID_TTF_RESOURCE);
    this.resource = new String(resource);
  }

  @Override public void load() {
    try { this.font = Font.createFont(Font.TRUETYPE_FONT,
      getUrlFromResource(this.resource).openStream()); }
    catch (FontFormatException | IOException e) {
      LOGGER.error("Unable to load TTF: {}", this.resource);
      throw exitWithError(MESSAGE_INVALID_TTF_RESOURCE);
    }
  }

  @Override public void bind() { throw new EngineExpectedException(
    "Use the `bakeText` method to create a bindable object!"); }

  private void setFontSize(final Graphics2D graphics2d, final Float fontSize) {
    if(this.font == null) RenderableObject.throwResourceNotLoadedException();
    graphics2d.setFont(this.font.deriveFont(Font.PLAIN, fontSize));
  }

  public static class BakedText extends RenderableObject {
    private String text;
    private Float fontSize;
    private Color color;
    private BakedText(final String text, final float fontSize, final Color color,
        final ByteBuffer data, final Dimension dimension) {
      this.text = text == null ? "" : text;
      this.fontSize = fontSize <= BigInteger.ZERO.floatValue() ? DEFAULT_FONT_SIZE : fontSize;
      this.color = color == null ? DEFAULT_FONT_COLOR : color;
      this.update(data, dimension);
    }
    @Override public void load() { }
  }

  public BakedText bakeText(final String text, final float fontSize, final Color color,
      final BakedText oldBakedText) {
    if(oldBakedText != null) if(oldBakedText.text.equals(text) &&
       oldBakedText.fontSize.equals(fontSize) &&
       oldBakedText.color.equals(color))
      return oldBakedText; else oldBakedText.free();
    try {
      BufferedImage input = new BufferedImage(
        BigInteger.ONE.intValue(), BigInteger.ONE.intValue(), BufferedImage.TYPE_INT_ARGB);
      Graphics2D graphics2d = input.createGraphics();
      this.setFontSize(graphics2d, fontSize);
      final Dimension dimension = new Dimension(
        graphics2d.getFontMetrics().stringWidth(text),
        graphics2d.getFontMetrics().getHeight());
      graphics2d.dispose();
      input = new BufferedImage(dimension.getWidth(),
        dimension.getHeight(), BufferedImage.TYPE_INT_ARGB);
      graphics2d = input.createGraphics();
      this.setFontSize(graphics2d, fontSize);
      graphics2d.setColor(color);
      graphics2d.drawString(text, BigInteger.ZERO.intValue(),
        graphics2d.getFontMetrics().getMaxAscent());
      graphics2d.dispose();
      final ByteArrayOutputStream baos = new ByteArrayOutputStream();
      javax.imageio.ImageIO.write(input, PNG.getExtension(), baos);
      final ByteBuffer data = org.lwjgl.stb.STBImage.stbi_load_from_memory(
        createBufferFromInputStream(new java.io.ByteArrayInputStream(baos.toByteArray())),
          BufferUtils.createIntBuffer(BigInteger.ONE.intValue()),
            BufferUtils.createIntBuffer(BigInteger.ONE.intValue()),
              BufferUtils.createIntBuffer(BigInteger.ONE.intValue()),
                BigInteger.TWO.intValue() + BigInteger.TWO.intValue());
      return new BakedText(text, fontSize, color, data, dimension);
    }
    catch(final IOException ioe) {
      final String errorMessage = String.format(
        "Failed to bake text with font '%s': %s", this.resource, ioe.getMessage());
      throw exitWithError(errorMessage);
    }
  }

  public BakedText bakeText(final String text, final Float fontSize, final BakedText oldBakedText) {
    return this.bakeText(text, fontSize, DEFAULT_FONT_COLOR, oldBakedText);
  }

  public BakedText bakeText(final String text, final Color color, final BakedText oldBakedText) {
    return this.bakeText(text, DEFAULT_FONT_SIZE, color, oldBakedText);
  }

  public BakedText bakeText(final String text, final BakedText oldBakedText) {
    return this.bakeText(text, DEFAULT_FONT_SIZE, DEFAULT_FONT_COLOR, oldBakedText);
  }

}
