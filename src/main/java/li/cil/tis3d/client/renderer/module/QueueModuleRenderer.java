package li.cil.tis3d.client.renderer.module;

import com.mojang.blaze3d.vertex.PoseStack;
import li.cil.manual.api.render.FontRenderer;
import li.cil.tis3d.api.API;
import li.cil.tis3d.api.ClientAPI;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.prefab.module.AbstractModuleWithRotationRenderer;
import li.cil.tis3d.api.util.RenderContext;
import li.cil.tis3d.client.renderer.Textures;
import li.cil.tis3d.common.module.QueueModule;
import li.cil.tis3d.util.Color;

public class QueueModuleRenderer extends AbstractModuleWithRotationRenderer<QueueModule> {

    @Override
    public boolean matches(Module module) {
        return module instanceof QueueModule;
    }

    @Override
    public void render(final QueueModule module, final RenderContext context) {
        final PoseStack matrixStack = context.getMatrixStack();
        matrixStack.pushPose();
        rotateForRendering(module, matrixStack);

        context.drawAtlasQuadUnlit(Textures.LOCATION_OVERLAY_MODULE_QUEUE);

        // Render detailed state when player is close.
        if (!module.isEmpty() && context.closeEnoughForDetails(module.getCasing().getPosition())) {
            drawState(module, context);
        }

        matrixStack.popPose();
    }

    private void drawState(final QueueModule module, final RenderContext context) {
        final PoseStack matrixStack = context.getMatrixStack();

        // Offset to start drawing at top left of inner area, slightly inset.
        matrixStack.translate(3 / 16f, 5 / 16f, 0);
        matrixStack.scale(1 / 128f, 1 / 128f, 1);
        matrixStack.translate(4.5f, 14.5f, 0);

        final FontRenderer fontRenderer = ClientAPI.smallFontRenderer;
        for (int i = module.getTail(), j = 0; i != module.getHead(); i = (i + 1) % QueueModule.QUEUE_SIZE, j++) {
            context.drawString(fontRenderer, String.format("%4X", module.getAt(i)), Color.WHITE);
            matrixStack.translate(0, fontRenderer.lineHeight() + 1, 0);
            if ((j + 1) % 4 == 0) {
                matrixStack.translate((fontRenderer.width(" ") + 1) * 5, (fontRenderer.lineHeight() + 1) * -4, 0);
            }
        }
    }
}
