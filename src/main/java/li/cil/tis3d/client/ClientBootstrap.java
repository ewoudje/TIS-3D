package li.cil.tis3d.client;

import li.cil.tis3d.client.gui.ReadOnlyMemoryModuleScreen;
import li.cil.tis3d.client.renderer.ModuleRenderers;
import li.cil.tis3d.client.renderer.color.CasingBlockColor;
import li.cil.tis3d.client.renderer.entity.NullEntityRenderer;
import li.cil.tis3d.common.block.Blocks;
import li.cil.tis3d.common.container.ModMenus;
import li.cil.tis3d.common.entity.Entities;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class ClientBootstrap {
    public static void setup(IEventBus bus) {
        ModuleRenderers.initialize(bus);

        bus.addListener((RegisterColorHandlersEvent.Block e) ->
            e.register(new CasingBlockColor(), Blocks.CASING.get()));

        bus.addListener((EntityRenderersEvent.RegisterRenderers e) ->
            e.registerEntityRenderer(Entities.INFRARED_PACKET.get(), NullEntityRenderer::new));

        bus.addListener((RegisterMenuScreensEvent e) ->
            e.register(ModMenus.READ_ONLY_MEMORY_MODULE.get(), ReadOnlyMemoryModuleScreen::new));

        bus.addListener(ClientSetup::handleModelRegistryEvent);
        bus.addListener(ClientSetup::setup);
    }
}
