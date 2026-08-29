package li.cil.tis3d.client.renderer.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.util.ModuleRenderContext;
import li.cil.tis3d.api.util.TransformUtil;
import li.cil.tis3d.client.renderer.ModTextures;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ConfigOverlayRenderer {
    private static final Vector3f AXIS_Z_POSITIVE = new Vector3f(0, 0, 1);

    public static boolean drawConfigOverlay(final ModuleRenderContext context) {
        if (!context.isKindaClose()) return false;

        if (context.isSneaking() && !context.isLocked()) {
            final Identifier closedSprite;
            final Identifier openSprite;

            final Port lookingAtPort;
            final boolean isLookingAt = context.isLookingAt();
            if (isLookingAt) {
                closedSprite = ModTextures.LOCATION_OVERLAY_CASING_PORT_CLOSED;
                openSprite = ModTextures.LOCATION_OVERLAY_CASING_PORT_OPEN;

                final HitResult hit = context.getHitResult();
                assert hit.getType() == HitResult.Type.BLOCK : "renderer.cameraHitResult.getType() is not of type BLOCK even though it was in isObserverLookingAt";
                assert hit instanceof BlockHitResult : "renderer.cameraHitResult is not a BlockRayTraceResult even though it was in isObserverLookingAt";
                final BlockHitResult blockHit = (BlockHitResult) hit;
                final BlockPos pos = blockHit.getBlockPos();
                final Vec3 uv = TransformUtil.hitToUV(context.getFace(), blockHit.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ()));
                lookingAtPort = Port.fromUVQuadrant(uv);
            } else {
                closedSprite = ModTextures.LOCATION_OVERLAY_CASING_PORT_CLOSED_SMALL;
                openSprite = null;

                lookingAtPort = null;
            }

            final PoseStack matrixStack = context.getMatrixStack();
            matrixStack.pushPose();
            for (final Port port : Port.CLOCKWISE) {
                final boolean isClosed = context.isReceivingPipeLocked(port);
                final Identifier sprite = isClosed ? closedSprite : openSprite;
                if (sprite != null) {
                    context.drawAtlasQuadUnlit(sprite);
                }

                if (port == lookingAtPort) {
                    context.drawAtlasQuadUnlit(ModTextures.LOCATION_OVERLAY_CASING_PORT_HIGHLIGHT);
                }

                matrixStack.translate(0.5, 0.5, 0.5);
                matrixStack.mulPose(new Quaternionf().fromAxisAngleDeg(AXIS_Z_POSITIVE, 90));
                matrixStack.translate(-0.5, -0.5, -0.5);
            }
            matrixStack.popPose();

            return isLookingAt;
        } else {
            final Identifier sprite;
            if (context.isLocked()) {
                sprite = ModTextures.LOCATION_OVERLAY_CASING_LOCKED;
            } else {
                sprite = ModTextures.LOCATION_OVERLAY_CASING_UNLOCKED;
            }

            context.drawAtlasQuadUnlit(sprite);
        }

        return true;
    }
}
