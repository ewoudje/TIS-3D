package li.cil.tis3d.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import li.cil.tis3d.api.API;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import li.cil.tis3d.common.config.CommonConfig;
import li.cil.tis3d.common.network.message.AbstractMessage;
import li.cil.tis3d.common.network.message.CasingEnabledStateMessage;
import li.cil.tis3d.common.network.message.CasingInventoryMessage;
import li.cil.tis3d.common.network.message.CasingLockedStateMessage;
import li.cil.tis3d.common.network.message.ClientCasingDataMessage;
import li.cil.tis3d.common.network.message.ClientCasingLoadedMessage;
import li.cil.tis3d.common.network.message.ClientReadOnlyMemoryModuleDataMessage;
import li.cil.tis3d.common.network.message.CodeBookDataMessage;
import li.cil.tis3d.common.network.message.ControllerStateMessage;
import li.cil.tis3d.common.network.message.HaltAndCatchFireMessage;
import li.cil.tis3d.common.network.message.ReceivingPipeLockedStateMessage;
import li.cil.tis3d.common.network.message.RedstoneParticleEffectMessage;
import li.cil.tis3d.common.network.message.ServerCasingDataMessage;
import li.cil.tis3d.common.network.message.ServerCasingInitializeMessage;
import li.cil.tis3d.common.network.message.ServerReadOnlyMemoryModuleDataMessage;
import li.cil.tis3d.util.LevelUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;

/**
 * Central networking hub for TIS-3D.
 * <p>
 * Aside from managing the mod's channel this also has facilities for throttling
 * package throughput to avoid overloading the network when a large number of
 * casings are active and nearby players. Throttling is applied to particle
 * effect emission and module packets where possible.
 */
public final class Network {
    public static final int RANGE_HIGH = 48;
    public static final int RANGE_MEDIUM = 32;
    public static final int RANGE_LOW = 16;
    private static final Logger LOGGER = LogManager.getLogger();

    // --------------------------------------------------------------------- //
    private static final int TICK_TIME = 50;


    // --------------------------------------------------------------------- //
    private static final Set<Position> particleQueue = new HashSet<>();
    private static final Stack<CasingSendQueue> queuePool = new Stack<>();
    private static final Map<CasingBlockEntity, CasingSendQueue> clientQueues = new HashMap<>();
    private static final Map<CasingBlockEntity, CasingSendQueue> serverQueues = new HashMap<>();
    private static long lastParticlesSent = 0;

    // --------------------------------------------------------------------- //
    private static int particlesSent = 0;
    private static int particleSendInterval = TICK_TIME;
    private static int packetsSentServer = 0;
    private static int packetsSentClient = 0;

    // --------------------------------------------------------------------- //
    // Particle message queueing
    private static int throttleServer = 0;
    private static int throttleClient = 0;

    private Network() {
    }

    public static void register(RegisterPayloadHandlersEvent e) {
        final PayloadRegistrar registrar = e.registrar("1");
        final PayloadRegistrar onMain = registrar.executesOn(HandlerThread.MAIN);

        onMain.playToClient(ServerCasingDataMessage.TYPE, ServerCasingDataMessage.STREAM_CODEC, ServerCasingDataMessage::handleMessage);
        onMain.playToClient(CasingEnabledStateMessage.TYPE, CasingEnabledStateMessage.STREAM_CODEC, CasingEnabledStateMessage::handleMessage);
        onMain.playToClient(CasingLockedStateMessage.TYPE, CasingLockedStateMessage.STREAM_CODEC, CasingLockedStateMessage::handleMessage);
        onMain.playToClient(CasingInventoryMessage.TYPE, CasingInventoryMessage.STREAM_CODEC, CasingInventoryMessage::handleMessage);
        onMain.playToClient(HaltAndCatchFireMessage.TYPE, HaltAndCatchFireMessage.STREAM_CODEC, HaltAndCatchFireMessage::handleMessage);
        onMain.playToClient(RedstoneParticleEffectMessage.TYPE, RedstoneParticleEffectMessage.STREAM_CODEC, RedstoneParticleEffectMessage::handleMessage);
        onMain.playToClient(ReceivingPipeLockedStateMessage.TYPE, ReceivingPipeLockedStateMessage.STREAM_CODEC, ReceivingPipeLockedStateMessage::handleMessage);
        onMain.playToClient(ServerReadOnlyMemoryModuleDataMessage.TYPE, ServerReadOnlyMemoryModuleDataMessage.STREAM_CODEC, ServerReadOnlyMemoryModuleDataMessage::handleMessage);
        onMain.playToClient(ControllerStateMessage.TYPE, ControllerStateMessage.STREAM_CODEC, ControllerStateMessage::handleMessage);
        onMain.playToClient(ServerCasingInitializeMessage.TYPE, ServerCasingInitializeMessage.STREAM_CODEC, ServerCasingInitializeMessage::handleMessage);

        onMain.playToServer(CodeBookDataMessage.TYPE, CodeBookDataMessage.STREAM_CODEC, CodeBookDataMessage::handleMessage);
        onMain.playToServer(ClientCasingDataMessage.TYPE, ClientCasingDataMessage.STREAM_CODEC, ClientCasingDataMessage::handleMessage);
        onMain.playToServer(ClientReadOnlyMemoryModuleDataMessage.TYPE, ClientReadOnlyMemoryModuleDataMessage.STREAM_CODEC, ClientReadOnlyMemoryModuleDataMessage::handleMessage);
        onMain.playToServer(ClientCasingLoadedMessage.TYPE, ClientCasingLoadedMessage.STREAM_CODEC, ClientCasingLoadedMessage::handleMessage);

        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post _e) -> {
            flushCasingQueues(Side.DEDICATED_SERVER);
            flushParticleQueue();
        });
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post _e) -> flushCasingQueues(Side.CLIENT));
    }

    public static void sendToPlayer(final ServerPlayer player, final AbstractMessage message) {
        PacketDistributor.sendToPlayer(player, message);
    }

    public static boolean sendToTrackingPlayers(final BlockEntity blockEntity, final AbstractMessage message) {
        final var level = blockEntity.getLevel();
        if (level == null) {
            return false;
        }

        // Avoids weird potential deadlock when loading into a single player world.
        if (!LevelUtils.isLoaded(level, blockEntity.getBlockPos())) {
            return false;
        }

        return sendToTrackingPlayers(level.getChunkAt(blockEntity.getBlockPos()), message);
    }

    public static boolean sendToTrackingPlayers(final LevelChunk chunk, final AbstractMessage message) {
        boolean didSend = false;
        if (chunk.getLevel().getChunkSource() instanceof final ServerChunkCache cache) {
            final var players = cache.chunkMap.getPlayers(chunk.getPos(), false);
            for (final ServerPlayer player : players) {
                sendToPlayer(player, message);
                didSend = true;
            }
        }
        return didSend;
    }

    public static void sendToNearbyPlayers(final BlockEntity blockEntity, final float range, final AbstractMessage message) {
        final Level level = blockEntity.getLevel();
        if (level != null) {
            sendToNearbyPlayers(level, Vec3.atCenterOf(blockEntity.getBlockPos()), range, message);
        }
    }

    public static boolean sendToNearbyPlayers(final Level level, final Vec3 pos, final float range, final AbstractMessage message) {
        final MinecraftServer server = level.getServer();
        if (server == null) {
            return false;
        }

        boolean didSend = false;
        for (final ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.level() != level) {
                continue;
            }
            final var distanceX = pos.x() - player.getX();
            final var distanceZ = pos.z() - player.getZ();
            if (distanceX * distanceX + distanceZ * distanceZ >= range * range) {
                continue;
            }

            sendToPlayer(player, message);
            didSend = true;
        }

        return didSend;
    }

    // --------------------------------------------------------------------- //
    // Module data metering

    public static void sendToServer(final AbstractMessage message) {
        ClientPacketDistributor.sendToServer(message);
    }

    public static void sendModuleData(final CasingBlockEntity casing, final Face face, final CompoundTag data, final byte type) {
        getQueueFor(casing).queueData(face, data, type);
    }

    public static void sendModuleData(final CasingBlockEntity casing, final Face face, final ByteBuf data, final byte type) {
        getQueueFor(casing).queueData(face, data, type);
    }

    public static void sendPipeEffect(final Level level, final double x, final double y, final double z) {
        final BlockPos position = BlockPos.containing(x, y, z);
        if (LevelUtils.isLoaded(level, position)) {
            final BlockState state = level.getBlockState(position);
            if (state.isSolidRender()) {
                // Skip particle emission when inside a block where they aren't visible anyway.
                return;
            }
        }

        queueParticleEffect(level, (float) x, (float) y, (float) z);
    }

    private static void queueParticleEffect(final Level level, final float x, final float y, final float z) {
        final Position position = new Position(level, x, y, z);
        particleQueue.add(position);
    }

    private static void flushParticleQueue() {
        final long now = System.currentTimeMillis();
        if (now - lastParticlesSent < particleSendInterval) {
            return;
        }
        lastParticlesSent = now;

        particlesSent = 0;
        particleQueue.forEach(Position::sendMessage);

        if (particlesSent > CommonConfig.maxParticlesPerTick) {
            final int throttle = (int) Math.ceil(particlesSent / (float) CommonConfig.maxParticlesPerTick);
            particleSendInterval = Math.min(2000, TICK_TIME * throttle);
        } else {
            particleSendInterval = TICK_TIME;
        }

        particleQueue.clear();
    }

    public static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> type(String name) {
        return new CustomPacketPayload.Type<>(API.resource(name));
    }

    private static int getPacketsSent(final Side side) {
        return side == Side.CLIENT ? packetsSentClient : packetsSentServer;
    }

    private static void resetPacketsSent(final Side side) {
        if (side == Side.CLIENT) {
            packetsSentClient = 0;
        } else {
            packetsSentServer = 0;
        }
    }

    private static void incrementPacketsSent(final Side side) {
        if (side == Side.CLIENT) {
            packetsSentClient++;
        } else {
            packetsSentServer++;
        }
    }

    private static int getThrottle(final Side side) {
        return side == Side.CLIENT ? throttleClient : throttleServer;
    }

    // --------------------------------------------------------------------- //
    // Module data queueing

    private static void setThrottle(final Side side, final int value) {
        if (side == Side.CLIENT) {
            throttleClient = value;
        } else {
            throttleServer = value;
        }
    }

    private static void decrementThrottle(final Side side) {
        if (side == Side.CLIENT) {
            throttleClient--;
        } else {
            throttleServer--;
        }
    }

    private static Map<CasingBlockEntity, CasingSendQueue> getQueues(final Side side) {
        if (side == Side.CLIENT) {
            return clientQueues;
        } else {
            return serverQueues;
        }
    }

    private static CasingSendQueue getQueueFor(final CasingBlockEntity casing) {
        final Level level = casing.getCasingLevel();
        final Side side = level.isClientSide() ? Side.CLIENT : Side.DEDICATED_SERVER;
        final Map<CasingBlockEntity, CasingSendQueue> queues = getQueues(side);
        CasingSendQueue queue = queues.get(casing);
        if (queue == null) {
            synchronized (queuePool) {
                if (!queuePool.isEmpty()) {
                    queue = queuePool.pop();
                } else {
                    queue = new CasingSendQueue();
                }
            }
            queues.put(casing, queue);
        }
        return queue;
    }

    private static void flushCasingQueues(final Side side) {
        if (getThrottle(side) > 0) {
            decrementThrottle(side);
            return;
        }

        resetPacketsSent(side);

        final Map<CasingBlockEntity, CasingSendQueue> queues = getQueues(side);
        queues.forEach(Network::flushCasingQueue);
        clearQueues(queues);

        final int sent = getPacketsSent(side);
        if (sent > CommonConfig.maxPacketsPerTick) {
            final int throttle = (int) Math.min(40, Math.ceil(sent / (float) CommonConfig.maxPacketsPerTick));
            setThrottle(side, throttle);
        }
    }

    private static void flushCasingQueue(final CasingBlockEntity casing, final CasingSendQueue queue) {
        queue.flush(casing);
    }

    private static void clearQueues(final Map<CasingBlockEntity, CasingSendQueue> queues) {
        synchronized (queuePool) {
            queuePool.addAll(queues.values());
        }
        queues.clear();
    }

    private enum Side {
        CLIENT, DEDICATED_SERVER
    }

    /**
     * Track dimensional position of particle emission for culling duplicates
     * when currently throttling.
     */
    private record Position(Level level, float x, float y, float z) {
        private void sendMessage() {
            final RedstoneParticleEffectMessage message = new RedstoneParticleEffectMessage(x, y, z);
            if (Network.sendToNearbyPlayers(level, new Vec3(x, y, z), RANGE_LOW, message)) {
                particlesSent++;
            }
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }

            final Position that = (Position) obj;
            return Objects.equals(level.dimension(), that.level.dimension()) && Float.compare(that.x, x) == 0 && Float.compare(that.y, y) == 0 && Float.compare(that.z, z) == 0;

        }

        @Override
        public int hashCode() {
            int result = level.dimension().hashCode();
            result = 31 * result + (x != 0.0f ? Float.floatToIntBits(x) : 0);
            result = 31 * result + (y != 0.0f ? Float.floatToIntBits(y) : 0);
            result = 31 * result + (z != 0.0f ? Float.floatToIntBits(z) : 0);
            return result;
        }
    }

    /**
     * Collects messages for a single casing.
     */
    private static final class CasingSendQueue {
        private final ModuleSendQueue[] moduleQueues = new ModuleSendQueue[Face.VALUES.length];

        private CasingSendQueue() {
            for (int i = 0; i < moduleQueues.length; i++) {
                moduleQueues[i] = new ModuleSendQueue();
            }
        }

        private void queueData(final Face face, final CompoundTag data, final byte type) {
            moduleQueues[face.ordinal()].queueData(data, type);
        }

        private void queueData(final Face face, final ByteBuf data, final byte type) {
            moduleQueues[face.ordinal()].queueData(data, type);
        }

        /**
         * Flush the casing's queue, sending all queued packets to clients.
         *
         * @param casing the casing this queue belongs to.
         */
        private void flush(final CasingBlockEntity casing) {
            final Level level = casing.getCasingLevel();
            final Side side = level.isClientSide() ? Side.CLIENT : Side.DEDICATED_SERVER;
            final ByteBuf data = Unpooled.buffer();
            collectData(data);
            if (data.readableBytes() > 0) {
                final boolean didSend;
                if (side == Side.CLIENT) {
                    final ClientCasingDataMessage message = new ClientCasingDataMessage(casing, data);
                    Network.sendToServer(message);
                    didSend = true;
                } else {
                    final ServerCasingDataMessage message = new ServerCasingDataMessage(casing, data);
                    didSend = Network.sendToTrackingPlayers(casing, message);
                }
                if (didSend) {
                    incrementPacketsSent(side);
                }
            }
        }

        private void collectData(final ByteBuf data) {
            for (int i = 0; i < moduleQueues.length; i++) {
                final ByteBuf moduleData = moduleQueues[i].collectData();
                if (moduleData.readableBytes() > 0) {
                    data.writeByte(i);
                    data.writeShort(moduleData.readableBytes());
                    data.writeBytes(moduleData);
                }
            }
        }
    }

    // --------------------------------------------------------------------- //

    /**
     * Collects messages for a single module.
     */
    private static final class ModuleSendQueue {
        private final List<QueueEntry> sendQueue = new ArrayList<>();
        private final BitSet sentTypes = new BitSet(0xFF);

        /**
         * Enqueue the specified data packet.
         *
         * @param data the data to enqueue.
         * @param type the type of the data.
         */
        private void queueData(final CompoundTag data, final byte type) {
            sendQueue.add(new QueueEntryCompoundTag(type, data));
        }

        /**
         * Enqueue the specified data packet.
         *
         * @param data the data to enqueue.
         * @param type the type of the data.
         */
        private void queueData(final ByteBuf data, final byte type) {
            sendQueue.add(new QueueEntryByteBuf(type, data));
        }

        /**
         * Collect all data in a tag list and clear the queue.
         *
         * @return the collected data for the module.
         */
        private ByteBuf collectData() {
            // Building the list backwards to easily use the last data of
            // any type without having to remove from the queue. However,
            // that could lead to sending different types in the reverse
            // they were queued in, so we first collect all packets to
            // actually send (by appending to the queue), and then sending
            // those selected packets -- in reverse again, to restore the
            // original order they were queued in.
            final ByteBuf data = Unpooled.buffer();
            final int firstToWrite = sendQueue.size();
            for (int i = sendQueue.size() - 1; i >= 0; i--) {
                final byte type = sendQueue.get(i).type;
                if (type >= 0) {
                    if (sentTypes.get(type)) {
                        continue;
                    }
                    sentTypes.set(type);
                }

                sendQueue.add(sendQueue.get(i));
            }
            for (int i = sendQueue.size() - 1; i >= firstToWrite; i--) {
                sendQueue.get(i).write(data);
            }

            sendQueue.clear();
            sentTypes.clear();

            return data;
        }

        /**
         * Base class for collected data packets.
         */
        private static abstract class QueueEntry {
            public final byte type;

            private QueueEntry(final byte type) {
                this.type = type;
            }

            /**
             * Serialize the queue entry into the specified byte buffer.
             *
             * @param buffer the buffer to write into.
             */
            public abstract void write(final ByteBuf buffer);
        }

        /**
         * Queue entry for pending tag data.
         */
        private static final class QueueEntryCompoundTag extends QueueEntry {
            public final CompoundTag data;

            private QueueEntryCompoundTag(final byte type, final CompoundTag data) {
                super(type);
                this.data = data;
            }

            @Override
            public void write(final ByteBuf buffer) {
                final ByteBuf data = Unpooled.buffer();
                try (final ByteBufOutputStream bos = new ByteBufOutputStream(data)) {
                    NbtIo.writeCompressed(this.data, bos);

                    if (data.readableBytes() > 0) {
                        buffer.writeBoolean(true);
                        buffer.writeShort(data.readableBytes());
                        buffer.writeBytes(data);
                    }
                } catch (final IOException e) {
                    LOGGER.warn("Failed sending packet.", e);
                }
            }
        }

        /**
         * Queue entry for pending raw data.
         */
        private static final class QueueEntryByteBuf extends QueueEntry {
            public final ByteBuf data;

            private QueueEntryByteBuf(final byte type, final ByteBuf data) {
                super(type);
                this.data = data;
            }

            @Override
            public void write(final ByteBuf buffer) {
                if (data.readableBytes() > 0) {
                    buffer.writeBoolean(false);
                    buffer.writeShort(data.readableBytes());
                    buffer.writeBytes(data);
                }
            }
        }
    }
}
