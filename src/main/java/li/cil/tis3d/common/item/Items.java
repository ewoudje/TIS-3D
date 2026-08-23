package li.cil.tis3d.common.item;

import li.cil.tis3d.common.block.Blocks;
import li.cil.tis3d.util.RegistryUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public final class Items {
    private static final DeferredRegister<Item> ITEMS = RegistryUtils.getDeferred(Registries.ITEM);

    // --------------------------------------------------------------------- //

    public static final DeferredHolder<Item, Item> CASING = register(Blocks.CASING);
    public static final DeferredHolder<Item, Item> CONTROLLER = register(Blocks.CONTROLLER);

    // --------------------------------------------------------------------- //

    public static final DeferredHolder<Item, CodeBookItem> BOOK_CODE = register("code_book", CodeBookItem::new);
    public static final DeferredHolder<Item, ManualItem> BOOK_MANUAL = register("manual", ManualItem::new);

    public static final DeferredHolder<Item, KeyItem> KEY = register("key", KeyItem::new);
    public static final DeferredHolder<Item, KeyItem> KEY_CREATIVE = register("skeleton_key", KeyItem::new);
    public static final DeferredHolder<Item, Item> PRISM = register("prism");
    public static final DeferredHolder<Item, ModuleItem> AUDIO_MODULE = register("audio_module", ModuleItem::new);
    public static final DeferredHolder<Item, ModuleItem> DISPLAY_MODULE = register("display_module", ModuleItem::new);
    public static final DeferredHolder<Item, ModuleItem> EXECUTION_MODULE = register("execution_module", ModuleItem::new);
    public static final DeferredHolder<Item, ModuleItem> FACADE_MODULE = register("facade_module", ModuleItem::new);
    public static final DeferredHolder<Item, ModuleItem> INFRARED_MODULE = register("infrared_module", ModuleItem::new);
    public static final DeferredHolder<Item, ModuleItem> KEYPAD_MODULE = register("keypad_module", ModuleItem::new);
    public static final DeferredHolder<Item, ModuleItem> QUEUE_MODULE = register("queue_module", ModuleItem::new);
    public static final DeferredHolder<Item, ModuleItem> RANDOM_MODULE = register("random_module", ModuleItem::new);
    public static final DeferredHolder<Item, ModuleItem> RANDOM_ACCESS_MEMORY_MODULE = register("random_access_memory_module", ModuleItem::new);
    public static final DeferredHolder<Item, ReadOnlyMemoryModuleItem> READ_ONLY_MEMORY_MODULE = register("read_only_memory_module", ReadOnlyMemoryModuleItem::new);
    public static final DeferredHolder<Item, ModuleItem> REDSTONE_MODULE = register("redstone_module", ModuleItem::new);
    public static final DeferredHolder<Item, ModuleItem> SEQUENCER_MODULE = register("sequencer_module", ModuleItem::new);
    public static final DeferredHolder<Item, ModuleItem> SERIAL_PORT_MODULE = register("serial_port_module", ModuleItem::new);
    public static final DeferredHolder<Item, ModuleItem> STACK_MODULE = register("stack_module", ModuleItem::new);
    public static final DeferredHolder<Item, ModuleItem> TERMINAL_MODULE = register("terminal_module", ModuleItem::new);
    public static final DeferredHolder<Item, ModuleItem> TIMER_MODULE = register("timer_module", ModuleItem::new);

    // --------------------------------------------------------------------- //

    public static void initialize(IEventBus bus) {
        ITEMS.register(bus);
    }

    public static <T extends Item> boolean is(final ItemStack stack, final DeferredHolder<Item, T> item) {
        return is(stack, item.get());
    }

    public static <T extends Item> boolean is(final ItemStack stack, final T item) {
        return !stack.isEmpty() && stack.getItem() == item;
    }

    // --------------------------------------------------------------------- //

    private static DeferredHolder<Item, Item> register(final String name) {
        return register(name, ModItem::new);
    }

    private static <T extends Item> DeferredHolder<Item, T> register(final String name, final Function<Item.Properties, T> factory) {
        return ITEMS.register(name, registryName ->
            factory.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, registryName)))
        );
    }

    private static <T extends Block> DeferredHolder<Item, Item> register(final DeferredHolder<Block, T> block) {
        return register(block, ModBlockItem::new);
    }

    private static <TBlock extends Block, TItem extends Item> DeferredHolder<Item, TItem> register(final DeferredHolder<Block, TBlock> block, final BiFunction<TBlock, Item.Properties, TItem> factory) {
        return register(block.getId().getPath(), p -> factory.apply(block.get(), p));
    }
}
