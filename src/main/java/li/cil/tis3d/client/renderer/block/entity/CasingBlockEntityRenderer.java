package li.cil.tis3d.client.renderer.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.ModuleRenderer;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import li.cil.tis3d.common.network.Network;
import li.cil.tis3d.util.RegistryUtils;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Tile entity renderer for casings, used to dynamically render stuff for
 * different modules (in particular to allow dynamic displayed content, but
 * also so as not to spam the model registry with potentially a gazillion
 * block states for static individual texturing).
 */
public final class CasingBlockEntityRenderer implements BlockEntityRenderer<CasingBlockEntity, CasingBlockEntityRenderer.RenderState> {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final double Z_FIGHT_BUFFER = 0.001;
    private static final Vector3f AXIS_X_POSITIVE = new Vector3f(1, 0, 0);
    private static final Vector3f AXIS_Y_POSITIVE = new Vector3f(0, 1, 0);
    private static final Vector3f AXIS_Z_POSITIVE = new Vector3f(0, 0, 1);
    private final static Set<Class<?>> BLACKLIST = new HashSet<>();
    private final static Map<Module, ModuleRenderer<?>> RENDERERS = new HashMap<>();
    private final BlockEntityRenderDispatcher renderer;

    public CasingBlockEntityRenderer(final BlockEntityRendererProvider.Context context) {
        renderer = context.blockEntityRenderDispatcher();
    }

    @Override
    public int getViewDistance() {
        return Network.RANGE_HIGH;
    }

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }


    @Override
    public void extractRenderState(CasingBlockEntity blockEntity, RenderState renderState, float partialTick, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderState.extractBase(blockEntity, renderState, breakProgress);

        for (Face f : Face.VALUES) {
            var module = renderState.modules[f.ordinal()] = blockEntity.getModule(f);
            if (module != null) {
                renderState.renderers[f.ordinal()] = findRenderer(module);
            }
        }
    }

    @Override
    public void submit(RenderState renderState, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        //TODO final RenderContextImpl context = new RenderContextImpl(renderer, poseStack, bufferFactory, partialTicks, light, overlay);

        // Render all modules, adjust matrix stack to allow easily rendering an overlay in (0, 0, 0) to (1, 1, 0).
        for (final Face face : Face.VALUES) {
            if (isBackFace(renderState.blockPos, face)) {
                continue;
            }

            poseStack.pushPose();
            setupMatrix(face, poseStack);

            //if (!isObserverHoldingKey() || !drawConfigOverlay(context, casing, face)) {
            // Grab neighbor lighting for module rendering because the casing itself is opaque and hence fully dark.
            //final BlockPos neighborPos = renderState.blockPos.relative(Face.toDirection(face));
            // TODO final int neighborLight = LevelRenderer.getLightColor(renderer.level, neighborPos);
            //drawModuleOverlay(new RenderContextImpl(context, neighborLight), casing, face);
            //}

            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private boolean isBackFace(final BlockPos position, final Face face) {

        final Vec3 cameraPosition = Vec3.ZERO; //TODO renderer.camera.getPosition();
        final Vec3 faceNormal = Vec3.atLowerCornerOf(Face.toDirection(face).getUnitVec3i()); //TODO wtf?
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

    //private boolean drawConfigOverlay(final RenderContext context, final CasingBlockEntity casing, final Face face) {
    //    // Only bother rendering the overlay if the player is nearby.
    //    if (!isObserverKindaClose(casing)) {
    //        return false;
    //    }
    //
    //    if (isObserverSneaking() && !casing.isLocked()) {
    //        final Identifier closedSprite;
    //        final Identifier openSprite;
    //
    //        final Port lookingAtPort;
    //        final boolean isLookingAt = isObserverLookingAt(casing.getPosition(), face);
    //        if (isLookingAt) {
    //            closedSprite = Textures.LOCATION_OVERLAY_CASING_PORT_CLOSED;
    //            openSprite = Textures.LOCATION_OVERLAY_CASING_PORT_OPEN;
    //
    //            final HitResult hit = null; //TODO renderer.cameraHitResult;
    //            assert hit.getType() == HitResult.Type.BLOCK : "renderer.cameraHitResult.getType() is not of type BLOCK even though it was in isObserverLookingAt";
    //            assert hit instanceof BlockHitResult : "renderer.cameraHitResult is not a BlockRayTraceResult even though it was in isObserverLookingAt";
    //            final BlockHitResult blockHit = (BlockHitResult) hit;
    //            final BlockPos pos = blockHit.getBlockPos();
    //            final Vec3 uv = TransformUtil.hitToUV(face, blockHit.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ()));
    //            lookingAtPort = Port.fromUVQuadrant(uv);
    //        } else {
    //            closedSprite = Textures.LOCATION_OVERLAY_CASING_PORT_CLOSED_SMALL;
    //            openSprite = null;
    //
    //            lookingAtPort = null;
    //        }
    //
    //        final PoseStack matrixStack = context.getMatrixStack();
    //        matrixStack.pushPose();
    //        for (final Port port : Port.CLOCKWISE) {
    //            final boolean isClosed = casing.isReceivingPipeLocked(face, port);
    //            final Identifier sprite = isClosed ? closedSprite : openSprite;
    //            if (sprite != null) {
    //                context.drawAtlasQuadUnlit(sprite);
    //            }
    //
    //            if (port == lookingAtPort) {
    //                context.drawAtlasQuadUnlit(Textures.LOCATION_OVERLAY_CASING_PORT_HIGHLIGHT);
    //            }
    //
    //            matrixStack.translate(0.5, 0.5, 0.5);
    //            matrixStack.mulPose(new Quaternionf().fromAxisAngleDeg(AXIS_Z_POSITIVE, 90));
    //            matrixStack.translate(-0.5, -0.5, -0.5);
    //        }
    //        matrixStack.popPose();
    //
    //        return isLookingAt;
    //    } else {
    //        final Identifier sprite;
    //        if (casing.isLocked()) {
    //            sprite = Textures.LOCATION_OVERLAY_CASING_LOCKED;
    //        } else {
    //            sprite = Textures.LOCATION_OVERLAY_CASING_UNLOCKED;
    //        }
    //
    //        context.drawAtlasQuadUnlit(sprite);
    //    }
    //
    //    return true;
    //}
    //
    //private void drawModuleOverlay(final RenderContext context, final CasingBlockEntity casing, final Face face) {
    //    final PoseStack matrixStack = context.getMatrixStack();
    //    matrixStack.pushPose();
    //    for (final Port port : Port.CLOCKWISE) {
    //        final boolean isClosed = casing.isReceivingPipeLocked(face, port);
    //        if (isClosed) {
    //            context.drawAtlasQuadUnlit(Textures.LOCATION_OVERLAY_CASING_PORT_CLOSED_SMALL);
    //        }
    //
    //        matrixStack.translate(0.5, 0.5, 0.5);
    //        matrixStack.mulPose(new Quaternionf().fromAxisAngleDeg(AXIS_Z_POSITIVE, 90));
    //        matrixStack.translate(-0.5, -0.5, -0.5);
    //    }
    //    matrixStack.popPose();
    //
    //    final Module module = casing.getModule(face);
    //    if (module == null) {
    //        return;
    //    }
    //    if (BLACKLIST.contains(module.getClass())) {
    //        return;
    //    }
    //
    //    try {
    //        findRenderer(module).render(module, context);
    //    } catch (final Exception e) {
    //        BLACKLIST.add(module.getClass());
    //        LOGGER.error("A module threw an exception while rendering, won't render again!", e);
    //    }
    //}
    //
    //private boolean isObserverKindaClose(final CasingBlockEntity casing) {
    //    return casing.getBlockPos().closerToCenterThan(renderer.camera.getPosition(), 16);
    //}
    //
    //private boolean isObserverHoldingKey() {
    //    if (renderer.camera.getEntity() instanceof LivingEntity le) {
    //        for (InteractionHand hand : InteractionHand.values()) {
    //            final ItemStack stack = le.getItemInHand(hand);
    //            if (Items.is(stack, Items.KEY) || Items.is(stack, Items.KEY_CREATIVE)) {
    //                return true;
    //            }
    //        }
    //    }
    //
    //    return false;
    //}
    //
    //private boolean isObserverSneaking() {
    //    return renderer.camera.getEntity().isShiftKeyDown();
    //}
    //
    //private boolean isObserverLookingAt(final BlockPos pos, final Face face) {
    //    final HitResult hit = renderer.cameraHitResult;
    //    if (!(hit instanceof final BlockHitResult blockHit)) {
    //        return false;
    //    }
    //
    //    if (Face.fromDirection(blockHit.getDirection()) != face) {
    //        return false;
    //    }
    //
    //    return Objects.equals(blockHit.getBlockPos(), pos);
    //}

    private ModuleRenderer<Module> findRenderer(final Module module) {
        return (ModuleRenderer<Module>) RENDERERS.computeIfAbsent(module, m ->
            RegistryUtils.get(ModuleRenderer.REGISTRY).stream()
                .filter(r -> r.matches(m))
                .findAny()
                .orElseThrow());
    }

    public static class RenderState extends BlockEntityRenderState {
        public Module[] modules = new Module[6];
        public ModuleRenderer<?>[] renderers = new ModuleRenderer<?>[6];
    }
}
