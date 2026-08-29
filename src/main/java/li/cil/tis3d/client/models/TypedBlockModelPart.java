package li.cil.tis3d.client.models;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class TypedBlockModelPart implements BlockModelPart {
    private final BlockModelPart base;
    private final ChunkSectionLayer renderType;

    public TypedBlockModelPart(BlockModelPart base, ChunkSectionLayer renderType) {
        this.base = base;
        this.renderType = renderType;
    }

    @Override
    public ChunkSectionLayer getRenderType(BlockState state) {
        return renderType;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable Direction direction) {
        return base.getQuads(direction);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return base.useAmbientOcclusion();
    }

    @Override
    public TextureAtlasSprite particleIcon() {
        return base.particleIcon();
    }
}
