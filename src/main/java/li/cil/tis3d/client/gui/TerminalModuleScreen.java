package li.cil.tis3d.client.gui;

import li.cil.tis3d.client.ClientConfig;
import li.cil.tis3d.common.module.TerminalModule;
import li.cil.tis3d.util.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

/**
 * Invisible GUI for the terminal module, purely used to grab keyboard input.
 */
public final class TerminalModuleScreen extends Screen {
    private final TerminalModule module;

    public TerminalModuleScreen(final TerminalModule module) {
        super(Component.empty());
        this.module = module;
    }

    public boolean isFor(final TerminalModule that) {
        return that == module;
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks) {
        graphics.fill(4, 4, width - 4, 8, Color.WHITE); // Top
        graphics.fill(4, 4, 8, height - 4, Color.WHITE); // Left
        graphics.fill(4, height - 8, width - 4, height - 4, Color.WHITE); // Bottom
        graphics.fill(width - 8, 4, width - 4, height - 4, Color.WHITE); // Right
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (super.keyPressed(event)) {
            return true;
        }

        final int keyCode = event.key();
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            writeToModule('\b');
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            writeToModule('\n');
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_TAB) {
            writeToModule('\t');
            return true;
        }

        return false;
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (super.charTyped(event)) {
            return true;
        }

        if (event.codepoint() != '\0') {
            writeToModule((char) event.codepoint());
            return true;
        }

        return false;
    }

    private void writeToModule(final char value) {
        module.writeToInput(value);

        final LocalPlayer player = getMinecraft().player;
        if (ClientConfig.animateTypingHand && player != null) {
            player.swing(InteractionHand.MAIN_HAND);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean isInGameUi() {
        return true;
    }

    // --------------------------------------------------------------------- //

    public Minecraft getMinecraft() {
        return Objects.requireNonNull(minecraft);
    }
}
