package li.cil.tis3d.common.block;

import li.cil.tis3d.util.RegistryUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

public final class Blocks {
    private static final DeferredRegister<Block> BLOCKS = RegistryUtils.getDeferred(Registries.BLOCK);

    // --------------------------------------------------------------------- //

    public static final DeferredHolder<Block, CasingBlock> CASING = register(
        "casing",
        p -> new CasingBlock(p
            .mapColor(MapColor.METAL)
            .sound(SoundType.METAL)
            .strength(1.5f, 6f))
    );

    public static final DeferredHolder<Block, ControllerBlock> CONTROLLER = register(
        "controller",
        p -> new ControllerBlock(p
            .mapColor(MapColor.METAL)
            .sound(SoundType.METAL)
            .strength(1.5f, 6f))
    );

    // --------------------------------------------------------------------- //

    private static <T extends Block> DeferredHolder<Block, T> register(final String name, final Function<BlockBehaviour.Properties, T> function) {
        return BLOCKS.register(name, registryName ->
            function.apply(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, registryName)))
        );
    }

    // --------------------------------------------------------------------- //

    public static void initialize(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
