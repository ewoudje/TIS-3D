package li.cil.tis3d.client;

import li.cil.tis3d.api.ClientAPI;
import li.cil.tis3d.api.prefab.module.AbstractModule;
import li.cil.tis3d.client.gui.TerminalModuleScreen;
import li.cil.tis3d.client.renderer.block.entity.CasingBlockEntityRenderer;
import li.cil.tis3d.client.renderer.block.entity.ControllerBlockEntityRenderer;
import li.cil.tis3d.client.renderer.font.NormalFontRenderer;
import li.cil.tis3d.client.renderer.font.SmallFontRenderer;
import li.cil.tis3d.common.block.entity.BlockEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class ClientSetup {
    public static void setup(final FMLClientSetupEvent ignoredEvent) {
        ClientAPI.normalFontRenderer = NormalFontRenderer.INSTANCE;
        ClientAPI.smallFontRenderer = SmallFontRenderer.INSTANCE;

        BlockEntityRenderers.register(BlockEntities.CASING.get(), CasingBlockEntityRenderer::new);
        BlockEntityRenderers.register(BlockEntities.CONTROLLER.get(), ControllerBlockEntityRenderer::new);

        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post e) -> AbstractModule.MainThreadDisposer.disposeModules());
        NeoForge.EVENT_BUS.addListener((RenderGuiEvent.Pre event) -> {
            if (Minecraft.getInstance().screen instanceof TerminalModuleScreen) {
                event.setCanceled(true);
            }
        });
    }
}
