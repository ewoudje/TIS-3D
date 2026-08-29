package li.cil.tis3d.common.network.message;

import com.mojang.logging.LogUtils;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import li.cil.tis3d.common.network.Network;
import li.cil.tis3d.common.network.message.c2s.C2SHandler;
import li.cil.tis3d.common.network.message.c2s.C2SMessage;
import li.cil.tis3d.common.network.message.s2c.S2CHandler;
import li.cil.tis3d.common.network.message.s2c.S2CMessage;
import li.cil.tis3d.common.network.message.s2c.S2CMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3dc;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class MessageSender {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final int RANGE_HIGH = 48;
    public static final int RANGE_MEDIUM = 32;
    public static final int RANGE_LOW = 16;


    public static <T extends CustomPacketPayload & S2CMessage> void sendToPlayer(final ServerPlayer player, final T message) {
        PacketDistributor.sendToPlayer(player, message);
    }

    public static <T extends CasingMessage> void sendMessageFor(CasingBlockEntity be, T message) {
        sendMessageFor(be.getCasing(), message);
    }

    public static <T extends CasingMessage> void sendMessageFor(CasingBlockEntity be, int range, T message) {
        sendMessageFor(be.getCasing(), range, message);
    }

    public static <T extends CasingMessage> void sendMessageFor(Casing casing, T message) {
        sendMessageFor(casing, RANGE_HIGH, message);
    }

    public static <T extends CasingMessage> void sendMessageFor(Casing casing, int range, T message) {
        Level level = casing.getCasingLevel();

        if (level instanceof ServerLevel sl && message instanceof S2CMessage) {
            PacketDistributor.sendToPlayersTrackingChunk(
                sl,
                new ChunkPos(casing.getPosition()),
                Network.C2S.wrap(message, casing.getPosition())
            );
        } else if (level.isClientSide() && message instanceof C2SMessage) {
            ClientPacketDistributor.sendToServer(Network.S2C.wrap(message, casing.getPosition()));
        } else {
            LOGGER.error("Tried to send (client/server) casing message on the wrong side.");
        }
    }

    public static <T extends ModuleMessage> void sendMessageFor(Module module, T message) {
        var wrappedMsg = new MessageHandler.WrappedModuleMessage<>(message, module.getFace());
        sendMessageFor(module.getCasing(), wrappedMsg);
    }

    public static <T extends ModuleMessage> void sendMessageFor(Module module, T message, byte type) {
        sendMessageFor(module, message);
    }

    public static <T extends S2CMessage & CustomPacketPayload> void sendToNearbyPlayers(Level level, BlockPos blockPos, int range, T message) {
        if (!(level instanceof ServerLevel sl)) {
            LOGGER.error("Tried to send global message with non-server level.");
            return;
        }

        PacketDistributor.sendToPlayersNear(sl, null, blockPos.getX(), blockPos.getY(), blockPos.getZ(), range, message);
    }

    public static <T extends S2CMessage & CustomPacketPayload> void sendToNearbyPlayers(Level level, BlockPos blockPos, T message) {
        if (!(level instanceof ServerLevel sl)) {
            LOGGER.error("Tried to send global message with non-server level.");
            return;
        }

        PacketDistributor.sendToPlayersTrackingChunk(sl, new ChunkPos(blockPos), message);
    }

    public static <T extends C2SMessage & CustomPacketPayload> void sendToServer(T message) {
        ClientPacketDistributor.sendToServer(message);
    }
}
