package li.cil.tis3d.client.models;

import li.cil.tis3d.api.API;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import net.neoforged.neoforge.model.data.ModelProperty;

public class ModModels {
    public static final ModelProperty<ModuleModelData[]> MODULE_MODEL_DATA_PROPERTY = new ModelProperty<>();
    public static final StandaloneModelKey<BlockStateModel> EMPTY_CASING = new StandaloneModelKey<>(() -> "module_casing_side");
    public static final Identifier EMPTY_CASING_ID = API.resource("block/casing_all");
    public static final Identifier MODULE_CASING_ID = API.resource("block/casing_module");


    public static void register(IEventBus bus) {
        bus.addListener((ModelEvent.RegisterStandalone e) -> {
            e.register(EMPTY_CASING, SimpleUnbakedStandaloneModel.blockStateModel(MODULE_CASING_ID));
        });

        bus.addListener((RegisterBlockStateModels e) -> {
            e.registerModel(API.resource("casing"), CasingModel.Unbaked.INSTANCE.codec());
        });
    }
}
