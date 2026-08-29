package li.cil.tis3d.common.network.message.c2s;

import io.netty.buffer.ByteBuf;
import li.cil.tis3d.common.item.CodeBookItem;
import li.cil.tis3d.common.network.Network;
import li.cil.tis3d.common.network.message.CasingMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;

import static li.cil.tis3d.common.network.message.CasingMessage.casingType;

public class C2SMessages {

    public record CodeBook(CodeBookItem.Data data, InteractionHand hand) implements CustomPacketPayload, C2SMessage {
        public static final Type<CodeBook> TYPE = Network.type("codebook");
        public static final StreamCodec<FriendlyByteBuf, CodeBook> STREAM_CODEC = StreamCodec.composite(
            CodeBookItem.Data.STREAM_CODEC, CodeBook::data,
            ByteBufCodecs.BOOL.map(
                b -> b ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND,
                h -> h == InteractionHand.MAIN_HAND
            ), CodeBook::hand,
            CodeBook::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record Loaded() implements CasingMessage, C2SMessage {
        public static final CasingMessage.Type<Loaded> TYPE = casingType("loaded");
        public static final StreamCodec<ByteBuf, Loaded> STREAM_CODEC =
            StreamCodec.of((b, c) -> {}, b -> new Loaded());

        @Override
        public CasingMessage.Type<Loaded> type() {
            return TYPE;
        }
    }
}
