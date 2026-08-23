package li.cil.tis3d.client.renderer.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import li.cil.tis3d.common.block.entity.ControllerBlockEntity;
import li.cil.tis3d.util.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class ControllerBlockEntityRenderer implements BlockEntityRenderer<ControllerBlockEntity, ControllerBlockEntityRenderer.RenderState> {
    private final BlockEntityRenderDispatcher renderer;
    private final Font font;

    public ControllerBlockEntityRenderer(final BlockEntityRendererProvider.Context context) {
        renderer = context.blockEntityRenderDispatcher();
        font = context.font();
    }

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(ControllerBlockEntity blockEntity, RenderState renderState, float partialTick, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderState.extractBase(blockEntity, renderState, breakProgress);
        renderState.state = blockEntity.getState();
    }

    @Override
    public void submit(RenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        if (!renderState.state.isError) {
            return;
        }

        final HitResult hit = Minecraft.getInstance().hitResult;
        if (hit instanceof final BlockHitResult blockHit) {
            if (Objects.equals(blockHit.getBlockPos(), renderState.blockPos)) {
                renderState(poseStack, cameraRenderState, submitNodeCollector, renderState.state);
            }
        }
    }

    private void renderState(final PoseStack matrixStack, CameraRenderState camera, final SubmitNodeCollector collector, final ControllerBlockEntity.ControllerState state) {
        matrixStack.pushPose();
        matrixStack.translate(0.5, 1.4, 0.5);
        matrixStack.mulPose(camera.orientation);
        matrixStack.scale(-0.025f, -0.025f, 0.025f);

        final Component message = state.message;
        final float x = -font.width(message) / 2.0f;
        final var matrix = matrixStack.last().pose();
        final int backgroundColor = Minecraft.getInstance().options.getBackgroundColor(0.25f);
        final int maxBrightness = LightTexture.pack(0xF, 0xF);

        MultiBufferSource bufferFactory = Minecraft.getInstance().renderBuffers().bufferSource();

        font.drawInBatch(message, x, 0, Color.withAlpha(Color.WHITE, 0.125f), false, matrix, bufferFactory, Font.DisplayMode.SEE_THROUGH, backgroundColor, maxBrightness);
        font.drawInBatch(message, x, 0, Color.WHITE, false, matrix, bufferFactory, Font.DisplayMode.NORMAL, 0, maxBrightness);

        matrixStack.popPose();
    }


    public static class RenderState extends BlockEntityRenderState {
        public ControllerBlockEntity.ControllerState state;
    }
}
