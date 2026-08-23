package li.cil.tis3d.client.renderer.block;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;

public final class ModuleModelLoader implements UnbakedModelLoader<ModuleModel> {
    @Override
    public ModuleModel read(final JsonObject modelContents, final JsonDeserializationContext context) throws JsonParseException {
        BlockModel model = context.deserialize(modelContents, BlockModel.class);
        return new ModuleModel(model);
    }
}
