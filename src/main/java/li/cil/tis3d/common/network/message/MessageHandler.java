package li.cil.tis3d.common.network.message;

import com.mojang.logging.LogUtils;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import li.cil.tis3d.common.network.message.common.CommonMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.apache.logging.log4j.util.TriConsumer;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class MessageHandler<M> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final List<HandlerEntry<Consumer<? extends CustomPacketPayload>, ? extends CustomPacketPayload>> globalHandlers = new ArrayList<>();
    private final List<HandlerEntry<BiConsumer<CasingBlockEntity, ? extends CasingMessage>, ? extends CasingMessage>> casingHandlers = new ArrayList<>();
    private final List<HandlerEntry<TriConsumer<CasingBlockEntity, Module, ? extends ModuleMessage>, ? extends ModuleMessage>> moduleHandlers = new ArrayList<>();
    private final boolean isClient;
    private ProblemReporter.ScopedCollector currentReporter;
    private Player player;

    protected MessageHandler(boolean isClient) {
        this.isClient = isClient;
    }

    public void register(PayloadRegistrar iRegistrar) {
        PayloadRegistrar registrar = iRegistrar.executesOn(HandlerThread.MAIN);
        globalHandlers.forEach(e -> {
            if (isClient) {
                registrar.playToClient((CustomPacketPayload.Type) e.type(), (StreamCodec) e.codec(), handleAnyMessage(e.consumer()));
            } else {
                registrar.playToServer((CustomPacketPayload.Type) e.type(), (StreamCodec) e.codec(), handleAnyMessage(e.consumer()));
            }
        });

        casingHandlers.forEach(e -> {
            var type = ((CasingMessage.Type) e.type()).asType(isClient);
            var codec = (StreamCodec) casingCodec(e.codec());
            var message = (IPayloadHandler) handleCasingMessage(e.consumer());
            if (isClient) {
                registrar.playToClient(type, codec, message);
            } else {
                registrar.playToServer(type, codec, message);
            }
        });

        moduleHandlers.forEach(e -> {
            var type = ((ModuleMessage.Type) e.type()).asType(isClient);
            var codec = (StreamCodec) moduleCodec(e.codec());
            var message = (IPayloadHandler) handleModuleMessage(e.consumer());
            if (isClient) {
                registrar.playToClient(type, codec, message);
            } else {
                registrar.playToServer(type, codec, message);
            }
        });
    }



    private <T extends CustomPacketPayload> IPayloadHandler<T> handleAnyMessage(Consumer<T> consumer) {
        return (p, ctx) -> {
            try {
                currentReporter = new ProblemReporter.ScopedCollector(LOGGER);
                player = ctx.player();
                consumer.accept(p);
            } catch (Exception e) {
                LOGGER.error("Failed to handle {} message", isClient ? "s2c" : "c2s", e);
            } finally {
                currentReporter.close();
                currentReporter = null;
            }
        };
    }


    private <T extends CasingMessage> StreamCodec<? super FriendlyByteBuf, WrappedCasingMessage<T>> casingCodec(StreamCodec<? super FriendlyByteBuf, T> codec) {
        return StreamCodec.composite(
            codec, WrappedCasingMessage::message,
            BlockPos.STREAM_CODEC, WrappedCasingMessage::pos,
            WrappedCasingMessage::new
        );
    }

    private <T extends CasingMessage> IPayloadHandler<WrappedCasingMessage<T>> handleCasingMessage(BiConsumer<CasingBlockEntity, T> consumer) {
        return handleAnyMessage(m -> {
            Level level = getPlayer().level();
            BlockEntity be = level.getBlockEntity(m.pos);
            if (!(be instanceof CasingBlockEntity casing)) {
                LOGGER.error("Received casing message for invalid block entity: {}", m.pos);
                return;
            }

            consumer.accept(casing, m.message());
        });
    }

    private <T extends ModuleMessage> StreamCodec<? super FriendlyByteBuf, WrappedCasingMessage<WrappedModuleMessage<T>>> moduleCodec(StreamCodec<? super FriendlyByteBuf, T> codec) {
        return casingCodec(StreamCodec.composite(
            codec, WrappedModuleMessage::message,
            Face.STREAM_CODEC, WrappedModuleMessage::face,
            WrappedModuleMessage::new
        ));
    }

    private <T extends ModuleMessage> IPayloadHandler<WrappedCasingMessage<WrappedModuleMessage<T>>> handleModuleMessage(TriConsumer<CasingBlockEntity, Module, T> consumer) {
        return handleCasingMessage((c, wrappedMsg) -> {
            Module m = c.getModule(wrappedMsg.face());
            if (m == null) throw new IllegalStateException("Module not found for face " + wrappedMsg.face());

            consumer.accept(c, m, wrappedMsg.message());
        });
    }

    protected ProblemReporter reporter() {
        return currentReporter;
    }

    protected Player getPlayer() {
        return player;
    }

    protected RegistryAccess registryAccess() {
        return player.level().registryAccess();
    }

    protected <T extends CustomPacketPayload> void globalHandler(
        CustomPacketPayload.Type<T> type,
        Consumer<T> consumer,
        StreamCodec<? super FriendlyByteBuf, T> codec
    ) {
        globalHandlers.add(new HandlerEntry<>(type, consumer, codec));
    }

    protected <T extends CasingMessage> void casingHandler(
        CasingMessage.Type<T> type,
        BiConsumer<CasingBlockEntity, T> consumer,
        StreamCodec<? super FriendlyByteBuf, T> codec
    ) {
        casingHandlers.add(new HandlerEntry<>(type, consumer, codec));
    }

    protected <T extends ModuleMessage> void moduleHandler(
        ModuleMessage.Type<T> type,
        TriConsumer<CasingBlockEntity, Module, T> consumer,
        StreamCodec<? super FriendlyByteBuf, T> codec
    ) {
        moduleHandlers.add(new HandlerEntry<>(type, consumer, codec));
    }

    private record HandlerEntry<C, T>(
        Object type,
        C consumer,
        StreamCodec<? super FriendlyByteBuf, T> codec
    ) {}

    public <T extends CasingMessage> WrappedCasingMessage<T> wrap(T message, BlockPos pos) {
        return new WrappedCasingMessage<>(message, pos);
    }

    public class WrappedCasingMessage<T extends CasingMessage> implements CustomPacketPayload, CommonMessage {
        private final T message;
        private final BlockPos pos;

        public WrappedCasingMessage(T message, BlockPos pos) {
            this.message = message;
            this.pos = pos;
        }

        public T message() {
            return message;
        }

        public BlockPos pos() {
            return pos;
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return message.type().asType(isClient);
        }
    }

    public record WrappedModuleMessage<T extends ModuleMessage>(T message, Face face) implements CasingMessage, CommonMessage {

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return (isClient) -> message.type().asType(isClient);
        }
    }
}
