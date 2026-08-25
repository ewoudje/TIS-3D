package li.cil.tis3d.common.network.message;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.slf4j.Logger;


import javax.annotation.Nullable;

public abstract class AbstractMessage implements CustomPacketPayload {
    protected static final Logger LOGGER = LogUtils.getLogger();

    protected AbstractMessage() {
    }

    protected AbstractMessage(final RegistryFriendlyByteBuf buffer) {
        fromBytes(buffer);
    }

    // --------------------------------------------------------------------- //

    public abstract void handleMessage(final IPayloadContext context);

    public abstract void fromBytes(final RegistryFriendlyByteBuf buffer);

    public abstract void toBytes(final RegistryFriendlyByteBuf buffer);

    @Nullable
    protected Level getServerLevel(final IPayloadContext context) {
        final var sender = context.player();
        return sender != null ? sender.level() : null;
    }

    @Nullable
    protected Level getClientLevel() {
        return Minecraft.getInstance().level;
    }
}
