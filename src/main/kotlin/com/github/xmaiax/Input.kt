package com.github.xmaiax

import org.lwjgl.glfw.GLFW

data class InputedAction(val key: InputedKey, val event: InputEvent) {
  override fun toString() = "${event} -> ${this.key}" }
fun InputedAction(inputKey: Int, keyEvent: Int) =
  InputedAction(InputedKey.fromCode(inputKey), InputEvent.fromCode(keyEvent))

enum class InputedControllerKey(val position: Int, val key: InputedKey) {
  CROSS(0, InputedKey._GAMEPAD_CROSS),
  CIRCLE(1, InputedKey._GAMEPAD_CIRCLE),
  SQUARE(2, InputedKey._GAMEPAD_SQUARE),
  TRIANGLE(3, InputedKey._GAMEPAD_TRIANGLE),
  L1(4, InputedKey._GAMEPAD_L1),
  R1(5, InputedKey._GAMEPAD_R2),
  SELECT(6, InputedKey._GAMEPAD_SELECT),
  START(7, InputedKey._GAMEPAD_START),
  L3(8, InputedKey._GAMEPAD_L3),
  R3(9, InputedKey._GAMEPAD_R3),
  UP(10, InputedKey._GAMEPAD_DPAD_UP),
  RIGHT(11, InputedKey._GAMEPAD_DPAD_RIGHT),
  DOWN(12, InputedKey._GAMEPAD_DPAD_DOWN),
  LEFT(13, InputedKey._GAMEPAD_DPAD_LEFT);
  fun getInputedAction(event: InputEvent) = InputedAction(this.key, event)
}

enum class InputedKey(private val alias: String, val glfwCode: Int) {
  _SPACE("Space", GLFW.GLFW_KEY_SPACE),
  _MINUS("-", GLFW.GLFW_KEY_MINUS),
  _APOSTROPHE("'", GLFW.GLFW_KEY_APOSTROPHE),
  _COMMA(",", GLFW.GLFW_KEY_COMMA),
  _PERIOD(".", GLFW.GLFW_KEY_PERIOD),
  _SEMICOLON(";", GLFW.GLFW_KEY_SEMICOLON),
  _SLASH("/", GLFW.GLFW_KEY_SLASH),
  _BACKSLASH("\\", GLFW.GLFW_KEY_BACKSLASH),
  _EQUAL("=", GLFW.GLFW_KEY_EQUAL),
  _LEFT_BRACKET("{", GLFW.GLFW_KEY_LEFT_BRACKET),
  _RIGHT_BRACKET("}", GLFW.GLFW_KEY_RIGHT_BRACKET),
  _GRAVE_ACCENT("`", GLFW.GLFW_KEY_GRAVE_ACCENT),
  _0("0", GLFW.GLFW_KEY_0),
  _1("1", GLFW.GLFW_KEY_1),
  _2("2", GLFW.GLFW_KEY_2),
  _3("3", GLFW.GLFW_KEY_3),
  _4("4", GLFW.GLFW_KEY_4),
  _5("5", GLFW.GLFW_KEY_5),
  _6("6", GLFW.GLFW_KEY_6),
  _7("7", GLFW.GLFW_KEY_7),
  _8("8", GLFW.GLFW_KEY_8),
  _9("9", GLFW.GLFW_KEY_9),
  _A("A", GLFW.GLFW_KEY_A),
  _B("B", GLFW.GLFW_KEY_B),
  _C("C", GLFW.GLFW_KEY_C),
  _D("D", GLFW.GLFW_KEY_D),
  _E("E", GLFW.GLFW_KEY_E),
  _F("F", GLFW.GLFW_KEY_F),
  _G("G", GLFW.GLFW_KEY_G),
  _H("H", GLFW.GLFW_KEY_H),
  _I("I", GLFW.GLFW_KEY_I),
  _J("J", GLFW.GLFW_KEY_J),
  _K("K", GLFW.GLFW_KEY_K),
  _L("L", GLFW.GLFW_KEY_L),
  _M("M", GLFW.GLFW_KEY_M),
  _N("N", GLFW.GLFW_KEY_N),
  _O("O", GLFW.GLFW_KEY_O),
  _P("P", GLFW.GLFW_KEY_P),
  _Q("Q", GLFW.GLFW_KEY_Q),
  _R("R", GLFW.GLFW_KEY_R),
  _S("S", GLFW.GLFW_KEY_S),
  _T("T", GLFW.GLFW_KEY_T),
  _U("U", GLFW.GLFW_KEY_U),
  _V("V", GLFW.GLFW_KEY_V),
  _W("W", GLFW.GLFW_KEY_W),
  _X("X", GLFW.GLFW_KEY_X),
  _Y("Y", GLFW.GLFW_KEY_Y),
  _Z("Z", GLFW.GLFW_KEY_Z),
  _WORLD_1("World 1", GLFW.GLFW_KEY_WORLD_1),
  _WORLD_2("World 2", GLFW.GLFW_KEY_WORLD_2),
  _ESCAPE("Escape", GLFW.GLFW_KEY_ESCAPE),
  _ENTER("Enter", GLFW.GLFW_KEY_ENTER),
  _TAB("Tab", GLFW.GLFW_KEY_TAB),
  _BACKSPACE("Backspace", GLFW.GLFW_KEY_BACKSPACE),
  _CAPS_LOCK("Caps Lock", GLFW.GLFW_KEY_CAPS_LOCK),
  _INSERT("Insert", GLFW.GLFW_KEY_INSERT),
  _DELETE("Delete", GLFW.GLFW_KEY_DELETE),
  _UP("Up", GLFW.GLFW_KEY_UP),
  _LEFT("Left", GLFW.GLFW_KEY_LEFT),
  _DOWN("Down", GLFW.GLFW_KEY_DOWN),
  _RIGHT("Right", GLFW.GLFW_KEY_RIGHT),
  _PAGE_UP("Page Up", GLFW.GLFW_KEY_PAGE_UP),
  _PAGE_DOWN("Page Down", GLFW.GLFW_KEY_PAGE_DOWN),
  _HOME("Home", GLFW.GLFW_KEY_HOME),
  _END("End", GLFW.GLFW_KEY_END),
  _F1("F1", GLFW.GLFW_KEY_F1),
  _F2("F2", GLFW.GLFW_KEY_F2),
  _F3("F3", GLFW.GLFW_KEY_F3),
  _F4("F4", GLFW.GLFW_KEY_F4),
  _F5("F5", GLFW.GLFW_KEY_F5),
  _F6("F6", GLFW.GLFW_KEY_F6),
  _F7("F7", GLFW.GLFW_KEY_F7),
  _F8("F8", GLFW.GLFW_KEY_F8),
  _F9("F9", GLFW.GLFW_KEY_F9),
  _F10("F10", GLFW.GLFW_KEY_F10),
  _F11("F11", GLFW.GLFW_KEY_F11),
  _F12("F12", GLFW.GLFW_KEY_F12),
  _F13("F13", GLFW.GLFW_KEY_F13),
  _F14("F14", GLFW.GLFW_KEY_F14),
  _F15("F15", GLFW.GLFW_KEY_F15),
  _F16("F16", GLFW.GLFW_KEY_F16),
  _F17("F17", GLFW.GLFW_KEY_F17),
  _F18("F18", GLFW.GLFW_KEY_F18),
  _F19("F19", GLFW.GLFW_KEY_F19),
  _F20("F20", GLFW.GLFW_KEY_F20),
  _F21("F21", GLFW.GLFW_KEY_F21),
  _F22("F22", GLFW.GLFW_KEY_F22),
  _F23("F23", GLFW.GLFW_KEY_F23),
  _F24("F24", GLFW.GLFW_KEY_F24),
  _F25("F25", GLFW.GLFW_KEY_F25),
  _PRINT_SCREEN("Print Screen", GLFW.GLFW_KEY_PRINT_SCREEN),
  _SCROLL_LOCK("Scroll Lock", GLFW.GLFW_KEY_SCROLL_LOCK),
  _PAUSE("Pause", GLFW.GLFW_KEY_PAUSE),
  _NUM_LOCK("Num Lock", GLFW.GLFW_KEY_NUM_LOCK),
  _KP_0("[Num] 0", GLFW.GLFW_KEY_KP_0),
  _KP_1("[Num] 1", GLFW.GLFW_KEY_KP_1),
  _KP_2("[Num] 2", GLFW.GLFW_KEY_KP_2),
  _KP_3("[Num] 3", GLFW.GLFW_KEY_KP_3),
  _KP_4("[Num] 4", GLFW.GLFW_KEY_KP_4),
  _KP_5("[Num] 5", GLFW.GLFW_KEY_KP_5),
  _KP_6("[Num] 6", GLFW.GLFW_KEY_KP_6),
  _KP_7("[Num] 7", GLFW.GLFW_KEY_KP_7),
  _KP_8("[Num] 8", GLFW.GLFW_KEY_KP_8),
  _KP_9("[Num] 9", GLFW.GLFW_KEY_KP_9),
  _KP_ADD("[Num] +", GLFW.GLFW_KEY_KP_ADD),
  _KP_SUBTRACT("[Num] -", GLFW.GLFW_KEY_KP_SUBTRACT),
  _KP_MULTIPLY("[Num] *", GLFW.GLFW_KEY_KP_MULTIPLY),
  _KP_DIVIDE("[Num] /", GLFW.GLFW_KEY_KP_DIVIDE),
  _KP_ENTER("[Num] Enter", GLFW.GLFW_KEY_KP_ENTER),
  _KP_EQUAL("[Num] =", GLFW.GLFW_KEY_KP_EQUAL),
  _KP_COMMA("[Num] .", GLFW.GLFW_KEY_KP_DECIMAL),
  _LEFT_SHIFT("Left Shift", GLFW.GLFW_KEY_LEFT_SHIFT),
  _RIGHT_SHIFT("Right Shift", GLFW.GLFW_KEY_RIGHT_SHIFT),
  _LEFT_CONTROL("Left Control", GLFW.GLFW_KEY_LEFT_CONTROL),
  _RIGHT_CONTROL("Right Control", GLFW.GLFW_KEY_RIGHT_CONTROL),
  _LEFT_ALT("Left Alt", GLFW.GLFW_KEY_LEFT_ALT),
  _RIGHT_ALT("Right Alt", GLFW.GLFW_KEY_RIGHT_ALT),
  _LEFT_SUPER("Left Super", GLFW.GLFW_KEY_LEFT_SUPER),
  _RIGHT_SUPER("Right Super", GLFW.GLFW_KEY_RIGHT_SUPER),
  _MENU("Menu", GLFW.GLFW_KEY_MENU),
  _LAST("Last", GLFW.GLFW_KEY_LAST),
  _GAMEPAD_L1("[Gamepad] L1", GLFW.GLFW_GAMEPAD_BUTTON_LEFT_BUMPER),
  _GAMEPAD_R2("[Gamepad] R1", GLFW.GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER),
  _GAMEPAD_SELECT("[Gamepad] Select", GLFW.GLFW_GAMEPAD_BUTTON_BACK),
  _GAMEPAD_START("[Gamepad] Start", GLFW.GLFW_GAMEPAD_BUTTON_START),
  _GAMEPAD_L3("[Gamepad] L3", GLFW.GLFW_GAMEPAD_BUTTON_LEFT_THUMB),
  _GAMEPAD_R3("[Gamepad] R3", GLFW.GLFW_GAMEPAD_BUTTON_RIGHT_THUMB),
  _GAMEPAD_DPAD_UP("[Gamepad] Digital Pad Up", GLFW.GLFW_GAMEPAD_BUTTON_DPAD_UP),
  _GAMEPAD_DPAD_RIGHT("[Gamepad] Digital Pad Right", GLFW.GLFW_GAMEPAD_BUTTON_DPAD_RIGHT),
  _GAMEPAD_DPAD_DOWN("[Gamepad] Digital Pad Down", GLFW.GLFW_GAMEPAD_BUTTON_DPAD_DOWN),
  _GAMEPAD_DPAD_LEFT("[Gamepad] Digital Pad Left", GLFW.GLFW_GAMEPAD_BUTTON_DPAD_LEFT),
  _GAMEPAD_CROSS("[Gamepad] Cross", GLFW.GLFW_GAMEPAD_BUTTON_CROSS),
  _GAMEPAD_CIRCLE("[Gamepad] Circle", GLFW.GLFW_GAMEPAD_BUTTON_CIRCLE),
  _GAMEPAD_SQUARE("[Gamepad] Square", GLFW.GLFW_GAMEPAD_BUTTON_SQUARE),
  _GAMEPAD_TRIANGLE("[Gamepad] Triangle", GLFW.GLFW_GAMEPAD_BUTTON_TRIANGLE),

  ; override fun toString() = this.alias
  companion object {
    fun fromCode(code: Int) = InputedKey.values()
      .filter { it.glfwCode == code }.first() }
}

enum class InputEvent(val alias: String) {
  RELEASE("RLS"), PRESS("PRS"), REPEAT("RPT"), INVALID("???");
  companion object {
    fun fromCode(code: Int) =
      when (code) {
        GLFW.GLFW_RELEASE -> RELEASE
        GLFW.GLFW_PRESS -> PRESS
        GLFW.GLFW_REPEAT -> REPEAT
        else -> INVALID
      }
  }
  override fun toString() = this.alias
}