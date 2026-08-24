package li.cil.tis3d.client.renderer;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import li.cil.tis3d.api.API;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;

public abstract class ModRenderTypes {
    // Why no work
    private static final RenderType UNLIT_ATLAS_TEXTURE = RenderType.create("atlas_module_overlay",
        RenderSetup.builder(ModRenderPipelines.MODULE_TEXTURED_OVERLAY)
            .withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS)
            .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
            .affectsCrumbling()
            .createRenderSetup());

    private static final RenderType UNLIT = RenderType.create("color_module_overlay",
        RenderSetup.builder(ModRenderPipelines.MODULE_COLORED_OVERLAY)
            .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
            .affectsCrumbling()
            .createRenderSetup());

    private static final RenderType LIT_ATLAS_TEXTURE = RenderType.create("lit_atlas_module_overlay",
        RenderSetup.builder(ModRenderPipelines.MODULE_TEXTURED_OVERLAY)
            .withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS)
            //TODO should use lightmap
            .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
            .affectsCrumbling()
            .createRenderSetup());

    // --------------------------------------------------------------------- //

    private ModRenderTypes() {
        throw new UnsupportedOperationException("Not meant to be instantiated.");
    }

    /**
     * Render layer intended for modules, intended for rendering layered, transparent overlays.
     * As such, depth write is disabled in this layer. Textures must be in the block texture
     * atlas.
     *
     * @return the {@link RenderType} instance.
     */
    public static RenderType unlit() {
        return UNLIT;
    }

    /**
     * Render layer intended for modules, intended for rendering layered, transparent overlays.
     * As such, depth write is disabled in this layer. Textures must be in the block texture
     * atlas.
     *
     * @return the {@link RenderType} instance.
     */
    public static RenderType unlitAtlasTexture() {
        return UNLIT_ATLAS_TEXTURE;
    }

    public static RenderType litAtlasTexture() {
        return LIT_ATLAS_TEXTURE;
    }

    // --------------------------------------------------------------------- //

    /**
     * Create a render layer that is identical to {@link RenderTypes#entityCutout(Identifier)},
     * except with diffuse lighting disabled.
     *
     * @param texture the id of the texture to be bound.
     * @return the {@link RenderType} instance.
     */
    public static RenderType unlitTexture(final Identifier texture) {
        return RenderType.create("texture_module_overlay",
            RenderSetup.builder(ModRenderPipelines.MODULE_TEXTURED_OVERLAY)
                .withTexture("Sampler0", texture)
                .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
                .affectsCrumbling()
                .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
                .createRenderSetup());
    }
}
