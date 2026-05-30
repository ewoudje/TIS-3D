package li.cil.tis3d.common;

import li.cil.tis3d.api.API;
import li.cil.tis3d.client.ClientConfig;
import li.cil.tis3d.common.block.Blocks;
import li.cil.tis3d.common.block.entity.BlockEntities;
import li.cil.tis3d.common.config.CommonConfig;
import li.cil.tis3d.common.container.Containers;
import li.cil.tis3d.common.entity.Entities;
import li.cil.tis3d.common.item.DataComponentTypes;
import li.cil.tis3d.common.item.Items;
import li.cil.tis3d.common.item.ModCreativeTabs;
import li.cil.tis3d.common.network.Network;
import li.cil.tis3d.common.ponder.TIS3DPonderPlugin;
import li.cil.tis3d.common.provider.ModuleProviders;
import li.cil.tis3d.common.provider.RedstoneInputProviders;
import li.cil.tis3d.common.provider.SerialInterfaceProviders;
import li.cil.tis3d.common.tags.BlockTags;
import li.cil.tis3d.common.tags.ItemTags;
import li.cil.tis3d.data.DataGenerators;
import li.cil.tis3d.util.ConfigManager;
import li.cil.tis3d.util.RegistryUtils;
import li.cil.tis3d.util.neoforge.ConfigManagerImpl;
import net.createmod.ponder.foundation.PonderIndex;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;

public final class CommonBootstrap {
    public static void setup(ModContainer modContainer, IEventBus bus) {
        ConfigManagerImpl.add(CommonConfig::new);
        ConfigManagerImpl.add(ClientConfig::new);
        ConfigManagerImpl.initialize(modContainer, bus);

        bus.addListener(DataGenerators::gatherData);
        bus.addListener(Network::register);

        ItemTags.initialize();
        BlockTags.initialize();
        Blocks.initialize(bus);
        DataComponentTypes.initialize(bus);
        Items.initialize(bus);
        BlockEntities.initialize(bus);
        Entities.initialize(bus);
        Containers.initialize(bus);

        ModuleProviders.initialize(bus);
        SerialInterfaceProviders.initialize(bus);
        RedstoneInputProviders.initialize(bus);
        ModCreativeTabs.initialize(bus);
    }
}
