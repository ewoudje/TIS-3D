package li.cil.tis3d.client.renderer.module;

import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.ModuleRenderer;
import li.cil.tis3d.api.util.ModuleRenderContext;
import li.cil.tis3d.client.models.ModuleModelData;
import li.cil.tis3d.client.models.TypedBlockModelPart;
import li.cil.tis3d.common.module.FacadeModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

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

        final var renderType = ItemBlockRenderTypes.getChunkRenderType(facade);
        final var model = Minecraft.getInstance().getBlockRenderer().getBlockModel(facade);

        return (blockAndTintGetter, blockPos1, blockState1, randomSource, list) -> {
            List<BlockModelPart> writeList = new ArrayList<>();
            model.collectParts(blockAndTintGetter, blockPos1, blockState1, randomSource, writeList);

            for (BlockModelPart part : writeList) {
                list.add(new TypedBlockModelPart(part, renderType));
            }
        };
    }

    @Override
    public OptionalInt getTintColor(@Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex, FacadeModule module) {
        BlockState facade = module.getFacadeState();
        if (facade == null) facade = Blocks.IRON_BLOCK.defaultBlockState();

        return OptionalInt.of(Minecraft.getInstance().getBlockColors().getColor(facade, level, pos, tintIndex));
    }
}
