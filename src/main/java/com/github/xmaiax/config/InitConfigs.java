package com.github.xmaiax.config;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class InitConfigs {

  private static final String SET_LOOK_AND_FEEL = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
  private static final String RESOLUTION_SEPARATOR = "x";
  private static final String PROPERTIES_SEPARATOR = "=";
  private static final Integer JAVA_UI_WIDTH = 250;
  private static final Integer JAVA_UI_HEIGHT = 250;
  private static final Boolean JAVA_UI_RESIZABLE = Boolean.FALSE;
  private static final Boolean JAVA_UI_ALWAYS_ON_TOP = Boolean.TRUE;
  private static final String JAVA_UI_RESOLUTION_LABEL = "Resolution";
  private static final String JAVA_UI_FULLSCREEN_LABEL = "Fullscreen";
  private static final String JAVA_UI_VSYNC_LABEL = "V-Sync";
  private static final String JAVA_UI_DEBUG_MODE_LABEL = "Debug Mode";
  private static final String JAVA_UI_START_BUTTON_LABEL = "Start";
  private static final Boolean JAVA_UI_FLAG_FULLSCREEN_DEFAULT = Boolean.TRUE;
  private static final Boolean JAVA_UI_FLAG_VSYNC_DEFAULT = Boolean.TRUE;
  private static final Boolean JAVA_UI_FLAG_DEBUG_MODE_DEFAULT = Boolean.TRUE;
  private static final String PROP_PREFIX_RESOLUTION_WIDTH = "width";
  private static final String PROP_PREFIX_RESOLUTION_HEIGHT = "height";
  private static final String PROP_PREFIX_FULLSCREEN = "fullscreen";
  private static final String PROP_PREFIX_VSYNC = "vsync";
  private static final String PROP_PREFIX_TITLE = "title";
  private static final String PROP_WINDOW_ICON_LOCATION = "app.info.window-icon-location";
  private static final String PROP_ABOUT_IMAGE_LOCATION = "app.info.about-image";

  private static String getValuePropertyNameFromVideoSettings(final String fieldName) {
    return Arrays.asList(VideoSettings.class.getConstructors()
        [BigInteger.ZERO.intValue()].getParameters())
      .stream().map(parameter -> {
        final String p = parameter.getDeclaredAnnotation(
          org.springframework.beans.factory.annotation.Value.class).value();
        return p.substring(BigInteger.TWO.intValue(), p.length() - BigInteger.ONE.intValue());
      }).filter(p -> p.endsWith(fieldName)).findFirst().orElse(null);
  }

  private static class Resolution {
    public Resolution(final java.awt.DisplayMode displayMode) {
      this.width = displayMode.getWidth();
      this.height = displayMode.getHeight();
    }
    private final Integer width;
    public Integer getWidth() { return this.width; }
    private final Integer height;
    public Integer getHeight() { return this.height; }
    @Override public String toString() {
      return this.width.toString().concat(RESOLUTION_SEPARATOR).concat(this.height.toString());
    }
    @Override public boolean equals(final Object that) {
      if(!(that instanceof Resolution)) return Boolean.FALSE;
      return ((Resolution) that).getWidth().equals(this.getWidth()) &&
             ((Resolution) that).getHeight().equals(this.getHeight()); }
    @Override public int hashCode() { return this.width.hashCode() + this.height.hashCode(); }
  }

  private static ImageIcon getImageIconFromProperty(
      final Map<String, String> applicationProperties, final String property) {
    final String imageResourceLocation = applicationProperties.get(property);
    if(imageResourceLocation == null || imageResourceLocation.isBlank()) return null;
    return new ImageIcon(Thread.currentThread().getContextClassLoader()
      .getResource(imageResourceLocation.trim()));
  }

  @SuppressWarnings({ "rawtypes", "unchecked", })
  public static void initConfig(
      final org.springframework.boot.SpringApplication springApp, final String... args)
        throws Exception {
    final Map<String, String> applicationProperties = Arrays.asList(
        java.nio.charset.StandardCharsets.UTF_8.decode(
      com.github.xmaiax.App.getBufferFromResource("application.properties")).toString()
        .split(System.lineSeparator())).stream()
          .filter(str -> str != null && !str.isBlank()).collect(
        Collectors.toMap(str -> str.toString().split(PROPERTIES_SEPARATOR)[BigInteger.ZERO.intValue()],
                         str -> str.toString().split(PROPERTIES_SEPARATOR)[BigInteger.ONE.intValue()]));
    javax.swing.UIManager.setLookAndFeel(SET_LOOK_AND_FEEL);
    final String title = applicationProperties.get(
      getValuePropertyNameFromVideoSettings(PROP_PREFIX_TITLE)).toString();
    final JFrame configFrame = new JFrame(title);
    configFrame.setLocationRelativeTo(null);
    configFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    configFrame.setMinimumSize(new java.awt.Dimension(JAVA_UI_WIDTH, JAVA_UI_HEIGHT));
    configFrame.setResizable(JAVA_UI_RESIZABLE);
    configFrame.setAlwaysOnTop(JAVA_UI_ALWAYS_ON_TOP);
    final JPanel panel = new JPanel();
    final java.util.List<Resolution> allResolutions = Arrays.asList(
        java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
      .getScreenDevices()).stream().map(screenDevices -> Arrays.asList(screenDevices.getDisplayModes()))
        .flatMap(java.util.Collection::stream).map(displayMode -> new Resolution(displayMode))
          .collect(Collectors.toSet())
          .stream().sorted((x, y) -> x.getWidth().compareTo(y.getWidth()))
          .collect(Collectors.toList());
    final JPanel resolutionsPanel = new JPanel();
    final JComboBox resolutionsComboBox = new JComboBox(allResolutions.toArray());
    resolutionsComboBox.setSelectedIndex(allResolutions.size() - BigInteger.ONE.intValue());
    resolutionsPanel.add(new javax.swing.JLabel(InitConfigs.JAVA_UI_RESOLUTION_LABEL));
    resolutionsPanel.add(resolutionsComboBox);
    panel.add(resolutionsPanel);
    final JPanel fullscreenPanel = new JPanel();
    final JCheckBox fullscreenCheckbox = new JCheckBox(
      JAVA_UI_FULLSCREEN_LABEL, JAVA_UI_FLAG_FULLSCREEN_DEFAULT);
    fullscreenPanel.add(fullscreenCheckbox);
    panel.add(fullscreenPanel);
    final JPanel vsyncPanel = new JPanel();
    final JCheckBox vsyncCheckbox = new JCheckBox(
      JAVA_UI_VSYNC_LABEL, JAVA_UI_FLAG_VSYNC_DEFAULT);
    vsyncCheckbox.setEnabled(true);
    vsyncPanel.add(vsyncCheckbox);
    panel.add(vsyncPanel);
    final JPanel debugModePanel = new JPanel();
    final JCheckBox debugModeCheckbox = new JCheckBox(
      JAVA_UI_DEBUG_MODE_LABEL, JAVA_UI_FLAG_DEBUG_MODE_DEFAULT);
    debugModePanel.add(debugModeCheckbox);
    panel.add(debugModePanel);
    panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
    configFrame.getContentPane().add(panel, java.awt.BorderLayout.CENTER);
    final JPanel buttonsPanel = new JPanel();
    final JButton startButton = new JButton(JAVA_UI_START_BUTTON_LABEL);
    startButton.addActionListener(e -> {
      configFrame.dispose();
      final Properties props = new Properties();
      final Resolution selectedResolution = (Resolution) allResolutions
        .toArray()[resolutionsComboBox.getSelectedIndex()];
      props.setProperty(getValuePropertyNameFromVideoSettings(
        PROP_PREFIX_RESOLUTION_WIDTH), selectedResolution.getWidth().toString());
      props.setProperty(getValuePropertyNameFromVideoSettings(
        PROP_PREFIX_RESOLUTION_HEIGHT), selectedResolution.getHeight().toString());
      props.setProperty(getValuePropertyNameFromVideoSettings(
        PROP_PREFIX_FULLSCREEN), Boolean.toString(fullscreenCheckbox.isSelected()));
      props.setProperty(getValuePropertyNameFromVideoSettings(
        PROP_PREFIX_VSYNC), Boolean.toString(vsyncCheckbox.isSelected()));
      props.setProperty("logging.file", String.format("./%s.log",
        applicationProperties.get("app.info.release-name")));
      props.setProperty("logging.pattern.file",
        applicationProperties.get("logging.pattern.console"));
      props.setProperty("logging.level.root", debugModeCheckbox.isSelected() ? "INFO" : "WARN");
      props.setProperty(String.format("logging.level.%s",
        applicationProperties.get("app.info.release-package")),
        debugModeCheckbox.isSelected() ? "DEBUG" : "WARN");
      springApp.setDefaultProperties(props);
      springApp.run(args);
    });
    final ImageIcon appIcon = getImageIconFromProperty(
      applicationProperties, PROP_WINDOW_ICON_LOCATION);
    if(appIcon != null) configFrame.setIconImage(appIcon.getImage());
    final JButton aboutButton = new JButton("About");
    aboutButton.addActionListener((e) -> {
      javax.swing.JOptionPane.showMessageDialog(configFrame, String.format(
          "%s\n\nVersion: %s\nProfile: %s\nLWJGL Version: %s\n\nContact:\n%s (%s)\n",
        applicationProperties.get("app.info.welcome-message"),
        applicationProperties.get("app.info.release-version"),
        applicationProperties.get("spring.profiles.active"),
        applicationProperties.get("app.info.lwjgl-version"),
        applicationProperties.get("app.info.contact.name"),
        applicationProperties.get("app.info.contact.email")), "About ".concat(title),
          javax.swing.JOptionPane.QUESTION_MESSAGE,
          getImageIconFromProperty(applicationProperties, PROP_ABOUT_IMAGE_LOCATION));
    });
    buttonsPanel.add(aboutButton);
    buttonsPanel.add(startButton);
    configFrame.getContentPane().add(buttonsPanel, java.awt.BorderLayout.PAGE_END);
    configFrame.pack();
    configFrame.setVisible(Boolean.TRUE);
  }

}
