package li.cil.tis3d.common.network.message;

import li.cil.tis3d.common.network.Network;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface ModuleMessage {
    Type<? extends ModuleMessage> type();

    static <T extends ModuleMessage> ModuleMessage.Type<T> moduleType(final String name) {
        return (isClient) -> Network.type("module_" + name + (isClient ? "_c2s" : "_s2c"));
    }

    interface Type<T extends ModuleMessage> {
        CustomPacketPayload.Type<? extends CustomPacketPayload> asType(boolean isClient);
    }
}
