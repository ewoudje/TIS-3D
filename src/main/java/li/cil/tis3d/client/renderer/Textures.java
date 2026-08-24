package li.cil.tis3d.client.renderer;

import li.cil.tis3d.api.API;
import net.minecraft.resources.Identifier;

public final class Textures {
    public static final Identifier LOCATION_GUI_BOOK_CODE_BACKGROUND = API.resource("textures/gui/code_book.png");
    public static final Identifier LOCATION_GUI_MANUAL_BACKGROUND = API.resource("textures/gui/manual.png");
    public static final Identifier LOCATION_GUI_MANUAL_TAB = API.resource("textures/gui/manual_tab.png");
    public static final Identifier LOCATION_GUI_MANUAL_SCROLL = API.resource("textures/gui/manual_scroll.png");
    public static final Identifier LOCATION_GUI_MEMORY = API.resource("textures/gui/module_memory.png");

    public static final Identifier LOCATION_OVERLAY_CASING_LOCKED = API.resource("block/overlay/casing_locked");
    public static final Identifier LOCATION_OVERLAY_CASING_UNLOCKED = API.resource("block/overlay/casing_unlocked");
    public static final Identifier LOCATION_OVERLAY_CASING_PORT_CLOSED = API.resource("block/overlay/casing_port_closed");
    public static final Identifier LOCATION_OVERLAY_CASING_PORT_OPEN = API.resource("block/overlay/casing_port_open");
    public static final Identifier LOCATION_OVERLAY_CASING_PORT_HIGHLIGHT = API.resource("block/overlay/casing_port_highlight");
    public static final Identifier LOCATION_OVERLAY_CASING_PORT_CLOSED_SMALL = API.resource("block/overlay/casing_port_closed_small");

    public static final Identifier LOCATION_OVERLAY_MODULE_AUDIO = API.resource("block/overlay/audio_module");
    public static final Identifier LOCATION_OVERLAY_MODULE_EXECUTION_ERROR = API.resource("block/overlay/execution_module_error");
    public static final Identifier LOCATION_OVERLAY_MODULE_EXECUTION_IDLE = API.resource("block/overlay/execution_module_idle");
    public static final Identifier LOCATION_OVERLAY_MODULE_EXECUTION_RUNNING = API.resource("block/overlay/execution_module_running");
    public static final Identifier LOCATION_OVERLAY_MODULE_EXECUTION_WAITING = API.resource("block/overlay/execution_module_waiting");
    public static final Identifier LOCATION_OVERLAY_MODULE_INFRARED = API.resource("block/overlay/infrared_module");
    public static final Identifier LOCATION_OVERLAY_MODULE_KEYPAD = API.resource("block/overlay/keypad_module");
    public static final Identifier LOCATION_OVERLAY_MODULE_QUEUE = API.resource("block/overlay/queue_module");
    public static final Identifier LOCATION_OVERLAY_MODULE_RANDOM = API.resource("block/overlay/random_module");
    public static final Identifier LOCATION_OVERLAY_MODULE_REDSTONE = API.resource("block/overlay/redstone_module");
    public static final Identifier LOCATION_OVERLAY_MODULE_REDSTONE_BARS = API.resource("block/overlay/redstone_bars_module");
    public static final Identifier LOCATION_OVERLAY_MODULE_SEQUENCER = API.resource("block/overlay/sequencer_module");
    public static final Identifier LOCATION_OVERLAY_MODULE_SERIAL_PORT = API.resource("block/overlay/serial_port_module");
    public static final Identifier LOCATION_OVERLAY_MODULE_STACK = API.resource("block/overlay/stack_module");
    public static final Identifier LOCATION_OVERLAY_MODULE_TERMINAL = API.resource("block/overlay/terminal_module");
    public static final Identifier LOCATION_OVERLAY_MODULE_TIMER = API.resource("block/overlay/timer_module");

    // --------------------------------------------------------------------- //

    private Textures() {
    }
}
