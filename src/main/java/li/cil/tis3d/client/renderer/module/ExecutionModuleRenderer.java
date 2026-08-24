package li.cil.tis3d.client.renderer.module;

import com.mojang.blaze3d.vertex.PoseStack;
import li.cil.manual.api.render.FontRenderer;
import li.cil.tis3d.api.API;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.prefab.module.AbstractModuleWithRotationRenderer;
import li.cil.tis3d.api.util.ModuleRenderContext;
import li.cil.tis3d.client.renderer.Textures;
import li.cil.tis3d.common.module.ExecutionModule;
import li.cil.tis3d.common.module.execution.ExecutionState;
import li.cil.tis3d.common.module.execution.MachineState;
import li.cil.tis3d.common.module.execution.compiler.ParseException;
import li.cil.tis3d.util.Color;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public class ExecutionModuleRenderer extends AbstractModuleWithRotationRenderer<ExecutionModule> {

    @Override
    public boolean matches(Module module) {
        return module instanceof ExecutionModule;
    }

    @Override
    public void render(final ExecutionModule module, final ModuleRenderContext context) {
        if (!module.getCasing().isEnabled() || !module.isVisible() || !context.isLookingAt()) {
            return;
        }

        final PoseStack matrixStack = context.getMatrixStack();
        matrixStack.pushPose();
        rotateForRendering(module, matrixStack);

        // Draw status texture.
        context.drawAtlasQuadUnlit(RenderData.STATE_LOCATIONS[module.getExecutionState().ordinal()]);

        // Render detailed state when player is close.
        final MachineState machineState = module.getState();
        if (machineState.code != null && context.closeEnoughForDetails()) {
            renderState(module, context, machineState);
        }

        matrixStack.popPose();
    }

    private void renderState(final ExecutionModule module, final ModuleRenderContext context, final MachineState machineState) {
        final ExecutionState executionState = module.getExecutionState();
        final ParseException compileError = module.getCompileError();
        final PoseStack matrixStack = context.getMatrixStack();
        matrixStack.pushPose();

        // Offset to start drawing at top left of inner area, slightly inset.
        matrixStack.translate(3.5f / 16f, 3.5f / 16f, 0);
        matrixStack.scale(1 / 128f, 1 / 128f, 1);
        matrixStack.translate(1, 1, 0);

        final FontRenderer fontRenderer = API.smallFontRenderer;

        // Draw register info on top.
        final String accLast = String.format("ACC:%4X LAST:%s", machineState.acc, machineState.last.map(Enum::name).orElse("NONE"));
        context.drawString(fontRenderer, accLast, Color.WHITE);
        matrixStack.translate(0, fontRenderer.lineHeight() + 4, 0);

        final String bakState = String.format("BAK:%4X MODE:%s", machineState.bak, executionState.name());
        context.drawString(fontRenderer, bakState, Color.WHITE);
        matrixStack.translate(0, fontRenderer.lineHeight() + 4, 0);

        drawLine(context, 1, Color.WHITE);

        matrixStack.translate(0, 5, 0);

        // If we have more lines than fit on our "screen", offset so that the
        // current line is in the middle, but don't let last line scroll in.
        final int maxLines = 50 / (fontRenderer.lineHeight() + 1);
        final int totalLines = machineState.code.length;
        final int currentLine;
        if (!machineState.lineNumbers.isEmpty()) {
            currentLine = Optional.ofNullable(machineState.lineNumbers.get(machineState.pc)).orElse(-1);
        } else if (compileError != null) {
            currentLine = compileError.getLineNumber();
        } else {
            currentLine = -1;
        }
        final int page = currentLine / maxLines;
        final int offset = page * maxLines;

        for (int lineNumber = offset; lineNumber < Math.min(totalLines, offset + maxLines); lineNumber++) {
            final String line = machineState.code[lineNumber];
            final CharSequence charSequence = line.subSequence(0, Math.min(line.length(), 18));
            if (lineNumber == currentLine) {
                // Draw current line marker behind text
                if (executionState == ExecutionState.WAIT) {
                    drawLine(context, fontRenderer.lineHeight(), Color.LIGHT_GRAY);
                } else if (executionState == ExecutionState.ERR || compileError != null && compileError.getLineNumber() == currentLine) {
                    drawLine(context, fontRenderer.lineHeight(), Color.RED);
                } else {
                    drawLine(context, fontRenderer.lineHeight(), Color.WHITE);
                }

                context.drawString(fontRenderer, charSequence, Color.BLACK);
            } else {
                context.drawString(fontRenderer, charSequence, Color.WHITE);
            }

            matrixStack.translate(0, fontRenderer.lineHeight() + 1, 0);
        }

        matrixStack.popPose();
    }

    /**
     * Draws a horizontal line of the specified height.
     *
     * @param height the height of the line to draw.
     */
    private void drawLine(final ModuleRenderContext context, final int height, final int color) {
        context.drawQuadUnlit(-0.5f, -0.5f, 72, height + 1, color);
    }

    private static final class RenderData {
        private static final Identifier[] STATE_LOCATIONS = new Identifier[]{
            Textures.LOCATION_OVERLAY_MODULE_EXECUTION_IDLE,
            Textures.LOCATION_OVERLAY_MODULE_EXECUTION_ERROR,
            Textures.LOCATION_OVERLAY_MODULE_EXECUTION_RUNNING,
            Textures.LOCATION_OVERLAY_MODULE_EXECUTION_WAITING
        };
    }
}
