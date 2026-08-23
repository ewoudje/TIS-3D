package li.cil.tis3d.client.renderer.block;

import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.resources.model.UnbakedGeometry;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public final class ModuleModel implements UnbakedModel {
    private final UnbakedModel proxy;

    // --------------------------------------------------------------------- //

    ModuleModel(final UnbakedModel proxy) {
        this.proxy = proxy;
    }

    // --------------------------------------------------------------------- //
    // UnbakedModel


    @Override
    public @org.jspecify.annotations.Nullable Boolean ambientOcclusion() {
        return proxy.ambientOcclusion();
    }

    @Override
    public @Nullable GuiLight guiLight() {
        return proxy.guiLight();
    }

    @Override
    public @Nullable ItemTransforms transforms() {
        return proxy.transforms();
    }

    @Override
    public @Nullable UnbakedGeometry geometry() {
        return proxy.geometry(); //TODO
    }

    @Override
    public TextureSlots.Data textureSlots() {
        return proxy.textureSlots();
    }

    @Override
    public @Nullable Identifier parent() {
        return proxy.parent();
    }
}
