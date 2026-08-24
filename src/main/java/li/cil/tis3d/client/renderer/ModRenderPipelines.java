package li.cil.tis3d.client.renderer;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import li.cil.tis3d.api.API;
import net.minecraft.client.renderer.RenderPipelines;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

public class ModRenderPipelines {
    public static final RenderPipeline MODULE_TEXTURED_OVERLAY = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
        .withLocation(API.resource("pipeline/module_textured_overlay"))
        .withVertexShader("core/position_tex_color")
        .withFragmentShader("core/position_tex_color")
        .withSampler("Sampler0")
        .withBlend(BlendFunction.TRANSLUCENT)
        .withDepthWrite(false)
        .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
        .build();

    public static final RenderPipeline MODULE_COLORED_OVERLAY = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
        .withLocation(API.resource("pipeline/module_colored_overlay"))
        .withVertexShader("core/position_color")
        .withFragmentShader("core/position_color")
        .withSampler("Sampler0")
        .withBlend(BlendFunction.TRANSLUCENT)
        .withDepthWrite(false)
        .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
        .build();

    public static void register(IEventBus bus) {
        bus.addListener((RegisterRenderPipelinesEvent e) -> {
            e.registerPipeline(MODULE_TEXTURED_OVERLAY);
            e.registerPipeline(MODULE_COLORED_OVERLAY);
        });
    }
}
