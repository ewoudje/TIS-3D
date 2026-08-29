package li.cil.tis3d.common.network.message.s2c;

import io.netty.buffer.ByteBuf;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.common.block.entity.ControllerBlockEntity;
import li.cil.tis3d.common.network.Network;
import li.cil.tis3d.common.network.message.CasingMessage;
import li.cil.tis3d.common.network.message.ModuleMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

import static li.cil.tis3d.common.network.message.CasingMessage.casingType;
import static li.cil.tis3d.common.network.message.ModuleMessage.moduleType;

public class S2CMessages {

    public record PipeParticle(double x, double y, double z) implements CustomPacketPayload, S2CMessage {
        public static final Type<PipeParticle> TYPE = Network.type("pipe_particle");
        public static final StreamCodec<? super ByteBuf, PipeParticle> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, PipeParticle::x,
            ByteBufCodecs.DOUBLE, PipeParticle::y,
            ByteBufCodecs.DOUBLE, PipeParticle::z,
            PipeParticle::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record HaltCatchFire(BlockPos pos) implements CustomPacketPayload, S2CMessage {
        public static final Type<HaltCatchFire> TYPE = Network.type("halt_catch_fire");
        public static final StreamCodec<? super ByteBuf, HaltCatchFire> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, HaltCatchFire::pos,
            HaltCatchFire::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ControllerState(BlockPos pos, ControllerBlockEntity.ControllerState state) implements CustomPacketPayload, S2CMessage {
        public static final Type<ControllerState> TYPE = Network.type("controller_state");
        public static final StreamCodec<? super ByteBuf, ControllerState> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ControllerState::pos,
            ControllerBlockEntity.ControllerState.STREAM_CODEC, ControllerState::state,
            ControllerState::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record Initialize(Tag list) implements CasingMessage, S2CMessage {
        public static final Type<Initialize> TYPE = casingType("initialize");
        public static final StreamCodec<? super ByteBuf, Initialize> STREAM_CODEC =
            ByteBufCodecs.TAG.map(Initialize::new, Initialize::list);

        @Override
        public Type<Initialize> type() {
            return TYPE;
        }
    }

    public record EnabledState(boolean isEnabled) implements CasingMessage, S2CMessage {
        public static final Type<EnabledState> TYPE = casingType("enabled_state");
        public static final StreamCodec<? super ByteBuf, EnabledState> STREAM_CODEC =
            ByteBufCodecs.BOOL.map(EnabledState::new, EnabledState::isEnabled);

        @Override
        public Type<EnabledState> type() {
            return TYPE;
        }
    }

    public record Inventory(int slot, ItemStack stack, CompoundTag moduleData) implements CasingMessage, S2CMessage {
        public static final Type<Inventory> TYPE = casingType("inventory");
        public static final StreamCodec<? super RegistryFriendlyByteBuf, Inventory> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, Inventory::slot,
            ItemStack.OPTIONAL_STREAM_CODEC, Inventory::stack,
            ByteBufCodecs.COMPOUND_TAG, Inventory::moduleData,
            Inventory::new
        );

        @Override
        public Type<Inventory> type() {
            return TYPE;
        }
    }

    public record LockedState(boolean isLocked) implements CasingMessage, S2CMessage {
        public static final Type<LockedState> TYPE = casingType("locked_state");
        public static final StreamCodec<? super ByteBuf, LockedState> STREAM_CODEC =
            ByteBufCodecs.BOOL.map(LockedState::new, LockedState::isLocked);

        @Override
        public Type<LockedState> type() {
            return TYPE;
        }
    }

    public record PipeLockedState(Port port, boolean isLocked) implements ModuleMessage, S2CMessage {
        public static final Type<PipeLockedState> TYPE = moduleType("pipe_locked_state");
        public static final StreamCodec<? super ByteBuf, PipeLockedState> STREAM_CODEC = StreamCodec.composite(
            Port.STREAM_CODEC, PipeLockedState::port,
            ByteBufCodecs.BOOL, PipeLockedState::isLocked,
            PipeLockedState::new
        );

        @Override
        public Type<PipeLockedState> type() {
            return TYPE;
        }
    }
}
