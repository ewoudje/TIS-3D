package li.cil.tis3d.common.network.message;

import li.cil.tis3d.common.network.Network;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface CasingMessage {
    Type<? extends CasingMessage> type();

    static <T extends CasingMessage> CasingMessage.Type<T> casingType(final String name) {
        return (isClient) -> Network.type("casing_" + name + (isClient ? "_c2s" : "_s2c"));
    }

    interface Type<T extends CasingMessage> {
        CustomPacketPayload.Type<? extends CustomPacketPayload> asType(boolean isClient);
    }
}
