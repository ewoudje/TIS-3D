package li.cil.tis3d.common.item;

import li.cil.tis3d.common.block.CasingBlock;
import li.cil.tis3d.common.container.ReadOnlyMemoryModuleMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.nio.ByteBuffer;

public final class ReadOnlyMemoryModuleItem extends ModuleItem {
    private static final String TAG_DATA = "data";
    private static final byte[] EMPTY_DATA = new byte[0];

    public ReadOnlyMemoryModuleItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    // --------------------------------------------------------------------- //
    // Item

    /**
     * Load ROM data from the specified item stack.
     *
     * @param stack the item stack to load the data from.
     * @return the data loaded from the stack.
     */
    public static byte[] loadFromStack(final ItemStack stack) {
        ByteBuffer buffer = stack.get(DataComponentTypes.ROM_DATA_COMPONENT);
        if (buffer == null) return EMPTY_DATA;
        if (buffer.hasArray()) return buffer.array();

        byte[] result = new byte[buffer.remaining()];
        buffer.get(result);
        return result;
    }

    /**
     * Save the specified ROM data to the specified item stack.
     *
     * @param stack the item stack to save the data to.
     * @param data  the data to save to the item stack.
     */
    public static void saveToStack(final ItemStack stack, final byte[] data) {
        stack.set(DataComponentTypes.ROM_DATA_COMPONENT, ByteBuffer.wrap(data));
    }

    // --------------------------------------------------------------------- //

    @Override
    public InteractionResult use(final Level level, final Player player, final InteractionHand hand) {
        if (!level.isClientSide() && player instanceof final ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.empty();
                }

                @Override
                public AbstractContainerMenu createMenu(final int id, final Inventory playerInventory, final Player player) {
                    return new ReadOnlyMemoryModuleMenu(id, player, hand);
                }
            }, buffer -> buffer.writeEnum(hand));
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(final UseOnContext context) {
        return CasingBlock.useIfCasing(context).orElseGet(() -> super.useOn(context));
    }
}
