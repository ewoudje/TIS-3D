package li.cil.tis3d.common.network.message.common;

import io.netty.buffer.ByteBuf;
import li.cil.tis3d.common.network.Network;
import li.cil.tis3d.common.network.message.ModuleMessage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;

import static li.cil.tis3d.common.network.message.ModuleMessage.moduleType;

public class CommonMessages {

    public record ROMData(byte[] bytes, InteractionHand hand) implements CustomPacketPayload, CommonMessage {
        public static final Type<ROMData> C2S_TYPE = Network.type("rom_data_c2s");
        public static final Type<ROMData> S2C_TYPE = Network.type("rom_data_s2c");
        public static final Type<ROMData> TYPE = Network.type("rom_data");

        public static final StreamCodec<ByteBuf, ROMData> STREAM_CODEC =
            StreamCodec.composite(
                ByteBufCodecs.BYTE_ARRAY, ROMData::bytes,
                ByteBufCodecs.BOOL.map(
                    b -> b ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND,
                    h -> h == InteractionHand.MAIN_HAND
                ), ROMData::hand,
                ROMData::new
            );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ModuleNBTData(CompoundTag tag) implements ModuleMessage, CommonMessage {
        public static final Type<ModuleNBTData> TYPE = moduleType("nbt_data");
        public static final StreamCodec<ByteBuf, ModuleNBTData> STREAM_CODEC =
            ByteBufCodecs.COMPOUND_TAG.map(ModuleNBTData::new, ModuleNBTData::tag);

        @Override
        public Type<ModuleNBTData> type() {
            return TYPE;
        }
    }

    public record ModuleByteData(ByteBuf buf) implements ModuleMessage, CommonMessage {
        public static final Type<ModuleByteData> TYPE = moduleType("byte_data");
        public static final StreamCodec<ByteBuf, ModuleByteData> STREAM_CODEC =
            StreamCodec.of((buf, data) -> {
                buf.writeInt(data.buf.readableBytes());
                buf.writeBytes(data.buf);
            }, (buf) -> {
                int length = buf.readInt();
                return new ModuleByteData(buf.readBytes(length));
            });

        @Override
        public Type<ModuleByteData> type() {
            return TYPE;
        }
    }
}
