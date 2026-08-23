package li.cil.tis3d.data;

import net.neoforged.neoforge.data.event.GatherDataEvent;

public final class DataGenerators {

    public static void gatherData(final GatherDataEvent.Server event) {
        event.createProvider(ModLootTableProvider::new);
        event.createProvider(ModBlockTagsProvider::new);
        event.createProvider(ModItemTagsProvider::new);
        event.createProvider(ModRecipesProvider.Runner::new);
    }
}
