package li.cil.tis3d.common.network.message.c2s;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import li.cil.tis3d.common.item.CodeBookItem;
import li.cil.tis3d.common.item.Items;
import li.cil.tis3d.common.item.ReadOnlyMemoryModuleItem;
import li.cil.tis3d.common.network.message.MessageHandler;
import li.cil.tis3d.common.network.message.MessageSender;
import li.cil.tis3d.common.network.message.s2c.S2CMessages;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

import static li.cil.tis3d.common.network.message.c2s.C2SMessages.CodeBook;
import static li.cil.tis3d.common.network.message.c2s.C2SMessages.Loaded;
import static li.cil.tis3d.common.network.message.common.CommonMessages.*;

public class C2SHandler extends MessageHandler<C2SMessage> {

    public C2SHandler() {
        super(false);

        globalHandler(CodeBook.TYPE, this::handleMessage, CodeBook.STREAM_CODEC);
        globalHandler(ROMData.C2S_TYPE, this::handleMessage, ROMData.STREAM_CODEC);
        casingHandler(Loaded.TYPE, this::handleMessage, Loaded.STREAM_CODEC);
        moduleHandler(ModuleNBTData.TYPE, this::handleMessage, ModuleNBTData.STREAM_CODEC);
        moduleHandler(ModuleByteData.TYPE, this::handleMessage, ModuleByteData.STREAM_CODEC);
    }

    private void handleMessage(CodeBook message) {
        final ItemStack stack = getPlayer().getItemInHand(message.hand());
        if (Items.is(stack, Items.BOOK_CODE)) {
            CodeBookItem.MutableData.setToStack(stack, message.data());
        }
    }

    private void handleMessage(ROMData message) {
        ItemStack item = getPlayer().getItemInHand(message.hand());
        if (item.isEmpty()) return;

        if (Items.is(item, Items.READ_ONLY_MEMORY_MODULE)) {
            ReadOnlyMemoryModuleItem.saveToStack(item, message.bytes());
        }
    }

    private void handleMessage(CasingBlockEntity casing, Loaded message) {
        final var listTag = new ListTag();
        for (var face : Face.VALUES) {
            final var module = casing.getModule(face);
            final var output = TagValueOutput.createWithContext(reporter(), registryAccess());

            if (module != null) {
                module.save(output);
            }

            listTag.add(output.buildResult());
        }

        MessageSender.sendMessageFor(casing, new S2CMessages.Initialize(listTag));
    }

    private void handleMessage(CasingBlockEntity casing, Module module, ModuleNBTData message) {
        module.onData(TagValueInput.create(reporter(), registryAccess(), message.tag()));
    }

    private void handleMessage(CasingBlockEntity casing, Module module, ModuleByteData message) {
        module.onData(message.buf());
    }
}
