package li.cil.tis3d.common.network.message;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import li.cil.tis3d.common.network.Network;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ReceivingPipeLockedStateMessage extends AbstractMessageWithPosition {
    public static final CustomPacketPayload.Type<ReceivingPipeLockedStateMessage> TYPE = Network.type("receving_pipe_locked_state");
    public static final StreamCodec<RegistryFriendlyByteBuf, ReceivingPipeLockedStateMessage> STREAM_CODEC = CustomPacketPayload.codec(
        ReceivingPipeLockedStateMessage::toBytes,
        ReceivingPipeLockedStateMessage::new
    );

    private Face face;
    private Port port;
    private boolean isLocked;

    public ReceivingPipeLockedStateMessage(final Casing casing, final Face face, final Port port, final boolean isLocked) {
        super(casing.getPosition());
        this.face = face;
        this.port = port;
        this.isLocked = isLocked;
    }

    @SuppressWarnings("unused") // For deserialization.
    public ReceivingPipeLockedStateMessage(final RegistryFriendlyByteBuf buffer) {
        super(buffer);
    }

    // --------------------------------------------------------------------- //

    @Override
    public void handleMessage(final IPayloadContext context) {
        final Level level = getClientLevel();
        if (level != null) {
            withBlockEntity(level, CasingBlockEntity.class, casing ->
                casing.setReceivingPipeLockedClient(face, port, isLocked));
        }
    }

    // We can nicely compress the data of this message into one byte:
    // - Face can have 6 values, needs 3 bits.
    // - Port can have 4 values, needs 2 bits.
    // - Locked can have 2 values, needs 1 bit.
    // It's an infrequent message, so this is totally overkill. But it's fun!

    @Override
    public void fromBytes(final RegistryFriendlyByteBuf buffer) {
        super.fromBytes(buffer);

        final byte compressed = buffer.readByte();
        face = Face.VALUES[(compressed >>> 3) & 0b111];
        port = Port.VALUES[(compressed >>> 1) & 0b11];
        isLocked = (compressed & 0b1) == 1;
    }

    @Override
    public void toBytes(final RegistryFriendlyByteBuf buffer) {
        super.toBytes(buffer);

        final byte compressed = (byte) ((face.ordinal() << 3) |
            (port.ordinal() << 1) |
            (isLocked ? 1 : 0));
        buffer.writeByte(compressed);
    }


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
