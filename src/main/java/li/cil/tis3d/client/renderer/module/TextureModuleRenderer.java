package li.cil.tis3d.client.renderer.module;

import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.ModuleRenderer;
import li.cil.tis3d.api.util.RenderContext;
import net.minecraft.resources.Identifier;

public class TextureModuleRenderer<T extends Module> implements ModuleRenderer<T> {
    private final Identifier texture;
    private final Class<? extends T> moduleClass;

    public TextureModuleRenderer(Identifier texture, Class<? extends T> moduleClass) {
        this.texture = texture;
        this.moduleClass = moduleClass;
    }

    @Override
    public boolean matches(Module module) {
        return moduleClass.isInstance(module);
    }

    @Override
    public void render(final T module, final RenderContext context) {
        if (!module.getCasing().isEnabled()) {
            return;
        }

        context.drawAtlasQuadLit(texture);
    }
}
