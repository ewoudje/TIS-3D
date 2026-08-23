package li.cil.tis3d.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import li.cil.manual.api.render.FontRenderer;
import li.cil.tis3d.api.util.RenderContext;
import li.cil.tis3d.util.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import org.joml.Vector3f;

public final class RenderContextImpl implements RenderContext {
    private static final int DETAIL_RENDER_RANGE = 8;

    private final BlockEntityRenderDispatcher dispatcher;
    private final PoseStack matrixStack;
    private final MultiBufferSource buffer;
    private final float partialTicks;
    private final int light;
    private final int overlay;

    // --------------------------------------------------------------------- //

    public RenderContextImpl(final BlockEntityRenderDispatcher dispatcher, final PoseStack matrixStack,
                             final MultiBufferSource buffer, final float partialTicks,
                             final int light, final int overlay) {
        this.dispatcher = dispatcher;
        this.matrixStack = matrixStack;
        this.buffer = buffer;
        this.partialTicks = partialTicks;
        this.light = light;
        this.overlay = overlay;
    }

    public RenderContextImpl(final RenderContextImpl other, final int light) {
        this(other.dispatcher, other.matrixStack, other.buffer, other.partialTicks, light, other.overlay);
    }

    // --------------------------------------------------------------------- //

    private static TextureAtlasSprite getSprite(final Identifier location) {
        return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).getSprite(location);
    }

    @Override
    public BlockEntityRenderDispatcher getDispatcher() {
        return dispatcher;
    }

    @Override
    public PoseStack getMatrixStack() {
        return matrixStack;
    }

    @Override
    public float getPartialTicks() {
        return partialTicks;
    }

    @Override
    public MultiBufferSource getBuffer() {
        return buffer;
    }

    @Override
    public boolean closeEnoughForDetails(final BlockPos position) {
        //TODO
        return 0 < DETAIL_RENDER_RANGE * DETAIL_RENDER_RANGE;
    }

    @Override
    public void drawString(final FontRenderer fontRenderer, final CharSequence value, final int argb) {
        fontRenderer.drawInBatch(value, argb, matrixStack.last().pose(), buffer);
    }

    @Override
    public void drawAtlasQuadLit(final Identifier location) {
        final VertexConsumer builder = buffer.getBuffer(RenderTypes.entityTranslucent(location));
        drawAtlasQuad(builder, getSprite(location), 0, 0, 1, 1, 0, 0, 1, 1, Color.WHITE);
    }

    @Override
    public void drawAtlasQuadUnlit(final Identifier location) {
        drawAtlasQuadUnlit(location, 0, 0, 1, 1, 0, 0, 1, 1, Color.WHITE);
    }

    @Override
    public void drawAtlasQuadUnlit(final Identifier location,
                                   final float x, final float y, final float width, final float height,
                                   final float u0, final float v0, final float u1, final float v1,
                                   final int argb) {
        final VertexConsumer builder = buffer.getBuffer(ModRenderTypes.unlitAtlasTexture());
        drawAtlasQuad(builder, getSprite(location), x, y, width, height, u0, v0, u1, v1, argb);
    }

    @Override
    public void drawQuadUnlit(final float x, final float y, final float width, final float height, final int argb) {
        final VertexConsumer builder = buffer.getBuffer(ModRenderTypes.unlit());
        drawQuad(builder, x, y, width, height, 0, 0, 1, 1, argb);
    }

    @Override
    public void drawQuad(final VertexConsumer builder, final float x, final float y, final float width, final float height) {
        drawQuad(builder, x, y, width, height, Color.WHITE);
    }

    // --------------------------------------------------------------------- //

    @Override
    public void drawQuad(final VertexConsumer builder,
                         final float x, final float y, final float width, final float height,
                         final float u0, final float v0, final float u1, final float v1,
                         final int argb) {
        final var pose = getMatrixStack().last();
        final var up = new Vector3f(0, 0, -1);

        builder.addVertex(pose, x, y + height, 0)
            .setColor(argb)
            .setUv(u0, v1)
            .setOverlay(overlay)
            .setLight(light)
            .setNormal(pose, up.x(), up.y(), up.z());

        builder.addVertex(pose, x + width, y + height, 0)
            .setColor(argb)
            .setUv(u1, v1)
            .setOverlay(overlay)
            .setLight(light)
            .setNormal(pose, up.x(), up.y(), up.z());

        builder.addVertex(pose, x + width, y, 0)
            .setColor(argb)
            .setUv(u1, v0)
            .setOverlay(overlay)
            .setLight(light)
            .setNormal(pose, up.x(), up.y(), up.z());

        builder.addVertex(pose, x, y, 0)
            .setColor(argb)
            .setUv(u0, v0)
            .setOverlay(overlay)
            .setLight(light)
            .setNormal(pose, up.x(), up.y(), up.z());
    }
}
