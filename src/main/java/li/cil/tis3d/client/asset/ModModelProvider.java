package li.cil.tis3d.client.asset;

import li.cil.tis3d.api.API;
import li.cil.tis3d.common.block.Blocks;
import li.cil.tis3d.common.item.Items;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplate;
import net.neoforged.neoforge.registries.DeferredHolder;


public final class ModModelProvider extends ModelProvider {
    private static final Identifier FULL_CASING_MODEL = API.resource("block/casing_all");
    private static final Identifier EMPTY_CASING_MODEL = API.resource("block/casing_empty");
    private static final Identifier MODULE_IN_CASING_MODEL = API.resource("block/casing_module");
    private static final Identifier CONTROLLER_MODEL = API.resource("block/controller");
    private final ExtendedModelTemplate modelTemplate;


    public ModModelProvider(final PackOutput output) {
        super(output, API.MOD_ID);

        modelTemplate = ModelTemplates.create().extend()
            .parent(modLocation("item/module"))
            .requiredTextureSlot(TextureSlot.LAYER0)
            .requiredTextureSlot(TextureSlot.LAYER1)
            .requiredTextureSlot(TextureSlot.PARTICLE)
            .build();
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {

        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(
            Blocks.CONTROLLER.get(),
            BlockModelGenerators.variant(new Variant(CONTROLLER_MODEL)))
        );

        itemModels.itemModelOutput.accept(Items.CASING.get(), ItemModelUtils.plainModel(FULL_CASING_MODEL));
        itemModels.itemModelOutput.accept(Items.CONTROLLER.get(), ItemModelUtils.plainModel(CONTROLLER_MODEL));

        simpleItem(itemModels, Items.BOOK_CODE);
        simpleItem(itemModels, Items.BOOK_MANUAL);
        simpleItem(itemModels, Items.KEY);
        simpleItem(itemModels, Items.KEY_CREATIVE);
        simpleItem(itemModels, Items.PRISM);

        itemModule(itemModels, Items.AUDIO_MODULE, "block/overlay/audio_module");
        itemModule(itemModels, Items.DISPLAY_MODULE, "block/item/display_module");
        itemModule(itemModels, Items.EXECUTION_MODULE, "block/overlay/execution_module_running");
        itemModule(itemModels, Items.FACADE_MODULE, mcLocation("block/iron_block"));
        itemModule(itemModels, Items.INFRARED_MODULE, "block/overlay/infrared_module");
        itemModule(itemModels, Items.KEYPAD_MODULE, "block/overlay/keypad_module");
        itemModule(itemModels, Items.QUEUE_MODULE, "block/item/queue_module");
        itemModule(itemModels, Items.RANDOM_MODULE, "block/overlay/random_module");
        itemModule(itemModels, Items.RANDOM_ACCESS_MEMORY_MODULE, "block/item/random_access_memory_module");
        itemModule(itemModels, Items.READ_ONLY_MEMORY_MODULE, "block/item/read_only_memory_module");
        itemModule(itemModels, Items.REDSTONE_MODULE, "block/item/redstone_module");
        itemModule(itemModels, Items.SEQUENCER_MODULE, "block/item/sequencer_module");
        itemModule(itemModels, Items.SERIAL_PORT_MODULE, "block/overlay/serial_port_module");
        itemModule(itemModels, Items.STACK_MODULE, "block/item/stack_module");
        itemModule(itemModels, Items.TERMINAL_MODULE, "block/item/terminal_module");
        itemModule(itemModels, Items.TIMER_MODULE, "block/item/timer_module");
    }

    private <T extends Item> void simpleItem(ItemModelGenerators models, final DeferredHolder<Item, T> item) {
        models.generateFlatItem(item.get(), ModelTemplates.FLAT_ITEM);
    }

    private <T extends Item> void itemModule(final ItemModelGenerators itemModels, final DeferredHolder<Item, T> item, final String overlayTexture) {
        itemModule(itemModels, item, modLocation(overlayTexture));
    }

    private <T extends Item> void itemModule(final ItemModelGenerators itemModels, final DeferredHolder<Item, T> item, final Identifier overlayTexture) {
        try {
            var model = modelTemplate.create(
                item.get(),
                TextureMapping.layered(modLocation("block/casing_module"), overlayTexture)
                    .put(TextureSlot.PARTICLE, modLocation("block/casing_module")),
                itemModels.modelOutput
            );

            itemModels.itemModelOutput.accept(item.get(), ItemModelUtils.plainModel(model));
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate item module model for " + item.getKey().identifier(), e);
        }
    }
}
