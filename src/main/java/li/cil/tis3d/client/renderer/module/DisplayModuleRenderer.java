package li.cil.tis3d.client.renderer.module;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import li.cil.tis3d.api.API;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.prefab.module.AbstractModuleWithRotationRenderer;
import li.cil.tis3d.api.util.RenderContext;
import li.cil.tis3d.client.renderer.ModRenderTypes;
import li.cil.tis3d.common.module.DisplayModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public class DisplayModuleRenderer extends AbstractModuleWithRotationRenderer<DisplayModule> {

    // Don't allow displaying stuff on the edge of the casing. I mean we could,
    // technically, but that'd usually look pretty weird. Also it's more
    // intuitive that the usable area start in the inner, black part.
    private static final int MARGIN = 4;
    private static final Map<DisplayModule, RenderData> RENDER_DATA = new HashMap<>();
    private static int nextTextureId = 0;

    /**
     * Deletes our texture from the GPU, if we have one.
     */
    public static void deleteTexture(DisplayModule module) {
        final RenderData data = RENDER_DATA.get(module);
        if (data != null) {
            data.delete();
        }
    }

    private RenderData getRenderData(DisplayModule module) {
        return RENDER_DATA.computeIfAbsent(module, m -> {
            final TextureManager textureManager = Minecraft.getInstance().getTextureManager();
            final String name = "dynamic/display_module_" + (++nextTextureId);
            final DynamicTexture texture = new DynamicTexture(name, DisplayModule.RESOLUTION, DisplayModule.RESOLUTION, false);
            final Identifier textureId = API.resource(name);
            textureManager.register(textureId, texture);
            return new RenderData(texture, textureId, ModRenderTypes.unlitTexture(textureId));
        });
    }

    @Override
    public boolean matches(Module module) {
        return module instanceof DisplayModule;
    }

    @Override
    public void render(final DisplayModule module, final RenderContext context) {
        if (!module.getCasing().isEnabled()) {
            return;
        }

        final PoseStack matrixStack = context.getMatrixStack();
        matrixStack.pushPose();
        rotateForRendering(module, matrixStack);
        final RenderData renderData = getRenderData(module);

        if (module.resetImageDirty())
            renderData.write(module.getImage());

        final VertexConsumer builder = context.getBuffer().getBuffer(renderData.renderType());
        context.drawQuad(builder, MARGIN / 32f, MARGIN / 32f, DisplayModule.RESOLUTION / 32f, DisplayModule.RESOLUTION / 32f);

        matrixStack.popPose();
    }

    private record RenderData(DynamicTexture texture, Identifier textureId, RenderType renderType) {
        private void write(int[] image) {
            final NativeImage nativeImage = texture.getPixels();
            if (nativeImage == null) {
                return;
            }

            int ip = 0;
            for (int iy = 0; iy < DisplayModule.RESOLUTION; iy++) {
                for (int ix = 0; ix < DisplayModule.RESOLUTION; ix++, ip++) {
                    nativeImage.setPixel(ix, iy, image[ip]);
                }
            }

            texture.upload();
        }

        private void delete() {
            if (textureId != null) {
                Minecraft.getInstance().doRunTask(() ->
                    Minecraft.getInstance().getTextureManager().release(textureId));
            }

            if (texture != null) {
                Minecraft.getInstance().doRunTask(texture::close);
            }
        }
    }
}
