package li.cil.tis3d.data;

import li.cil.tis3d.api.API;
import li.cil.tis3d.common.item.Items;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.concurrent.CompletableFuture;

public final class ModRecipesProvider extends RecipeProvider {
    public ModRecipesProvider(final HolderLookup.Provider provider, final RecipeOutput output) {
        super(provider, output);
    }

    private ShapedRecipeBuilder module(final DeferredHolder<Item, ? extends Item> module, final int count, final Item item, final Criterion<InventoryChangeTrigger.TriggerInstance> unlockedBy) {
        return module(module, count, unlockedBy)
            .define('S', item);
    }

    private ShapedRecipeBuilder module(final DeferredHolder<Item, ? extends Item> module, final int count, final TagKey<Item> tag, final Criterion<InventoryChangeTrigger.TriggerInstance> unlockedBy) {
        return module(module, count, unlockedBy)
            .define('S', tag);
    }

    private ShapedRecipeBuilder module(final DeferredHolder<Item, ? extends Item> module, final int count, final Criterion<InventoryChangeTrigger.TriggerInstance> unlockedBy) {
        return shaped(RecipeCategory.MISC, module.get(), count)
            .pattern("PPP")
            .pattern("ISI")
            .pattern(" R ")
            .define('P', Tags.Items.GLASS_PANES_COLORLESS)
            .define('I', Tags.Items.INGOTS_IRON)
            .define('R', Tags.Items.DUSTS_REDSTONE)
            .unlockedBy("has_base_item", unlockedBy);
    }

    private Criterion<InventoryChangeTrigger.TriggerInstance> inventoryChange(final TagKey<Item> tag) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(items, tag).build());
    }

    private Criterion<InventoryChangeTrigger.TriggerInstance> inventoryChange(final ItemLike item) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(item);
    }

    @Override
    protected void buildRecipes() {
        shaped(RecipeCategory.REDSTONE, Items.CASING.get(), 8)
            .pattern("IRI")
            .pattern("RSR")
            .pattern("IRI")
            .define('I', Tags.Items.INGOTS_IRON)
            .define('R', Tags.Items.DUSTS_REDSTONE)
            .define('S', Tags.Items.STORAGE_BLOCKS_IRON)
            .unlockedBy("has_redstone", inventoryChange(Tags.Items.DUSTS_REDSTONE))
            .save(this.output);

        shaped(RecipeCategory.REDSTONE, Items.CONTROLLER.get())
            .pattern("IRI")
            .pattern("RSR")
            .pattern("IRI")
            .define('I', Tags.Items.INGOTS_IRON)
            .define('R', Tags.Items.DUSTS_REDSTONE)
            .define('S', Tags.Items.GEMS_DIAMOND)
            .unlockedBy("has_redstone", inventoryChange(Tags.Items.DUSTS_REDSTONE))
            .save(this.output);

        shaped(RecipeCategory.TOOLS, Items.KEY.get())
            .pattern("GI ")
            .pattern("GI ")
            .pattern("LRQ")
            .define('L', Tags.Items.GEMS_LAPIS)
            .define('G', Tags.Items.NUGGETS_GOLD)
            .define('I', Tags.Items.INGOTS_IRON)
            .define('Q', Tags.Items.GEMS_QUARTZ)
            .define('R', Tags.Items.DUSTS_REDSTONE)
            .unlockedBy("has_casing", inventoryChange(Items.CASING.get()))
            .save(this.output);

        shapeless(RecipeCategory.MISC, Items.PRISM.get())
            .requires(Tags.Items.GEMS_QUARTZ)
            .requires(Tags.Items.DUSTS_REDSTONE)
            .requires(Tags.Items.GEMS_LAPIS)
            .requires(Tags.Items.GEMS_EMERALD)
            .unlockedBy("has_execution_module", inventoryChange(Items.EXECUTION_MODULE.get()))
            .save(this.output);

        module(Items.AUDIO_MODULE, 2, net.minecraft.world.item.Items.NOTE_BLOCK, inventoryChange(Items.SEQUENCER_MODULE.get()))
            .save(this.output);
        module(Items.DISPLAY_MODULE, 2, Items.PRISM.get(), inventoryChange(Items.EXECUTION_MODULE.get()))
            .save(this.output);
        module(Items.EXECUTION_MODULE, 2, Tags.Items.INGOTS_GOLD, inventoryChange(Items.REDSTONE_MODULE.get()))
            .save(this.output);
        module(Items.FACADE_MODULE, 8, net.minecraft.world.item.Items.PAPER, inventoryChange(Items.CASING.get()))
            .save(this.output);
        module(Items.INFRARED_MODULE, 2, net.minecraft.world.item.Items.SPIDER_EYE, inventoryChange(Items.REDSTONE_MODULE.get()))
            .save(this.output);
        module(Items.KEYPAD_MODULE, 2, ItemTags.BUTTONS, inventoryChange(Items.REDSTONE_MODULE.get()))
            .save(this.output);
        module(Items.RANDOM_MODULE, 2, Tags.Items.ENDER_PEARLS, inventoryChange(Items.EXECUTION_MODULE.get()))
            .save(this.output);
        module(Items.RANDOM_ACCESS_MEMORY_MODULE, 2, Tags.Items.GEMS_EMERALD, inventoryChange(Items.STACK_MODULE.get()))
            .save(this.output);
        module(Items.READ_ONLY_MEMORY_MODULE, 1, li.cil.tis3d.common.tags.ItemTags.BOOKS, inventoryChange(Items.STACK_MODULE.get()))
            .save(this.output);
        module(Items.REDSTONE_MODULE, 2, net.minecraft.world.item.Items.REPEATER, inventoryChange(Tags.Items.DUSTS_REDSTONE))
            .save(this.output);
        module(Items.SEQUENCER_MODULE, 2, Tags.Items.MUSIC_DISCS, inventoryChange(Items.QUEUE_MODULE.get()))
            .save(this.output);
        module(Items.SERIAL_PORT_MODULE, 2, Tags.Items.GEMS_QUARTZ, inventoryChange(Items.EXECUTION_MODULE.get()))
            .save(this.output);
        module(Items.STACK_MODULE, 2, Tags.Items.CHESTS, inventoryChange(Items.REDSTONE_MODULE.get()))
            .save(this.output);
        module(Items.TIMER_MODULE, 2, Tags.Items.SANDS, inventoryChange(Items.EXECUTION_MODULE.get()))
            .save(this.output);

        shaped(RecipeCategory.MISC, Items.TERMINAL_MODULE.get())
            .pattern("KDS")
            .pattern("IQI")
            .pattern(" R ")
            .define('K', Items.KEYPAD_MODULE.get())
            .define('D', Items.DISPLAY_MODULE.get())
            .define('S', Items.STACK_MODULE.get())
            .define('I', Tags.Items.INGOTS_IRON)
            .define('Q', Tags.Items.GEMS_QUARTZ)
            .define('R', Tags.Items.DUSTS_REDSTONE)
            .unlockedBy("has_keypad", inventoryChange(Items.KEYPAD_MODULE.get()))
            .save(this.output);


        shapeless(RecipeCategory.MISC, Items.QUEUE_MODULE.get())
            .requires(Items.STACK_MODULE.get())
            .unlockedBy("has_stack", inventoryChange(Items.STACK_MODULE.get()))
            .save(this.output, API.MOD_ID + ":" + Items.QUEUE_MODULE.getId().getPath() + "/from_stack");

        shapeless(RecipeCategory.MISC, Items.STACK_MODULE.get())
            .requires(Items.QUEUE_MODULE.get())
            .unlockedBy("has_queue", inventoryChange(Items.QUEUE_MODULE.get()))
            .save(this.output, API.MOD_ID + ":" + Items.STACK_MODULE.getId().getPath() + "/from_queue");
    }

    // The runner to add to the data generator
    public static class Runner extends RecipeProvider.Runner {
        // Get the parameters from the `GatherDataEvent`s.
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
            super(output, lookupProvider);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider provider, RecipeOutput output) {
            return new ModRecipesProvider(provider, output);
        }

        @Override
        public String getName() {
            return "TIS3D Recipe DataGen";
        }
    }
}
