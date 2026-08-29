package li.cil.tis3d.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import li.cil.manual.api.render.FontRenderer;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.ModuleRenderer;
import li.cil.tis3d.api.util.ModuleRenderContext;
import li.cil.tis3d.util.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class RenderModuleState extends BlockEntityRenderState implements ModuleRenderContext {
    public BlockEntityRenderDispatcher dispatcher;
    public PoseStack matrixStack;
    public SubmitNodeCollector collector;
    public CameraRenderState camera;
    public float partialTick;
    public int overlay;

    public Module[] modules = new Module[6];
    public ModuleRenderer<?>[] renderers = new ModuleRenderer<?>[6];
    public int[] light = new int[6];
    public boolean isSneaking;
    public boolean isLocked;
    public boolean isHoldingKey;
    public boolean isKindaClose;
    public boolean isCloseEnoughForDetails;
    public boolean[][] isPipeLocked = new boolean[6][4];
    public long gameTime;
    public HitResult hitResult;
    public Face currentFace;


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
        return partialTick;
    }

    @Override
    public boolean isSneaking() {
        return isSneaking;
    }

    @Override
    public boolean isLocked() {
        return isLocked;
    }

    @Override
    public boolean isReceivingPipeLocked(Port port) {
        return isPipeLocked[currentFace.ordinal()][port.ordinal()];
    }

    @Override
    public long gameTime() {
        return gameTime;
    }

    @Override
    public HitResult getHitResult() {
        return hitResult;
    }

    @Override
    public Face getFace() {
        return currentFace;
    }

    @Override
    public boolean isLookingAt() {
        if (hitResult instanceof BlockHitResult b) {
            return b.getDirection() == Face.toDirection(currentFace) && b.getBlockPos().equals(this.blockPos);
        }

        return false;
    }

    @Override
    public boolean isKindaClose() {
        return isKindaClose;
    }

    @Override
    public boolean closeEnoughForDetails() {
        return isCloseEnoughForDetails;
    }

    @Override
    public void drawString(final FontRenderer fontRenderer, final CharSequence value, final int argb) {
        fontRenderer.drawInBatch(value, argb, matrixStack, collector);
    }

    @Override
    public void drawAtlasQuadLit(final Identifier location) {
        drawAtlasQuad(ModRenderTypes.litAtlasTexture(), getSprite(location), 0, 0, 1, 1, 0, 0, 1, 1, Color.WHITE);
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
        drawAtlasQuad(ModRenderTypes.unlitAtlasTexture(), getSprite(location), x, y, width, height, u0, v0, u1, v1, argb);
    }

    @Override
    public void drawQuadUnlit(final float x, final float y, final float width, final float height, final int argb) {
        drawQuad(ModRenderTypes.unlit(), x, y, width, height, 0, 0, 1, 1, argb);
    }

    @Override
    public void drawQuad(final RenderType type, final float x, final float y, final float width, final float height) {
        drawQuad(type, x, y, width, height, Color.WHITE);
    }

    // --------------------------------------------------------------------- //

    @Override
    public void drawQuad(final RenderType type,
                         final float x, final float y, final float width, final float height,
                         final float u0, final float v0, final float u1, final float v1,
                         final int argb) {
        final int quadLight = light[currentFace.ordinal()];

        collector.submitCustomGeometry(getMatrixStack(), type, (p, vertices) -> {
            vertices.addVertex(p, x, y + height, 0)
                .setColor(argb)
                .setUv(u0, v1)
                .setOverlay(overlay)
                .setLight(quadLight)
                .setNormal(p, 0, 0, -1);

            vertices.addVertex(p, x + width, y + height, 0)
                .setColor(argb)
                .setUv(u1, v1)
                .setOverlay(overlay)
                .setLight(quadLight)
                .setNormal(p, 0, 0, -1);

            vertices.addVertex(p, x + width, y, 0)
                .setColor(argb)
                .setUv(u1, v0)
                .setOverlay(overlay)
                .setLight(quadLight)
                .setNormal(p, 0, 0, -1);

            vertices.addVertex(p, x, y, 0)
                .setColor(argb)
                .setUv(u0, v0)
                .setOverlay(overlay)
                .setLight(quadLight)
                .setNormal(p, 0, 0, -1);
        });
    }
}
