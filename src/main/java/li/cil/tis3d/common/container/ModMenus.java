package li.cil.tis3d.common.container;

import li.cil.tis3d.util.RegistryUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = RegistryUtils.getDeferred(Registries.MENU);

    // --------------------------------------------------------------------- //

    public static final DeferredHolder<MenuType<?>, MenuType<ReadOnlyMemoryModuleMenu>> READ_ONLY_MEMORY_MODULE = MENU_TYPES.register(
        "read_only_memory_module",
        () -> IMenuTypeExtension.create(ReadOnlyMemoryModuleMenu::create)
    );

    // --------------------------------------------------------------------- //

    public static void initialize(IEventBus bus) {
        MENU_TYPES.register(bus);
    }
}
