package li.cil.tis3d.client.renderer.module;

import com.mojang.blaze3d.vertex.PoseStack;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.prefab.module.AbstractModuleWithRotationRenderer;
import li.cil.tis3d.api.util.ModuleRenderContext;
import li.cil.tis3d.client.renderer.Textures;
import li.cil.tis3d.common.module.SequencerModule;
import li.cil.tis3d.util.Color;
import net.minecraft.world.phys.Vec3;

import static li.cil.tis3d.common.module.SequencerModule.*;

public class SequencerModuleRenderer extends AbstractModuleWithRotationRenderer<SequencerModule> {
    // Rendering info.
    private static final float CELLS_U0 = 5 / 32f;
    private static final float CELLS_V0 = 5 / 32f;
    private static final float CELLS_SIZE_U = 1 / 32f;
    private static final float CELLS_SIZE_V = 1 / 32f;
    private static final float CELLS_STEP_U = CELLS_SIZE_U + 2 / 32f;
    private static final float CELLS_STEP_V = CELLS_SIZE_V + 2 / 32f;
    private static final float BAR_U0 = 8 / 64f;
    private static final float BAR_V0 = 8 / 64f;
    private static final float BAR_SIZE_U = 6 / 64f;
    private static final float BAR_SIZE_V = 48 / 64f;
    private static final float BAR_STEP_U = BAR_SIZE_U;

    // Colors for module rendering.
    private static final int BAR_COLOR = 0xFF334C59;
    private static final int ACTIVE_CELL_COLOR = 0xFFCCD8DF;
    private static final int HIGHLIGHT_COLOR = 0x80B2CCE5;

    @Override
    public boolean matches(Module module) {
        return module instanceof SequencerModule;
    }

    @Override
    public void render(final SequencerModule module, final ModuleRenderContext context) {
        if (!module.isVisible()) {
            return;
        }

        final Casing casing = module.getCasing();
        final PoseStack matrixStack = context.getMatrixStack();
        matrixStack.pushPose();
        rotateForRendering(module, matrixStack);

        final boolean enabled = casing.isEnabled();
        if (enabled) {
            // Draw bar in background indicating current position in sequence.
            final float barU0 = BAR_U0 + BAR_STEP_U * module.getPosition();
            final int delay = module.getDelay();
            final float brightness = 0.75f + 0.25f * (delay == 0 ? 1 : (1 - (delay - module.getStepsRemaining()) / (float) delay));
            final int color = Color.withAlpha(BAR_COLOR, brightness);

            context.drawQuadUnlit(barU0, BAR_V0, BAR_SIZE_U, BAR_SIZE_V, color);
        }

        // Draw base grid of sequencer entries.
        context.drawAtlasQuadUnlit(Textures.LOCATION_OVERLAY_MODULE_SEQUENCER, Color.withAlpha(Color.WHITE, enabled ? 1f : 0.5f));

        if (context.closeEnoughForDetails()) {
            // Draw configuration of sequencer.
            final int color = Color.withAlpha(ACTIVE_CELL_COLOR, enabled ? 1f : 0.5f);
            for (int col = 0; col < SequencerModule.COL_COUNT; col++) {
                for (int row = 0; row < SequencerModule.ROW_COUNT; row++) {
                    if (module.isConfigured(col, row)) {
                        final float u0 = CELLS_U0 + CELLS_STEP_U * col;
                        final float v0 = CELLS_V0 + CELLS_STEP_V * row;
                        context.drawQuadUnlit(u0, v0, CELLS_SIZE_U, CELLS_SIZE_V, color);
                    }
                }
            }
        }

        // Draw selection overlay for focused cell, if any.
        final Vec3 hitPos = null; //TODO getLocalHitPosition(module, context.getDispatcher().cameraHitResult);
        if (hitPos != null) {
            final Vec3 uv = module.hitToUV(hitPos);
            final int col = module.uvToCol((float) uv.x);
            final int row = module.uvToRow((float) uv.y);
            if (col >= 0 && row >= 0) {
                final float u = CELLS_OUTER_U0 + col * CELLS_OUTER_STEP_U;
                final float v = CELLS_OUTER_V0 + row * CELLS_OUTER_STEP_V;
                context.drawQuadUnlit(u, v, CELLS_OUTER_SIZE_U, CELLS_OUTER_SIZE_V, HIGHLIGHT_COLOR);
            }
        }

        matrixStack.popPose();
    }
}
