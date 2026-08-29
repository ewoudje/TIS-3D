package li.cil.tis3d.common.network;

import li.cil.tis3d.api.API;
import li.cil.tis3d.common.network.message.c2s.C2SHandler;
import li.cil.tis3d.common.network.message.s2c.S2CHandler;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class Network {
    public static final S2CHandler S2C = FMLEnvironment.getDist() == Dist.CLIENT ? new S2CHandler() : null;
    public static final C2SHandler C2S = new C2SHandler();

    private Network() {
    }

    public static void register(RegisterPayloadHandlersEvent e) {
        final PayloadRegistrar registrar = e.registrar("2");

        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            S2C.register(registrar);
        }

        //TODO reimpl packet batching

        C2S.register(registrar);
    }

    public static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> type(String name) {
        return new CustomPacketPayload.Type<>(API.resource(name));
    }
}
