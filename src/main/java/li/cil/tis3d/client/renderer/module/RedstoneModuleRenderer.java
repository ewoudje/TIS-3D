package li.cil.tis3d.client.renderer.module;

import com.mojang.blaze3d.vertex.PoseStack;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.prefab.module.AbstractModuleWithRotationRenderer;
import li.cil.tis3d.api.util.ModuleRenderContext;
import li.cil.tis3d.client.renderer.ModTextures;
import li.cil.tis3d.common.module.RedstoneModule;
import li.cil.tis3d.util.Color;

public class RedstoneModuleRenderer extends AbstractModuleWithRotationRenderer<RedstoneModule> {

    // Rendering info.
    private static final float OUTPUT_X = 9 / 32f;
    private static final float INPUT_X = 20 / 32f;
    private static final float SHARED_V0 = 10 / 32f;
    private static final float SHARED_Y = 25 / 32f;
    private static final float SHARED_W = 3 / 32f;
    private static final float SHARED_H = SHARED_Y - SHARED_V0;


    @Override
    public boolean matches(Module module) {
        return module instanceof RedstoneModule;
    }

    @Override
    public void render(final RedstoneModule module, final ModuleRenderContext context) {
        if (!module.getCasing().isEnabled()) {
            return;
        }

        final PoseStack matrixStack = context.getMatrixStack();
        matrixStack.pushPose();
        rotateForRendering(module, matrixStack);

        // Draw base overlay.
        context.drawAtlasQuadUnlit(ModTextures.LOCATION_OVERLAY_MODULE_REDSTONE);

        if (!module.getCasing().isEnabled()) {
            matrixStack.popPose();
            return;
        }

        // Draw output bar.
        final float relativeOutput = module.getRedstoneOutput() / 15f;
        final float heightOutput = relativeOutput * SHARED_H;
        final float v0Output = SHARED_Y - heightOutput;
        context.drawAtlasQuadUnlit(ModTextures.LOCATION_OVERLAY_MODULE_REDSTONE_BARS,
            OUTPUT_X, v0Output, SHARED_W, heightOutput,
            OUTPUT_X, v0Output, OUTPUT_X + SHARED_W, v0Output + heightOutput,
            Color.WHITE);

        // Draw input bar.
        final float relativeInput = module.getRedstoneInput() / 15f;
        final float heightInput = relativeInput * SHARED_H;
        final float v0Input = SHARED_Y - heightInput;
        context.drawAtlasQuadUnlit(ModTextures.LOCATION_OVERLAY_MODULE_REDSTONE_BARS,
            INPUT_X, v0Input, SHARED_W, heightInput,
            INPUT_X, v0Input, INPUT_X + SHARED_W, v0Input + heightInput,
            Color.WHITE);

        matrixStack.popPose();
    }
}
