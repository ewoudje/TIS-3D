package li.cil.tis3d.common.item;

import li.cil.tis3d.api.API;
import li.cil.tis3d.util.RegistryUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Map;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = RegistryUtils.getDeferred(Registries.CREATIVE_MODE_TAB);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> COMMON = TABS.register("common", () ->
        CreativeModeTab.builder()
            .icon(() -> new ItemStack(Items.CONTROLLER.get()))
            .title(Component.translatable("itemGroup.tis3d.common"))
            .displayItems((parameters, output) -> {
                BuiltInRegistries.ITEM.entrySet().stream()
                    .filter(entry -> entry.getKey().identifier().getNamespace().equals(API.MOD_ID))
                    .map(Map.Entry::getValue)
                    .forEach(item -> output.accept(new ItemStack(item)));
            }).build());

    public static void initialize(IEventBus bus) {
        TABS.register(bus);
    }
}
