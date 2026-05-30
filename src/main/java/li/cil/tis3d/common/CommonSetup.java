package li.cil.tis3d.common;

import li.cil.tis3d.api.API;
import li.cil.tis3d.api.prefab.module.AbstractModule;
import li.cil.tis3d.common.api.InfraredAPIImpl;
import li.cil.tis3d.common.event.InfraredPacketTickHandler;
import li.cil.tis3d.common.item.ModCreativeTabs;
import li.cil.tis3d.common.ponder.TIS3DPonderPlugin;
import net.createmod.ponder.foundation.PonderIndex;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class CommonSetup {
    public static void setup(final FMLCommonSetupEvent event) {
        API.itemGroup = ModCreativeTabs.COMMON;
        API.infraredAPI = new InfraredAPIImpl();

        PonderIndex.addPlugin(new TIS3DPonderPlugin());
        InfraredPacketTickHandler.initialize();

        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post e) -> AbstractModule.MainThreadDisposer.disposeModules());
    }
}
