package li.cil.tis3d.common.ponder;

import li.cil.tis3d.api.API;
import li.cil.tis3d.common.item.Items;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;

public class TIS3DPonderPlugin implements PonderPlugin {
    public static final ResourceLocation MODULES_TAG = API.resource("modules");
    public static final ResourceLocation CASING_AND_CONTROLLER_TAG = API.resource("casings_controllers");

    @Override
    public @NotNull String getModId() {
        return API.MOD_ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        var reg = helper.<DeferredHolder<Item, ?>>withKeyFunction(DeferredHolder::getId);
        reg.forComponents(Items.CASING, Items.CONTROLLER)
            .addStoryBoard("base/controller_and_casing", OtherPonderScenes::makeTIS, CASING_AND_CONTROLLER_TAG);
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        helper.registerTag(CASING_AND_CONTROLLER_TAG)
            .title("TIS Blocks")
            .register();
        helper.registerTag(MODULES_TAG)
            .title("Modules")
            .register();
    }
}
