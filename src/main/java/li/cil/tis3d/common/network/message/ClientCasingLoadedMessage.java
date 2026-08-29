package li.cil.tis3d.common.network.message;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import li.cil.tis3d.common.network.Network;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueOutput;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ClientCasingLoadedMessage extends AbstractMessageWithPosition {
    public static final Type<ClientCasingLoadedMessage> TYPE = Network.type("casing_loaded");
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientCasingLoadedMessage> STREAM_CODEC = CustomPacketPayload.codec(
        ClientCasingLoadedMessage::toBytes,
        ClientCasingLoadedMessage::new
    );

    public ClientCasingLoadedMessage(final Casing casing) {
        super(casing.getPosition());
    }

    public ClientCasingLoadedMessage(final RegistryFriendlyByteBuf buffer) {
        super(buffer);
    }

    // --------------------------------------------------------------------- //
    // AbstractMessage

    @Override
    public void handleMessage(IPayloadContext context) {
        final var level = getServerLevel(context);
        if (level != null && context.player() instanceof ServerPlayer player) {
            withBlockEntity(level, CasingBlockEntity.class, casing -> {
                final var listTag = new ListTag();
                for (var face : Face.VALUES) {
                    final var module = casing.getModule(face);

                    try (var reporter = new ProblemReporter.ScopedCollector(LOGGER)) {
                        final var output = TagValueOutput.createWithContext(reporter, level.registryAccess());

                        if (module != null) {
                            module.save(output);
                        }

                        listTag.add(output.buildResult());
                    }

                }
                Network.sendToPlayer(player, new ServerCasingInitializeMessage(casing, listTag));
            });
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
