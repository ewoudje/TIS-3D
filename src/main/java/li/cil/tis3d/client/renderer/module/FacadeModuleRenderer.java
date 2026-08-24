package li.cil.tis3d.client.renderer.module;

import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.prefab.module.AbstractModuleRenderer;
import li.cil.tis3d.api.util.ModuleRenderContext;
import li.cil.tis3d.common.module.FacadeModule;

public class FacadeModuleRenderer extends AbstractModuleRenderer<FacadeModule> {

    @Override
    public boolean matches(Module module) {
        return module instanceof FacadeModule;
    }

    @Override
    public void render(final FacadeModule module, final ModuleRenderContext context) {

    }
}
