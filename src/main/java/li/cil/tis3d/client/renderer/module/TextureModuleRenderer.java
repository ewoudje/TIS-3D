package li.cil.tis3d.client.renderer.module;

import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.ModuleRenderer;
import li.cil.tis3d.api.util.ModuleRenderContext;
import li.cil.tis3d.client.models.ModuleModelData;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.OptionalInt;

public class TextureModuleRenderer<T extends Module> implements ModuleRenderer<T> {
    private final Identifier texture;
    private final Class<? extends T> moduleClass;

    public TextureModuleRenderer(Identifier texture, Class<? extends T> moduleClass) {
        this.texture = texture;
        this.moduleClass = moduleClass;
    }

    @Override
    public boolean matches(Module module) {
        return moduleClass.isInstance(module);
    }

    @Override
    public void render(final T module, final ModuleRenderContext context) {
        if (!module.getCasing().isEnabled()) {
            return;
        }

        context.drawAtlasQuadLit(texture);
    }

    @Override
    public @Nullable ModuleModelData getModelData(@Nullable Level level, BlockPos blockPos, BlockState blockState, T module) {
        return ModuleModelData.CLASSIC;
    }

    @Override
    public OptionalInt getTintColor(@Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex, T module) {
        return OptionalInt.empty();
    }
}
