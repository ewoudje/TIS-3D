package li.cil.tis3d.common.network.message.s2c;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.client.gui.ReadOnlyMemoryModuleScreen;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import li.cil.tis3d.common.network.message.MessageHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.storage.TagValueInput;

import static li.cil.tis3d.common.network.message.common.CommonMessages.*;
import static li.cil.tis3d.common.network.message.s2c.S2CMessages.*;

public class S2CHandler extends MessageHandler<S2CMessage> {
    public S2CHandler() {
        super(true);

        globalHandler(PipeParticle.TYPE, this::handleMessage, PipeParticle.STREAM_CODEC);
        globalHandler(ROMData.S2C_TYPE, this::handleMessage, ROMData.STREAM_CODEC);

        casingHandler(Initialize.TYPE, this::handleMessage, Initialize.STREAM_CODEC);
        casingHandler(EnabledState.TYPE, this::handleMessage, EnabledState.STREAM_CODEC);
        casingHandler(Inventory.TYPE, this::handleMessage, Inventory.STREAM_CODEC);
        casingHandler(LockedState.TYPE, this::handleMessage, LockedState.STREAM_CODEC);

        moduleHandler(ModuleNBTData.TYPE, this::handleMessage, ModuleNBTData.STREAM_CODEC);
        moduleHandler(ModuleByteData.TYPE, this::handleMessage, ModuleByteData.STREAM_CODEC);
        moduleHandler(PipeLockedState.TYPE, this::handleMessage, PipeLockedState.STREAM_CODEC);
    }

    private void handleMessage(PipeParticle message) {
        getClientLevel().addParticle(
            DustParticleOptions.REDSTONE,
            message.x(), message.y(), message.z(),
            0, 0, 0
        );
    }

    private void handleMessage(ROMData message) {
        final Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof final ReadOnlyMemoryModuleScreen moduleScreen) {
            moduleScreen.setData(message.bytes());
        }
    }

    private void handleMessage(CasingBlockEntity casing, Initialize message) {
        if (!(message.list() instanceof ListTag l))
            throw new IllegalArgumentException();

        for (int i = 0; i < Face.VALUES.length; i++) {
            final var face = Face.VALUES[i];
            final var moduleTag = l.getCompoundOrEmpty(i);
            final var module = casing.getModule(face);
            if (module != null) {
                module.load(TagValueInput.create(reporter(), registryAccess(), moduleTag));
            }
        }
    }

    private void handleMessage(CasingBlockEntity casing, EnabledState message) {
        casing.setEnabledClient(message.isEnabled());
    }

    private void handleMessage(CasingBlockEntity casing, Inventory message) {
        casing.setStackAndModuleClient(
            message.slot(),
            message.stack(),
            TagValueInput.create(reporter(), registryAccess(), message.moduleData())
        );
    }

    private void handleMessage(CasingBlockEntity casing, LockedState message) {
        casing.setEnabledClient(message.isLocked());
    }

    private void handleMessage(CasingBlockEntity casing, Module module, ModuleNBTData message) {
        module.onData(TagValueInput.create(reporter(), registryAccess(), message.tag()));
    }

    private void handleMessage(CasingBlockEntity casing, Module module, ModuleByteData message) {
        module.onData(message.buf());
    }

    private void handleMessage(CasingBlockEntity casing, Module module, PipeLockedState message) {
        casing.setReceivingPipeLockedClient(module.getFace(), message.port(), message.isLocked());
    }

    private ClientLevel getClientLevel() {
        return Minecraft.getInstance().level;
    }

    @Override
    protected LocalPlayer getPlayer() {
        return Minecraft.getInstance().player;
    }

    @Override
    protected RegistryAccess registryAccess() {
        return getClientLevel().registryAccess();
    }
}
