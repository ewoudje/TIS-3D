package li.cil.tis3d.client.renderer.module;

import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.ModuleRenderer;
import li.cil.tis3d.api.util.ModuleRenderContext;
import li.cil.tis3d.client.models.ModuleModelData;
import li.cil.tis3d.common.module.FacadeModule;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class FacadeModuleRenderer implements ModuleRenderer<FacadeModule> {

    @Override
    public boolean matches(Module module) {
        return module instanceof FacadeModule;
    }

    @Override
    public void render(final FacadeModule module, final ModuleRenderContext context) {

    }

    @Override
    public @Nullable ModuleModelData getModelData(@Nullable Level level, BlockPos blockPos, BlockState blockState, FacadeModule module) {
        BlockState facade = module.getFacadeState();
        if (facade == null) facade = Blocks.IRON_BLOCK.defaultBlockState();

        var model = Minecraft.getInstance().getBlockRenderer().getBlockModel(facade);
        return model::collectParts;
    }
}
