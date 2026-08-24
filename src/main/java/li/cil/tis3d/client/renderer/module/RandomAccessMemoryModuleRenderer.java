package li.cil.tis3d.client.renderer.module;

import com.mojang.blaze3d.vertex.PoseStack;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.prefab.module.AbstractModuleWithRotationRenderer;
import li.cil.tis3d.api.util.ModuleRenderContext;
import li.cil.tis3d.common.module.RandomAccessMemoryModule;
import li.cil.tis3d.util.Color;

public class RandomAccessMemoryModuleRenderer extends AbstractModuleWithRotationRenderer<RandomAccessMemoryModule> {
    // Rendering info.
    private static final float QUADS_U0 = 5 / 32f;
    private static final float QUADS_V0 = 5 / 32f;
    private static final float QUADS_SIZE_U = 4 / 32f;
    private static final float QUADS_SIZE_V = 4 / 32f;
    private static final float QUADS_STEP_U = 6 / 32f;
    private static final float QUADS_STEP_V = 6 / 32f;

    @Override
    public boolean matches(Module module) {
        return module instanceof RandomAccessMemoryModule;
    }

    @Override
    public void render(final RandomAccessMemoryModule module, final ModuleRenderContext context) {
        if (!module.getCasing().isEnabled() || !module.isVisible()) {
            return;
        }

        final PoseStack matrixStack = context.getMatrixStack();
        matrixStack.pushPose();
        rotateForRendering(module, matrixStack);

        final int cells = 4;
        final int cellSize = RandomAccessMemoryModule.MEMORY_SIZE / (cells * cells);
        final int cellWidth = (int) Math.sqrt(cellSize);
        final int cellColor = module.getCellColor();
        for (int y = 0; y < cells; y++) {
            for (int x = 0; x < cells; x++) {
                final float brightness = 0.25f + sectorSum(module.getMemory(), x * cellWidth, y * cellWidth, cellWidth) * 0.75f;
                final int color = Color.withAlpha(cellColor, brightness);

                final float u0 = QUADS_U0 + x * QUADS_STEP_U;
                final float v0 = QUADS_V0 + y * QUADS_STEP_V;
                context.drawQuadUnlit(u0, v0, QUADS_SIZE_U, QUADS_SIZE_V, color);
            }
        }

        matrixStack.popPose();
    }

    private float sectorSum(final byte[] memory, final int x0, final int y0, final int sectorWidth) {
        int sum = 0;
        final int sectorSize = sectorWidth * sectorWidth;
        final int rowWidth = RandomAccessMemoryModule.MEMORY_SIZE / sectorSize;
        for (int y = y0; y < y0 + sectorWidth; y++) {
            for (int x = x0; x < x0 + sectorWidth; x++) {
                final int i = y * rowWidth + x;
                sum += memory[i] & 0xFF;
            }
        }
        return sum / (sectorWidth * sectorWidth * (float) 0xFF);
    }
}
