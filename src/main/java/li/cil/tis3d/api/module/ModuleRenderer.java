package li.cil.tis3d.api.module;

import li.cil.tis3d.api.API;
import li.cil.tis3d.api.util.ModuleRenderContext;
import li.cil.tis3d.client.models.ModuleModelData;
import li.cil.tis3d.util.RegistryUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.OptionalInt;
import java.util.WeakHashMap;

public interface ModuleRenderer<T extends Module> {
    //TODO should be changed to the state approach that is now used

    /**
     * The registry name of the registry holding module renderers.
     */
    ResourceKey<Registry<ModuleRenderer<?>>> REGISTRY = ResourceKey.createRegistryKey(API.resource("module_renderer"));

    /**
     * Checks whether the provider supports the specified module.
     * If it is supported the module will be permanently associated with this renderer.
     *
     * @param module to check for.
     * @return <tt>true</tt> if the module is supported, <tt>false</tt> otherwise.
     */
    boolean matches(final Module module);

    /**
     * Called to allow the module to render dynamic content on the casing it
     * is installed in.
     * <p>
     * The render state will be adjusted to take into account the face the
     * module is installed in, i.e. rendering from (0, 0, 0) to (1, 1, 0) will
     * render the full quad of face of the casing the module is installed in.
     *
     * @param module  the module to render for.
     * @param context the current render context.
     */
    void render(final T module, final ModuleRenderContext context);

    @Nullable ModuleModelData getModelData(@Nullable Level level, BlockPos blockPos, BlockState blockState, T module);

    OptionalInt getTintColor(@Nullable final BlockAndTintGetter level, @Nullable final BlockPos pos, final int tintIndex, T module);

    WeakHashMap<Module, ModuleRenderer<?>> CACHED_RENDERERS = new WeakHashMap<>();
    static ModuleRenderer<Module> findRenderer(final Module module) {
        return (ModuleRenderer<Module>) CACHED_RENDERERS.computeIfAbsent(module, m ->
            RegistryUtils.get(ModuleRenderer.REGISTRY).stream()
                .filter(r -> r.matches(m))
                .findAny()
                .orElseThrow());
    }
}
