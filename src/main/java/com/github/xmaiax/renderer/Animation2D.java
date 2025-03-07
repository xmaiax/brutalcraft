package com.github.xmaiax.renderer;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.github.xmaiax.App;

public class Animation2D extends RenderableObject {

  private static final String JAR_FILE_ENTRY_PREFFIX_URL = "BOOT-INF/classes/";

  private final List<Texture2D> textures2D = new ArrayList<>();
  public List<Texture2D> getTextures2D() { return this.textures2D;  }

  private final String resourceFolder;
  public Animation2D(final String resourceFolder) { this.resourceFolder = resourceFolder; }

  private void loadAllTexturesWithFileNames(final List<String> files) {
    this.textures2D.addAll(files.stream()
      .map(file -> new Texture2D(this.resourceFolder.concat(file))).collect(Collectors.toList()));
    this.textures2D.forEach(Texture2D::load);
  }

  @Override public void load() {
    if(this.resourceFolder == null || this.resourceFolder.isBlank())
      throw App.exitWithError("Invalid resource folder!");
    try {
      final java.net.URL url = Thread.currentThread().getContextClassLoader()
        .getResource(this.resourceFolder);
      final File dir = new File(url.getFile());
      if(dir.isDirectory()) this.loadAllTexturesWithFileNames(Arrays.asList(dir.listFiles()).stream()
        .map(file -> file.getName()).collect(Collectors.toList()));
      else {
        final ZipInputStream zipIS = new ZipInputStream(App.class.getProtectionDomain()
          .getCodeSource().getLocation().openStream());
        final List<String> files = new ArrayList<>();
        while(Boolean.TRUE) {
          final ZipEntry zipEntry = zipIS.getNextEntry();
          if(zipEntry != null) files.add(zipEntry.getName()); else break;
        }
        this.loadAllTexturesWithFileNames(files.stream()
          .map(file -> file.startsWith(JAR_FILE_ENTRY_PREFFIX_URL.concat(this.resourceFolder)) &&
            !file.endsWith("/") ? file.replace(JAR_FILE_ENTRY_PREFFIX_URL.concat(this.resourceFolder), "") :
              null).filter(f -> f != null && !f.isBlank()).collect(Collectors.toList()));
      }
      final Texture2D first = this.textures2D.stream().findFirst().get();
      this.update(first.getData(), first.getDimension());
    }
    catch(final IOException ioe) {
      throw App.exitWithError(String.format("Error while loading animation directory '%s': %s",
        this.resourceFolder, ioe.getMessage()));
    }
  }

  public static class Animation2DIndex {

    private long msUntilNextFrame;
    public long getMsUntilNextFrame() { return this.msUntilNextFrame; }
    public Animation2DIndex setMsUntilNextFrame(final long msUntilNextFrame) {
      this.msUntilNextFrame = msUntilNextFrame; return this; }

    public Animation2DIndex(long msUntilNextFrame) {
      this.reset().msUntilNextFrame = msUntilNextFrame; }

    private AtomicLong msCounter;
    public long getMsCounter() { return this.msCounter.longValue(); }
    public Animation2DIndex setMsCounter(final Long msCounter) {
      this.msCounter = new AtomicLong(msCounter); return this; }
    public long incrementMsCounter(final Long msSinceLastUpdate) {
      return this.msCounter.addAndGet(msSinceLastUpdate); }

    private int index;
    public int getIndex() { return this.index; }
    public Animation2DIndex setIndex(final int index) { this.index = index; return this; }
    public Animation2DIndex incrementIndex() {
      this.index += BigInteger.ONE.intValue(); return this; }

    public Animation2DIndex reset() {
      this.setMsCounter(BigInteger.ZERO.longValue());
      this.index = BigInteger.ZERO.intValue();
      return this;
    }

  }

  public void update(final long msSinceLastUpdate, final Animation2DIndex animationIndex) {
    if(animationIndex.incrementMsCounter(msSinceLastUpdate) >= animationIndex.getMsUntilNextFrame())
      animationIndex.setMsCounter(animationIndex.getMsCounter()
        -animationIndex.getMsUntilNextFrame()).incrementIndex();
    if(animationIndex.getIndex() >= this.textures2D.size())
      animationIndex.setIndex(BigInteger.ZERO.intValue());
    final Texture2D t2d = this.textures2D.get(animationIndex.getIndex());
    this.update(t2d.getData(), t2d.getDimension());
  }

  @Override
  public void free() {
    this.textures2D.stream().forEach(Texture2D::free);
    this.textures2D.clear();
  }

}
