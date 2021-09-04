package com.github.xmaiax

class InitConfigs {
  companion object {
    val SET_LOOK_AND_FEEL = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel"
    val RESOLUTION_SEPARATOR = "x"
    val JAVA_UI_WIDTH = 250
    val JAVA_UI_HEIGHT = 250
    val JAVA_UI_RESIZABLE = false
    val JAVA_UI_ALWAYS_ON_TOP = true
    val JAVA_UI_RESOLUTION_LABEL = "Resolution"
    val JAVA_UI_FULLSCREEN_LABEL = "Fullscreen"
    val JAVA_UI_VSYNC_LABEL = "V-Sync"
    val JAVA_UI_DEBUG_MODE_LABEL = "Debug Mode"
    val JAVA_UI_START_BUTTON_LABEL = "Start"
    val JAVA_UI_FLAG_FULLSCREEN_DEFAULT = true
    val JAVA_UI_FLAG_VSYNC_DEFAULT = true
    val JAVA_UI_FLAG_DEBUG_MODE_DEFAULT = false
    val PROP_PREFIX_RESOLUTION_WIDTH = "width"
    val PROP_PREFIX_RESOLUTION_HEIGHT = "height"
    val PROP_PREFIX_FULLSCREEN = "fullscreen"
    val PROP_PREFIX_VSYNC = "vsync"
    val PROP_PREFIX_TITLE = "title"
    val PROP_WINDOW_ICON_LOCATION = "app.info.window-icon-location"
    val PROP_ABOUT_IMAGE_LOCATION = "app.info.about-image"
  }
}

private fun getValuePropertyNameFromVideoSettings(fieldName: String) =
  VideoSettings::class.java.getConstructors().map { constructor ->
    constructor.getParameters().map { parameter -> val p = parameter.getDeclaredAnnotation(
      org.springframework.beans.factory.annotation.Value::class.java).value
      p.substring(2, p.length - 1) }.filter { it.endsWith(fieldName) } }.flatten().first()

fun initConfig(springApp: org.springframework.boot.SpringApplication, vararg args: String) {
  val propertySeparator = "="
  fun separateKeyFromValue(input: String) = if(input.contains(propertySeparator))
    input.split(propertySeparator)[0] to input.split(propertySeparator)[1] else null
  val applicationProperties = java.nio.charset.StandardCharsets.UTF_8.decode(
    App.getBufferFromResource("application.properties")).toString()
      .split("\n").mapNotNull { separateKeyFromValue(it.trim()) }.toMap()
  val title = applicationProperties.get(
    getValuePropertyNameFromVideoSettings(InitConfigs.PROP_PREFIX_TITLE))
  javax.swing.UIManager.setLookAndFeel(InitConfigs.SET_LOOK_AND_FEEL)
  val configFrame = javax.swing.JFrame(title)
  configFrame.setLocationRelativeTo(null)
  configFrame.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE)
  configFrame.setMinimumSize(java.awt.Dimension(
    InitConfigs.JAVA_UI_WIDTH, InitConfigs.JAVA_UI_HEIGHT))
  configFrame.setResizable(InitConfigs.JAVA_UI_RESIZABLE)
  configFrame.setAlwaysOnTop(InitConfigs.JAVA_UI_ALWAYS_ON_TOP)
  val panel = javax.swing.JPanel()
  val allResolutions = java.awt.GraphicsEnvironment
    .getLocalGraphicsEnvironment().getScreenDevices().map { screenDevices ->
      screenDevices.getDisplayModes().map { "${it.getWidth()}${
        InitConfigs.RESOLUTION_SEPARATOR}${it.getHeight()}" }
    }.flatten().toSet().toTypedArray();
  val resolutionsPanel = javax.swing.JPanel()
  val resolutionsComboBox = javax.swing.JComboBox(allResolutions)
  resolutionsComboBox.setSelectedIndex(allResolutions.size - 1)
  resolutionsPanel.add(javax.swing.JLabel(InitConfigs.JAVA_UI_RESOLUTION_LABEL))
  resolutionsPanel.add(resolutionsComboBox)
  panel.add(resolutionsPanel)
  val fullscreenPanel = javax.swing.JPanel()
  val fullscreenCheckbox = javax.swing.JCheckBox(
    InitConfigs.JAVA_UI_FULLSCREEN_LABEL, InitConfigs.JAVA_UI_FLAG_FULLSCREEN_DEFAULT)
  fullscreenPanel.add(fullscreenCheckbox)
  panel.add(fullscreenPanel)
  val vsyncPanel = javax.swing.JPanel()
  val vsyncCheckbox = javax.swing.JCheckBox(
    InitConfigs.JAVA_UI_VSYNC_LABEL, InitConfigs.JAVA_UI_FLAG_VSYNC_DEFAULT)
  vsyncPanel.add(vsyncCheckbox)
  panel.add(vsyncPanel)
  val debugModePanel = javax.swing.JPanel()
  val debugModeCheckbox = javax.swing.JCheckBox(
    InitConfigs.JAVA_UI_DEBUG_MODE_LABEL, InitConfigs.JAVA_UI_FLAG_DEBUG_MODE_DEFAULT)
  debugModePanel.add(debugModeCheckbox)
  panel.add(debugModePanel)
  panel.setLayout(javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS))
  configFrame.getContentPane().add(panel, java.awt.BorderLayout.CENTER)
  val buttonsPanel = javax.swing.JPanel()
  val startButton = javax.swing.JButton(InitConfigs.JAVA_UI_START_BUTTON_LABEL);
  startButton.addActionListener(java.awt.event.ActionListener { configFrame.dispose()
    val props = java.util.Properties()
    props.setProperty(getValuePropertyNameFromVideoSettings(InitConfigs.PROP_PREFIX_RESOLUTION_WIDTH),
      allResolutions.get(resolutionsComboBox.getSelectedIndex()).split(InitConfigs.RESOLUTION_SEPARATOR)[0])
    props.setProperty(getValuePropertyNameFromVideoSettings(InitConfigs.PROP_PREFIX_RESOLUTION_HEIGHT),
      allResolutions.get(resolutionsComboBox.getSelectedIndex()).split(InitConfigs.RESOLUTION_SEPARATOR)[1])
    props.setProperty(getValuePropertyNameFromVideoSettings(InitConfigs.PROP_PREFIX_FULLSCREEN),
      fullscreenCheckbox.isSelected().toString())
    props.setProperty(getValuePropertyNameFromVideoSettings(InitConfigs.PROP_PREFIX_VSYNC),
      vsyncCheckbox.isSelected().toString())
    if(debugModeCheckbox.isSelected()) {
      props.setProperty("logging.level.root", "INFO")
      props.setProperty("logging.level.${applicationProperties.get("app.info.release-package")}", "DEBUG")
      props.setProperty("logging.file", "./${applicationProperties.get("app.info.release-name")}.log")
      props.setProperty("logging.pattern.file", applicationProperties.get("logging.pattern.console"))
    }
    else {
      props.setProperty("logging.level.root", "WARN")
      props.setProperty("logging.level.${applicationProperties.get("app.info.release-package")}", "INFO")
    }
    springApp.setDefaultProperties(props)
    springApp.run(*args)
  })
  fun getImageIconFromProperty(property: String): javax.swing.ImageIcon? = applicationProperties.get(
    property)?.let { Thread.currentThread().getContextClassLoader()
      .getResource(it)?.let { imageUrl -> javax.swing.ImageIcon(imageUrl) } }
  getImageIconFromProperty(InitConfigs.PROP_WINDOW_ICON_LOCATION)?.let { icon ->
    configFrame.setIconImage(icon.getImage())
  }
  val aboutButton = javax.swing.JButton("About")
  aboutButton.addActionListener(java.awt.event.ActionListener {
    javax.swing.JOptionPane.showMessageDialog(configFrame, """${
  applicationProperties.get("app.info.welcome-message")}

Version: ${applicationProperties.get("app.info.release-version")}
Profile: ${applicationProperties.get("spring.profiles.active")}
LWJGL Version: ${applicationProperties.get("app.info.lwjgl-version")}

Contact:
${applicationProperties.get("app.info.contact.name")} (${
  applicationProperties.get("app.info.contact.email")})
""", "About ${title}", javax.swing.JOptionPane.QUESTION_MESSAGE,
    getImageIconFromProperty(InitConfigs.PROP_ABOUT_IMAGE_LOCATION))
  })
  buttonsPanel.add(aboutButton)
  buttonsPanel.add(startButton)
  configFrame.getContentPane().add(buttonsPanel, java.awt.BorderLayout.PAGE_END)
  configFrame.pack()
  configFrame.setVisible(true)
}
