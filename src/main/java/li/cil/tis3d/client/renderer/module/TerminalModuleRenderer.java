package li.cil.tis3d.client.renderer.module;

import com.mojang.blaze3d.vertex.PoseStack;
import li.cil.manual.api.render.FontRenderer;
import li.cil.tis3d.api.API;
import li.cil.tis3d.api.ClientAPI;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.prefab.module.AbstractModuleWithRotationRenderer;
import li.cil.tis3d.api.util.RenderContext;
import li.cil.tis3d.client.renderer.Textures;
import li.cil.tis3d.common.module.TerminalModule;
import li.cil.tis3d.util.Color;

import java.util.List;

public class TerminalModuleRenderer extends AbstractModuleWithRotationRenderer<TerminalModule> {

    @Override
    public boolean matches(Module module) {
        return module instanceof TerminalModule;
    }

    @Override
    public void render(final TerminalModule module, final RenderContext context) {
        if (!module.getCasing().isEnabled()) {
            return;
        }

        final PoseStack matrixStack = context.getMatrixStack();
        matrixStack.pushPose();
        rotateForRendering(module, matrixStack);

        if (context.closeEnoughForDetails(module.getCasing().getPosition())) {
            // Player is close, render actual terminal text.
            renderText(module, context);
        } else {
            // Player too far away for details, draw static overlay.
            context.drawAtlasQuadUnlit(Textures.LOCATION_OVERLAY_MODULE_TERMINAL);
        }

        matrixStack.popPose();
    }

    private void renderText(final TerminalModule module, final RenderContext context) {
        final PoseStack matrixStack = context.getMatrixStack();
        matrixStack.translate(2f / 16f, 2f / 16f, 0);
        matrixStack.scale(1 / 512f, 1 / 512f, 1);

        final var fontRenderer = ClientAPI.normalFontRenderer;
        final var display = module.getDisplay();

        final int totalWidth = 12 * 32;
        final int textWidth = TerminalModule.MAX_COLUMNS * fontRenderer.width(" ");
        final float offsetX = (totalWidth - textWidth) / 2f;
        matrixStack.translate(offsetX, 10f, 0);

        renderDisplay(context, display, fontRenderer);

        matrixStack.translate(0, (TerminalModule.MAX_ROWS - display.size()) * fontRenderer.lineHeight() + 4, 0);

        renderInput(module, context, fontRenderer, textWidth);
    }

    private void renderDisplay(final RenderContext context, final List<StringBuilder> display, final FontRenderer fontRenderer) {
        final PoseStack matrixStack = context.getMatrixStack();
        for (final StringBuilder line : display) {
            context.drawString(fontRenderer, line, Color.WHITE);
            matrixStack.translate(0, fontRenderer.lineHeight(), 0);
        }
    }

    private void renderInput(final TerminalModule module, final RenderContext context, final FontRenderer fontRenderer, final int textWidth) {
        final PoseStack matrixStack = context.getMatrixStack();
        final var input = module.getInput();

        final int color = Color.withAlpha(Color.WHITE, module.isInputEnabled() ? 1f : 0.5f);

        context.drawQuadUnlit(-4, 0, textWidth + 8, 24, color);
        context.drawQuadUnlit(-2, 2, textWidth + 4, 20, Color.DARK_GRAY);

        matrixStack.translate(0, 4, 0);
        context.drawString(fontRenderer, input, Color.WHITE);

        if (module.isInputEnabled() && input.length() < TerminalModule.MAX_COLUMNS && System.currentTimeMillis() % 800 > 400) {
            final int w = fontRenderer.width(" ");
            final int h = fontRenderer.lineHeight();
            final int x = input.length() * w;
            context.drawQuadUnlit(x, 0, w, h, Color.WHITE);
        }
    }
}
