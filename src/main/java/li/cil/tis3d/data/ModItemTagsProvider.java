package li.cil.tis3d.data;

import li.cil.tis3d.api.API;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

import java.util.concurrent.CompletableFuture;

import static li.cil.tis3d.common.item.Items.*;
import static li.cil.tis3d.common.tags.ItemTags.*;

public final class ModItemTagsProvider extends ItemTagsProvider {
    public ModItemTagsProvider(final PackOutput output, final CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, API.MOD_ID);
    }

    @Override
    protected void addTags(final HolderLookup.Provider provider) {

        tag(COMPUTERS)
            .add(
                CONTROLLER.get(),
                CASING.get()
            );

        tag(MODULES).add(
            AUDIO_MODULE.get(),
            DISPLAY_MODULE.get(),
            EXECUTION_MODULE.get(),
            FACADE_MODULE.get(),
            INFRARED_MODULE.get(),
            KEYPAD_MODULE.get(),
            QUEUE_MODULE.get(),
            RANDOM_MODULE.get(),
            RANDOM_ACCESS_MEMORY_MODULE.get(),
            READ_ONLY_MEMORY_MODULE.get(),
            REDSTONE_MODULE.get(),
            SEQUENCER_MODULE.get(),
            SERIAL_PORT_MODULE.get(),
            STACK_MODULE.get(),
            TERMINAL_MODULE.get(),
            TIMER_MODULE.get()
        );

        tag(BOOKS).add(
            BOOK_CODE.get(),
            BOOK_MANUAL.get(),
            Items.BOOK,
            Items.ENCHANTED_BOOK,
            Items.WRITABLE_BOOK,
            Items.WRITTEN_BOOK
        );

        tag(KEYS).add(
            KEY.get(),
            KEY_CREATIVE.get()
        );
    }
}
