package li.cil.tis3d.common.block.entity;

import li.cil.tis3d.common.block.Blocks;
import li.cil.tis3d.util.RegistryUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class BlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = RegistryUtils.getDeferred(Registries.BLOCK_ENTITY_TYPE);

    // --------------------------------------------------------------------- //

    public static void initialize(IEventBus bus) {
        BLOCK_ENTITY_TYPES.register(bus);
    }

    @SuppressWarnings("ConstantConditions") // .build(null) is fine
    private static <B extends Block, T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> register(final DeferredHolder<Block, B> block, final BlockEntityType.BlockEntitySupplier<T> factory) {
        return BLOCK_ENTITY_TYPES.register(block.getId().getPath(), () -> new BlockEntityType<>(factory, block.get()));
    }

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CasingBlockEntity>> CASING = register(Blocks.CASING, CasingBlockEntity::new);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ControllerBlockEntity>> CONTROLLER = register(Blocks.CONTROLLER, ControllerBlockEntity::new);

    // --------------------------------------------------------------------- //


    // --------------------------------------------------------------------- //


}
