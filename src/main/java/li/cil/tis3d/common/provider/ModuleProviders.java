package li.cil.tis3d.common.provider;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.ModuleProvider;
import li.cil.tis3d.common.item.Items;
import li.cil.tis3d.common.item.ModuleItem;
import li.cil.tis3d.common.module.AudioModule;
import li.cil.tis3d.common.module.DisplayModule;
import li.cil.tis3d.common.module.ExecutionModule;
import li.cil.tis3d.common.module.FacadeModule;
import li.cil.tis3d.common.module.InfraredModule;
import li.cil.tis3d.common.module.KeypadModule;
import li.cil.tis3d.common.module.QueueModule;
import li.cil.tis3d.common.module.RandomAccessMemoryModule;
import li.cil.tis3d.common.module.RandomModule;
import li.cil.tis3d.common.module.ReadOnlyMemoryModule;
import li.cil.tis3d.common.module.RedstoneModule;
import li.cil.tis3d.common.module.SequencerModule;
import li.cil.tis3d.common.module.SerialPortModule;
import li.cil.tis3d.common.module.StackModule;
import li.cil.tis3d.common.module.TerminalModule;
import li.cil.tis3d.common.module.TimerModule;
import li.cil.tis3d.common.provider.module.SimpleModuleProvider;
import li.cil.tis3d.util.RegistryUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Optional;
import java.util.function.BiFunction;

public final class ModuleProviders {
    private static final DeferredRegister<ModuleProvider> MODULE_PROVIDERS = RegistryUtils.getDeferred(ModuleProvider.REGISTRY);

    // --------------------------------------------------------------------- //

    public static void initialize(IEventBus eventBus) {
        RegistryUtils.builder(ModuleProvider.REGISTRY);

        register(Items.AUDIO_MODULE, AudioModule::new);
        register(Items.DISPLAY_MODULE, DisplayModule::new);
        register(Items.EXECUTION_MODULE, ExecutionModule::new);
        register(Items.FACADE_MODULE, FacadeModule::new);
        register(Items.INFRARED_MODULE, InfraredModule::new);
        register(Items.KEYPAD_MODULE, KeypadModule::new);
        register(Items.QUEUE_MODULE, QueueModule::new);
        register(Items.RANDOM_MODULE, RandomModule::new);
        register(Items.RANDOM_ACCESS_MEMORY_MODULE, RandomAccessMemoryModule::new);
        register(Items.READ_ONLY_MEMORY_MODULE, ReadOnlyMemoryModule::new);
        register(Items.REDSTONE_MODULE, RedstoneModule::new);
        register(Items.SEQUENCER_MODULE, SequencerModule::new);
        register(Items.SERIAL_PORT_MODULE, SerialPortModule::new);
        register(Items.STACK_MODULE, StackModule::new);
        register(Items.TERMINAL_MODULE, TerminalModule::new);
        register(Items.TIMER_MODULE, TimerModule::new);

        MODULE_PROVIDERS.register(eventBus);
    }

    public static Optional<ModuleProvider> getProviderFor(final ItemStack stack, final Casing casing, final Face face) {
        for (final ModuleProvider provider : RegistryUtils.get(ModuleProvider.REGISTRY)) {
            if (provider.matches(stack, casing, face)) {
                return Optional.of(provider);
            }
        }
        return Optional.empty();
    }

    // --------------------------------------------------------------------- //

    private static <T extends Module> void register(final DeferredHolder<Item, ? extends ModuleItem> item, final BiFunction<Casing, Face, T> factory) {
        MODULE_PROVIDERS.register(item.getId().getPath(), () -> new SimpleModuleProvider<>(item, factory));
    }
}
