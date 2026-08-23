package li.cil.tis3d.client.renderer;

import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.ModuleRenderer;
import li.cil.tis3d.client.renderer.module.DisplayModuleRenderer;
import li.cil.tis3d.client.renderer.module.ExecutionModuleRenderer;
import li.cil.tis3d.client.renderer.module.FacadeModuleRenderer;
import li.cil.tis3d.client.renderer.module.KeypadModuleRenderer;
import li.cil.tis3d.client.renderer.module.QueueModuleRenderer;
import li.cil.tis3d.client.renderer.module.RandomAccessMemoryModuleRenderer;
import li.cil.tis3d.client.renderer.module.RedstoneModuleRenderer;
import li.cil.tis3d.client.renderer.module.SequencerModuleRenderer;
import li.cil.tis3d.client.renderer.module.StackModuleRenderer;
import li.cil.tis3d.client.renderer.module.TerminalModuleRenderer;
import li.cil.tis3d.client.renderer.module.TextureModuleRenderer;
import li.cil.tis3d.client.renderer.module.TimerModuleRenderer;
import li.cil.tis3d.common.module.AudioModule;
import li.cil.tis3d.common.module.InfraredModule;
import li.cil.tis3d.common.module.RandomModule;
import li.cil.tis3d.common.module.SerialPortModule;
import li.cil.tis3d.util.RegistryUtils;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModuleRenderers {
    private static final DeferredRegister<ModuleRenderer<?>> MODULE_RENDERERS = RegistryUtils.getDeferred(ModuleRenderer.REGISTRY);
    // --------------------------------------------------------------------- //

    public static void initialize(IEventBus eventBus) {
        RegistryUtils.builder(ModuleRenderer.REGISTRY);

        MODULE_RENDERERS.register("display", DisplayModuleRenderer::new);
        MODULE_RENDERERS.register("execution", ExecutionModuleRenderer::new);
        MODULE_RENDERERS.register("random_access_memory", RandomAccessMemoryModuleRenderer::new);
        MODULE_RENDERERS.register("facade", FacadeModuleRenderer::new);
        MODULE_RENDERERS.register("queue", QueueModuleRenderer::new);
        MODULE_RENDERERS.register("redstone", RedstoneModuleRenderer::new);
        MODULE_RENDERERS.register("sequencer", SequencerModuleRenderer::new);
        MODULE_RENDERERS.register("stack", StackModuleRenderer::new);
        MODULE_RENDERERS.register("terminal", TerminalModuleRenderer::new);
        MODULE_RENDERERS.register("timer", TimerModuleRenderer::new);
        MODULE_RENDERERS.register("keypad", KeypadModuleRenderer::new);

        registerTexture("audio", Textures.LOCATION_OVERLAY_MODULE_AUDIO, AudioModule.class);
        registerTexture("infrared", Textures.LOCATION_OVERLAY_MODULE_INFRARED, InfraredModule.class);
        registerTexture("random", Textures.LOCATION_OVERLAY_MODULE_RANDOM, RandomModule.class);
        registerTexture("serial_port", Textures.LOCATION_OVERLAY_MODULE_SERIAL_PORT, SerialPortModule.class);

        MODULE_RENDERERS.register(eventBus);
    }

    private static void registerTexture(String name, Identifier texture, Class<? extends Module> moduleClass) {
        MODULE_RENDERERS.register(name, () -> new TextureModuleRenderer<>(texture, moduleClass));
    }
}
