package li.cil.tis3d.common;

import li.cil.tis3d.api.API;
import li.cil.tis3d.api.prefab.module.AbstractModule;
import li.cil.tis3d.client.ClientBootstrap;
import li.cil.tis3d.client.ClientConfig;
import li.cil.tis3d.client.manual.Manuals;
import li.cil.tis3d.common.api.InfraredAPIImpl;
import li.cil.tis3d.common.block.Blocks;
import li.cil.tis3d.common.block.entity.BlockEntities;
import li.cil.tis3d.common.config.CommonConfig;
import li.cil.tis3d.common.container.ModMenus;
import li.cil.tis3d.common.entity.Entities;
import li.cil.tis3d.common.event.InfraredPacketTickHandler;
import li.cil.tis3d.common.item.DataComponentTypes;
import li.cil.tis3d.common.item.Items;
import li.cil.tis3d.common.item.ModCreativeTabs;
import li.cil.tis3d.common.network.Network;
import li.cil.tis3d.common.provider.ModuleProviders;
import li.cil.tis3d.common.provider.RedstoneInputProviders;
import li.cil.tis3d.common.provider.SerialInterfaceProviders;
import li.cil.tis3d.common.tags.BlockTags;
import li.cil.tis3d.common.tags.ItemTags;
import li.cil.tis3d.data.DataGenerators;
import li.cil.tis3d.util.RegistryUtils;
import li.cil.tis3d.util.neoforge.ConfigManagerImpl;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod(API.MOD_ID)
public final class TIS3DMod {
    public TIS3DMod(final ModContainer modContainer, final IEventBus bus) {
        RegistryUtils.begin(API.MOD_ID);

        ConfigManagerImpl.add(CommonConfig::new);
        ConfigManagerImpl.add(ClientConfig::new);
        ConfigManagerImpl.initialize(modContainer, bus);

        bus.addListener(DataGenerators::gatherData);
        bus.addListener(Network::register);
        bus.addListener(TIS3DMod::setup);

        ItemTags.initialize();
        BlockTags.initialize();
        Blocks.initialize(bus);
        DataComponentTypes.initialize(bus);
        Items.initialize(bus);
        BlockEntities.initialize(bus);
        Entities.initialize(bus);
        ModMenus.initialize(bus);

        ModuleProviders.initialize(bus);
        SerialInterfaceProviders.initialize(bus);
        RedstoneInputProviders.initialize(bus);
        ModCreativeTabs.initialize(bus);

        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            Manuals.initialize(bus);
            ClientBootstrap.setup(bus);
        }

        RegistryUtils.finish(bus);
    }

    public static void setup(final FMLCommonSetupEvent event) {
        API.itemGroup = ModCreativeTabs.COMMON;
        API.infraredAPI = new InfraredAPIImpl();

        InfraredPacketTickHandler.initialize();

        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post e) -> AbstractModule.MainThreadDisposer.disposeModules());
    }
}
