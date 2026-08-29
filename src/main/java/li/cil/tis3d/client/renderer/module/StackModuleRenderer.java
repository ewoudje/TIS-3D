package li.cil.tis3d.client.renderer.module;

import com.mojang.blaze3d.vertex.PoseStack;
import li.cil.manual.api.render.FontRenderer;
import li.cil.tis3d.api.ClientAPI;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.prefab.module.AbstractModuleWithRotationRenderer;
import li.cil.tis3d.api.util.ModuleRenderContext;
import li.cil.tis3d.client.renderer.ModTextures;
import li.cil.tis3d.common.module.StackModule;
import li.cil.tis3d.util.Color;

public class StackModuleRenderer extends AbstractModuleWithRotationRenderer<StackModule> {

    @Override
    public boolean matches(Module module) {
        return module instanceof StackModule;
    }

    @Override
    public void render(final StackModule module, final ModuleRenderContext context) {
        if (!module.getCasing().isEnabled()) {
            return;
        }

        final PoseStack matrixStack = context.getMatrixStack();
        matrixStack.pushPose();
        rotateForRendering(module, matrixStack);

        context.drawAtlasQuadUnlit(ModTextures.LOCATION_OVERLAY_MODULE_STACK);

        // Render detailed state when player is close.
        if (context.closeEnoughForDetails()) {
            drawState(module, context);
        }

        matrixStack.popPose();
    }

    private void drawState(final StackModule module, final ModuleRenderContext context) {
        final PoseStack matrixStack = context.getMatrixStack();

        // Offset to start drawing at top left of inner area, slightly inset.
        matrixStack.translate(3 / 16f, 5 / 16f, 0);
        matrixStack.scale(1 / 128f, 1 / 128f, 1);
        matrixStack.translate(4.5f, 14.5f, 0);

        final FontRenderer fontRenderer = ClientAPI.smallFontRenderer;

        for (int i = 0; i <= module.getTop(); i++) {
            context.drawString(fontRenderer, String.format("%4X", module.getAt(i)), Color.WHITE);
            matrixStack.translate(0, fontRenderer.lineHeight() + 1, 0);
            if ((i + 1) % 4 == 0) {
                matrixStack.translate((fontRenderer.width(" ") + 1) * 5, (fontRenderer.lineHeight() + 1) * -4, 0);
            }
        }
    }
}
