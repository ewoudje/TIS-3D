package li.cil.tis3d.client.models;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class FilteredBlockModelPart implements BlockModelPart {
    private final BlockModelPart base;
    private final Direction forDirection;

    public FilteredBlockModelPart(BlockModelPart base, Direction forDirection) {
        this.base = base;
        this.forDirection = forDirection;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable Direction direction) {
        if (direction == null || direction == forDirection) {
            return base.getQuads(direction);
        }

        return List.of();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return base.useAmbientOcclusion();
    }

    @Override
    public TextureAtlasSprite particleIcon() {
        return base.particleIcon();
    }

    @Override
    public ChunkSectionLayer getRenderType(BlockState state) {
        return base.getRenderType(state);
    }
}
