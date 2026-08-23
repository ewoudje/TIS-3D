package li.cil.tis3d.common.entity;


import li.cil.tis3d.api.API;
import li.cil.tis3d.util.RegistryUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

public final class Entities {
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = RegistryUtils.getDeferred(Registries.ENTITY_TYPE);

    // --------------------------------------------------------------------- //

    public static final DeferredHolder<EntityType<?>, EntityType<InfraredPacketEntity>> INFRARED_PACKET = register("infrared_packet",
        InfraredPacketEntity::new,
        MobCategory.MISC,
        b -> b
            .sized(0.25f, 0.25f)
            .clientTrackingRange(16)
            .updateInterval(1)
            .canSpawnFarFromPlayer()
            .fireImmune()
            .noSummon()
    );

    // --------------------------------------------------------------------- //

    public static void initialize(IEventBus bus) {
        ENTITY_TYPES.register(bus);
    }

    // --------------------------------------------------------------------- //

    private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> register(final String name, final EntityType.EntityFactory<T> factory, final MobCategory classification, final Function<EntityType.Builder<T>, EntityType.Builder<T>> customizer) {
        return ENTITY_TYPES.register(name,
            () -> customizer.apply(EntityType.Builder.of(factory, classification)).build(ResourceKey.create(Registries.ENTITY_TYPE, API.resource(name)))
        );
    }
}
