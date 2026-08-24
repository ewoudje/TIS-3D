package li.cil.tis3d.client.gui;

import li.cil.tis3d.common.module.TerminalModule;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public class ModScreens {
    public static void openCodeBook(Player player, InteractionHand hand) {
        Minecraft.getInstance().setScreen(new CodeBookScreen(player, hand));
    }

    public static void openTerminal(TerminalModule module) {
        Minecraft.getInstance().setScreen(new TerminalModuleScreen(module));
    }

    public static void closeTerminal(TerminalModule module) {
        if (Minecraft.getInstance().screen instanceof TerminalModuleScreen s) {
            if (s.isFor(module)) Minecraft.getInstance().setScreen(null);
        }
    }

}
