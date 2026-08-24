package li.cil.tis3d.api.prefab.module;

import com.mojang.blaze3d.vertex.PoseStack;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.traits.ModuleWithRotation;
import org.joml.Quaternionf;

public abstract class AbstractModuleWithRotationRenderer<T extends ModuleWithRotation> extends AbstractModuleRenderer<T> {
    // --------------------------------------------------------------------- //
    // Rendering utility

    /**
     * Apply the module's rotation to the OpenGL state.
     *
     * @param matrixStack the current matrix stack.
     */
    protected void rotateForRendering(final T module, final PoseStack matrixStack) {
        final int rotation = Port.ROTATION[module.getFacing().ordinal()];
        matrixStack.translate(0.5f, 0.5f, 0);
        matrixStack.mulPose(new Quaternionf().fromAxisAngleDeg(0, 0, 1, 90 * rotation * Face.toDirection(module.getFace()).getStepY()));
        matrixStack.translate(-0.5f, -0.5f, 0);
    }
}
