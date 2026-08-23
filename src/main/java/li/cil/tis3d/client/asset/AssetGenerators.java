package li.cil.tis3d.client.asset;

import net.neoforged.neoforge.data.event.GatherDataEvent;

public class AssetGenerators {
    public static void gatherData(final GatherDataEvent.Client event) {
        event.createProvider(ModModelProvider::new);
    }
}
