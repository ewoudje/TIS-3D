package li.cil.tis3d.client.renderer.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.ModuleRenderer;
import li.cil.tis3d.client.renderer.ModTextures;
import li.cil.tis3d.client.renderer.RenderModuleState;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import li.cil.tis3d.common.item.Items;
import li.cil.tis3d.common.network.Network;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * Tile entity renderer for casings, used to dynamically render stuff for
 * different modules (in particular to allow dynamic displayed content, but
 * also so as not to spam the model registry with potentially a gazillion
 * block states for static individual texturing).
 */
public final class CasingBlockEntityRenderer implements BlockEntityRenderer<CasingBlockEntity, RenderModuleState> {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final double Z_FIGHT_BUFFER = 0.001;
    private static final Vector3f AXIS_X_POSITIVE = new Vector3f(1, 0, 0);
    private static final Vector3f AXIS_Y_POSITIVE = new Vector3f(0, 1, 0);
    private static final Vector3f AXIS_Z_POSITIVE = new Vector3f(0, 0, 1);
    private final static Set<Class<?>> BLACKLIST = new HashSet<>();
    private static final int DETAIL_RENDER_RANGE = 8;
    private static final int KINDA_CLOSE_RANGE = 16;

    public CasingBlockEntityRenderer(final BlockEntityRendererProvider.Context context) {

    }

    public static void update(CasingBlockEntity blockEntity) {
        if (blockEntity.getLevel() instanceof ClientLevel level) {
            blockEntity.requestModelDataUpdate();
            level.sendBlockUpdated(blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity.getBlockState(), 3);
        }
    }

    @Override
    public int getViewDistance() {
        return Network.RANGE_HIGH;
    }

    @Override
    public RenderModuleState createRenderState() {
        return new RenderModuleState();
    }


    @Override
    public void extractRenderState(CasingBlockEntity blockEntity, RenderModuleState renderState, float partialTick, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderState.extractBase(blockEntity, renderState, breakProgress);
        double distance = cameraPosition.distanceToSqr(blockEntity.getBlockPos().getCenter());

        renderState.partialTick = partialTick;
        renderState.isHoldingKey = isHoldingKey();
        renderState.isLocked = blockEntity.isLocked();
        renderState.isSneaking = Minecraft.getInstance().player.isShiftKeyDown();
        renderState.hitResult = Minecraft.getInstance().hitResult;
        renderState.isCloseEnoughForDetails = distance < DETAIL_RENDER_RANGE * DETAIL_RENDER_RANGE;
        renderState.isKindaClose = distance < KINDA_CLOSE_RANGE * KINDA_CLOSE_RANGE;
        renderState.gameTime = blockEntity.getLevel().getGameTime();

        for (Face f : Face.VALUES) {
            var module = renderState.modules[f.ordinal()] = blockEntity.getModule(f);
            if (module != null) {
                renderState.renderers[f.ordinal()] = ModuleRenderer.findRenderer(module);
                renderState.light[f.ordinal()] = LevelRenderer.getLightColor(blockEntity.getLevel(), blockEntity.getBlockPos());

                for (final Port port : Port.VALUES) {
                    renderState.isPipeLocked[f.ordinal()][port.ordinal()] = blockEntity.isReceivingPipeLocked(f, port);
                }
            }
        }
    }

    @Override
    public void submit(RenderModuleState renderState, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        renderState.collector = collector;
        renderState.camera = cameraRenderState;
        renderState.overlay = OverlayTexture.NO_OVERLAY;

        // Render all modules, adjust matrix stack to allow easily rendering an overlay in (0, 0, 0) to (1, 1, 0).
        for (final Face face : Face.VALUES) {
            if (isBackFace(cameraRenderState.pos, renderState.blockPos, face)) continue;
            if (renderState.modules[face.ordinal()] == null) continue;

            poseStack.pushPose();
            setupMatrix(face, poseStack);

            renderState.currentFace = face;
            renderState.matrixStack = poseStack;

            if (!renderState.isHoldingKey || !ConfigOverlayRenderer.drawConfigOverlay(renderState)) {
                drawModuleOverlay(renderState);
            }

            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private boolean isBackFace(final Vec3 cameraPosition, final BlockPos position, final Face face) {
        final Vec3 faceNormal = Vec3.atLowerCornerOf(Face.toDirection(face).getUnitVec3i());
        final Vec3 faceCenter = faceNormal.scale(0.5).add(position.getCenter());
        final Vec3 cameraToFaceCenter = faceCenter.subtract(cameraPosition);
        return faceNormal.dot(cameraToFaceCenter) > 0;
    }

    private void setupMatrix(final Face face, final PoseStack matrixStack) {
        final Vector3f axis;
        final int degree;

        switch (face) {
            case Y_NEG -> {
                axis = AXIS_X_POSITIVE;
                degree = -90;
            }
            case Y_POS -> {
                axis = AXIS_X_POSITIVE;
                degree = 90;
            }
            case Z_NEG -> {
                axis = AXIS_Y_POSITIVE;
                degree = 0;
            }
            case Z_POS -> {
                axis = AXIS_Y_POSITIVE;
                degree = 180;
            }
            case X_NEG -> {
                axis = AXIS_Y_POSITIVE;
                degree = 90;
            }
            case X_POS -> {
                axis = AXIS_Y_POSITIVE;
                degree = -90;
            }
            default -> throw new IllegalArgumentException("Invalid face");
        }

        matrixStack.mulPose(new Quaternionf().fromAxisAngleDeg(axis, degree));
        matrixStack.translate(0.5, 0.5, -(0.5 + Z_FIGHT_BUFFER));
        matrixStack.scale(-1, -1, 1);
    }

    private void drawModuleOverlay(final RenderModuleState state) {
        final PoseStack matrixStack = state.getMatrixStack();
        matrixStack.pushPose();
        for (final Port port : Port.CLOCKWISE) {
            final boolean isClosed = state.isReceivingPipeLocked(port);
            if (isClosed) {
                state.drawAtlasQuadUnlit(ModTextures.LOCATION_OVERLAY_CASING_PORT_CLOSED_SMALL);
            }

            matrixStack.translate(0.5, 0.5, 0.5);
            matrixStack.mulPose(new Quaternionf().fromAxisAngleDeg(AXIS_Z_POSITIVE, 90));
            matrixStack.translate(-0.5, -0.5, -0.5);
        }
        matrixStack.popPose();

        final Module module = state.modules[state.currentFace.ordinal()];
        if (module == null) {
            return;
        }

        if (BLACKLIST.contains(module.getClass())) {
            return;
        }

        try {
            ((ModuleRenderer) state.renderers[state.currentFace.ordinal()]).render(module, state);
        } catch (final Exception e) {
            BLACKLIST.add(module.getClass());
            LOGGER.error("A module threw an exception while rendering, won't render again!", e);
        }
    }

    private boolean isHoldingKey() {
        for (InteractionHand hand : InteractionHand.values()) {
            final ItemStack stack = Minecraft.getInstance().player.getItemInHand(hand);
            if (Items.is(stack, Items.KEY) || Items.is(stack, Items.KEY_CREATIVE)) {
                return true;
            }
        }

        return false;
    }
}
