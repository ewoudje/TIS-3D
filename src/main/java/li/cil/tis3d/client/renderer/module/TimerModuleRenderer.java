package li.cil.tis3d.client.renderer.module;

import com.mojang.blaze3d.vertex.PoseStack;
import li.cil.manual.api.render.FontRenderer;
import li.cil.tis3d.api.ClientAPI;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.prefab.module.AbstractModuleWithRotationRenderer;
import li.cil.tis3d.api.util.ModuleRenderContext;
import li.cil.tis3d.client.renderer.ModTextures;
import li.cil.tis3d.common.module.TimerModule;
import li.cil.tis3d.util.Color;

public class TimerModuleRenderer extends AbstractModuleWithRotationRenderer<TimerModule> {

    @Override
    public boolean matches(Module module) {
        return module instanceof TimerModule;
    }

    @Override
    public void render(final TimerModule module, final ModuleRenderContext context) {
        if (!module.getCasing().isEnabled()) {
            return;
        }

        final PoseStack matrixStack = context.getMatrixStack();
        matrixStack.pushPose();
        rotateForRendering(module, matrixStack);

        context.drawAtlasQuadUnlit(ModTextures.LOCATION_OVERLAY_MODULE_TIMER);

        // Render detailed state when player is close.
        if (!module.hasElapsed() && context.closeEnoughForDetails()) {
            final long gameTime = context.gameTime();
            final float remaining = (float) (module.getTimer() - gameTime) - context.getPartialTicks();
            if (remaining <= 0) {
                module.elapsed();
            } else {
                drawState(context, remaining);
            }
        }

        matrixStack.popPose();
    }

    private void drawState(final ModuleRenderContext context, final float remaining) {
        final float milliseconds = remaining * 50f; // One tick is 50ms.
        final float seconds = milliseconds / 1000f;
        final int minutes = (int) (seconds / 60f);

        final String time;
        if (minutes > 0) {
            time = String.format("%d:%02d", minutes, (int) seconds % 60);
        } else {
            time = String.format("%.2f", seconds);
        }

        final FontRenderer fontRenderer = ClientAPI.normalFontRenderer;

        final int width = fontRenderer.width(time);
        final int height = fontRenderer.lineHeight();

        final PoseStack matrixStack = context.getMatrixStack();
        matrixStack.translate(0.5f, 0.5f, 0);
        matrixStack.scale(1 / 80f, 1 / 80f, 1);
        matrixStack.translate(-width / 2f + 1, -height / 2f + 1, 0);

        context.drawString(fontRenderer, time, Color.WHITE);
    }
}
