package li.cil.tis3d.common.block;

import com.mojang.serialization.MapCodec;
import li.cil.tis3d.common.block.entity.BlockEntities;
import li.cil.tis3d.common.block.entity.ControllerBlockEntity;
import li.cil.tis3d.common.item.Items;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

/**
 * Block for the controller driving the casings.
 */
public final class ControllerBlock extends BaseEntityBlock {
    public static final MapCodec<ControllerBlock> CODEC = simpleCodec(ControllerBlock::new);

    // --------------------------------------------------------------------- //

    public ControllerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    // --------------------------------------------------------------------- //
    // BaseEntityBlock

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return BlockEntities.CONTROLLER.get().create(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level level, final BlockState state, final BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        } else {
            return createTickerHelper(type, BlockEntities.CONTROLLER.get(), ControllerBlockEntity::serverTick);
        }
    }

    @Override
    public RenderShape getRenderShape(final BlockState state) {
        return RenderShape.MODEL;
    }

    // --------------------------------------------------------------------- //
    // Common


    @Override
    protected InteractionResult useItemOn(ItemStack heldItem, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        final Item item = heldItem.getItem();
        if (item == net.minecraft.world.item.Items.BOOK) {
            if (!level.isClientSide()) {
                if (!player.getAbilities().instabuild) {
                    heldItem.split(1);
                }
                final ItemStack bookManual = new ItemStack(Items.BOOK_MANUAL.get());
                if (player.getInventory().add(bookManual)) {
                    player.containerMenu.broadcastChanges();
                }
                if (bookManual.getCount() > 0) {
                    player.drop(bookManual, false, false);
                }
            }

            return InteractionResult.SUCCESS;
        }

        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof final ControllerBlockEntity controller) {
            if (!level.isClientSide()) {
                controller.forceStep();
            }

            return InteractionResult.SUCCESS;
        }

        return super.useItemOn(heldItem, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof final ControllerBlockEntity controller) {
            if (!level.isClientSide()) {
                controller.forceStep();
            }

            return InteractionResult.SUCCESS;
        }

        return super.useWithoutItem(state, level, pos, player, hitResult);
    }


    // --------------------------------------------------------------------- //
    // Redstone

    @SuppressWarnings("deprecation")
    @Override
    public boolean hasAnalogOutputSignal(final BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof final ControllerBlockEntity controller) {
            return controller.getState() == ControllerBlockEntity.ControllerState.READY ? 15 : 0;
        }
        return 0;
    }

    // --------------------------------------------------------------------- //
    // Networking


    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof final ControllerBlockEntity controller) {
            controller.checkNeighbors();
        }
        super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);
    }
}
