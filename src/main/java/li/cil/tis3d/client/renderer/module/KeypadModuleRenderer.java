package li.cil.tis3d.client.renderer.module;

import com.mojang.blaze3d.vertex.PoseStack;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.prefab.module.AbstractModuleWithRotationRenderer;
import li.cil.tis3d.api.util.ModuleRenderContext;
import li.cil.tis3d.client.renderer.Textures;
import li.cil.tis3d.common.module.KeypadModule;
import li.cil.tis3d.util.Color;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

import static li.cil.tis3d.common.module.KeypadModule.*;

public class KeypadModuleRenderer extends AbstractModuleWithRotationRenderer<KeypadModule> {

    // Color of hovered/focused button highlight.
    private static final int HIGHLIGHT_COLOR = Color.withAlpha(Color.WHITE, 0.5f);

    @Override
    public boolean matches(Module module) {
        return module instanceof KeypadModule;
    }

    @Override
    public void render(final KeypadModule module, final ModuleRenderContext context) {
        if (!module.getCasing().isEnabled() || !module.isVisible()) {
            return;
        }

        final PoseStack matrixStack = context.getMatrixStack();
        matrixStack.pushPose();
        rotateForRendering(module, matrixStack);

        final Optional<Short> value = module.getValue();

        // Draw base texture. Draw half transparent while writing current value,
        // i.e. while no input is possible.
        context.drawAtlasQuadUnlit(Textures.LOCATION_OVERLAY_MODULE_KEYPAD, Color.withAlpha(Color.WHITE, value.isPresent() ? 0.5f : 1f));

        // Draw overlay for hovered button if we can currently input a value.

        if (value.isEmpty()) {
            final Vec3 hitPos = Vec3.ZERO; //TODO getLocalHitPosition(module, context.getDispatcher().cameraHitResult);
            if (hitPos != null) {
                final Vec3 uv = module.hitToUV(hitPos);
                final int button = module.uvToButton((float) uv.x, (float) uv.y);
                if (button >= 0) {
                    drawButtonOverlay(context, button);
                }
            }
        }

        matrixStack.popPose();
    }

    private void drawButtonOverlay(final ModuleRenderContext context, final int button) {
        final int column = button % 3;
        final int row = button / 3;
        final float x = KEYS_U0 + column * KEYS_STEP_U;
        final float y = KEYS_V0 + row * KEYS_STEP_V;
        final float w = KeypadModule.buttonToNumber(button) == 0 ? (KEYS_SIZE_U + KEYS_STEP_U) : KEYS_SIZE_U;
        final float h = row == 3 ? KEYS_SIZE_V_LAST : KEYS_SIZE_V;

        context.drawQuadUnlit(x, y, w, h, HIGHLIGHT_COLOR);
    }
}
