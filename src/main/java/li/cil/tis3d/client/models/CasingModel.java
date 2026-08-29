package li.cil.tis3d.client.models;

import com.mojang.serialization.MapCodec;
import li.cil.tis3d.api.machine.Face;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.SingleVariant;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CasingModel implements DynamicBlockStateModel {
    private static BlockStateModel classicModel;

    private final BlockStateModel emptySide;

    public CasingModel(BlockStateModel emptySide) {
        this.emptySide = emptySide;
    }

    public static BlockStateModel getClassicModuleModel() {
        if (classicModel == null) {
            classicModel = Minecraft.getInstance().getModelManager().getStandaloneModel(ModModels.EMPTY_CASING);
        }

        return classicModel;
    }

    @Override
    public @Nullable Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return level.getModelData(pos);
    }

    @Override
    public void collectParts(BlockAndTintGetter blockAndTintGetter, BlockPos blockPos, BlockState blockState, RandomSource randomSource, List<BlockModelPart> list) {
        var data = blockAndTintGetter.getModelData(blockPos);
        var moduleDatas = data.get(ModModels.MODULE_MODEL_DATA_PROPERTY);
        if (moduleDatas == null) return;

        for (Face face : Face.VALUES) {
            var moduleData = moduleDatas[face.ordinal()];
            var parts = new ArrayList<BlockModelPart>();
            if (moduleData != null) {
                moduleData.collectParts(blockAndTintGetter, blockPos, blockState, randomSource, parts);
            } else {
                emptySide.collectParts(blockAndTintGetter, blockPos, blockState, randomSource, parts);
            }

            for (var part : parts) {
                list.add(new FilteredBlockModelPart(part, Face.toDirection(face)));
            }
        }
    }

    @Override
    public TextureAtlasSprite particleIcon() {
        return emptySide.particleIcon();
    }

    public static class Unbaked implements CustomUnbakedBlockStateModel {
        public static final Unbaked INSTANCE = new Unbaked();
        private BlockStateModel.Unbaked emptySide = new SingleVariant.Unbaked(new Variant(ModModels.EMPTY_CASING_ID));
        private CasingModel model;


        @Override
        public MapCodec<Unbaked> codec() {
            return MapCodec.unit(INSTANCE);
        }

        @Override
        public BlockStateModel bake(ModelBaker modelBaker) {
            if (model == null) {
                model = new CasingModel(emptySide.bake(modelBaker));
            }
            return model;
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            emptySide.resolveDependencies(resolver);
        }
    }
}
