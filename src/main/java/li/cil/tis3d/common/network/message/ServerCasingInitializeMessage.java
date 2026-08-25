package li.cil.tis3d.common.network.message;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import li.cil.tis3d.common.network.Network;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static java.util.Objects.requireNonNull;

public final class ServerCasingInitializeMessage extends AbstractMessageWithPosition {
    public static final CustomPacketPayload.Type<ServerCasingInitializeMessage> TYPE = Network.type("casing_initialize");
    private static final String MODULES_TAG = "modules";
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerCasingInitializeMessage> STREAM_CODEC = CustomPacketPayload.codec(
        ServerCasingInitializeMessage::toBytes,
        ServerCasingInitializeMessage::new
    );
    private ListTag tag;

    public ServerCasingInitializeMessage(final Casing casing, final ListTag tag) {
        super(casing.getPosition());
        this.tag = tag;
    }

    public ServerCasingInitializeMessage(final RegistryFriendlyByteBuf buffer) {
        super(buffer);
    }

    // --------------------------------------------------------------------- //
    // AbstractMessage

    @Override
    public void handleMessage(IPayloadContext context) {
        final var level = getClientLevel();
        if (level != null) {
            withBlockEntity(level, CasingBlockEntity.class, casing -> {
                for (int i = 0; i < Face.VALUES.length; i++) {
                    final var face = Face.VALUES[i];
                    final var moduleTag = tag.getCompoundOrEmpty(i);
                    final var module = casing.getModule(face);
                    if (module != null) {
                        try (var reporter = new ProblemReporter.ScopedCollector(LOGGER)) {
                            module.load(TagValueInput.create(reporter, level.registryAccess(), moduleTag));
                        }
                    }
                }
            });
        }
    }

    @Override
    public void fromBytes(RegistryFriendlyByteBuf buffer) {
        super.fromBytes(buffer);

        final var wrapper = requireNonNull(buffer.readNbt());
        tag = wrapper.getListOrEmpty(MODULES_TAG);
    }

    @Override
    public void toBytes(RegistryFriendlyByteBuf buffer) {
        super.toBytes(buffer);

        final var wrapper = new CompoundTag();
        wrapper.put(MODULES_TAG, tag);
        buffer.writeNbt(wrapper);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
