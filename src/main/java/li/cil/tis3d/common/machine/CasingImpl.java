package li.cil.tis3d.common.machine;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.ModuleProvider;
import li.cil.tis3d.api.module.traits.ModuleWithRedstone;
import li.cil.tis3d.client.renderer.block.entity.CasingBlockEntityRenderer;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import li.cil.tis3d.common.block.entity.ControllerBlockEntity;
import li.cil.tis3d.common.item.DataComponentTypes;
import li.cil.tis3d.common.item.Items;
import li.cil.tis3d.common.network.Network;
import li.cil.tis3d.common.provider.ModuleProviders;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Implementation of a {@link Casing}, holding up to six {@link Module}s.
 */
public final class CasingImpl implements Casing {
    private static final Logger LOGGER = LogUtils.getLogger();

    // --------------------------------------------------------------------- //
    // Persisted data.

    // NBT tag names.
    private static final String TAG_MODULES = "modules";
    private static final String TAG_KEY = "key";

    // --------------------------------------------------------------------- //
    // Computed data.
    /**
     * The {@link Module}s currently installed in this {@link Casing}.
     */
    private final Module[] modules = new Module[Face.VALUES.length];
    /**
     * The tile entity hosting this casing.
     */
    private final CasingBlockEntity blockEntity;
    /**
     * The key the casing is currently locked with. If this is set, players
     * cannot add or remove modules from the casing. A key with the correct
     * UUID in its tag is required to unlock a casing.
     */
    private UUID lock = null;

    // --------------------------------------------------------------------- //

    public CasingImpl(final CasingBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    /**
     * Calls {@link Module#onEnabled()} on all modules.
     * <p>
     * Used by the controller when its state changes to {@link ControllerBlockEntity.ControllerState#RUNNING}.
     */
    public void onEnabled() {
        for (final Module module : modules) {
            if (module != null) {
                module.onEnabled();
            }
        }
        setChanged();
    }

    /**
     * Calls {@link Module#onDisabled()} on all modules and resets all pipes.
     * <p>
     * Used by the controller when its state changes from {@link ControllerBlockEntity.ControllerState#RUNNING},
     * or the controller is reset (scan scheduled), or the controller is unloaded.
     */
    public void onDisabled() {
        for (final Module module : modules) {
            if (module != null) {
                module.onDisabled();
            }
        }
        for (final Pipe pipe : blockEntity.getPipes()) {
            pipe.cancelRead();
            pipe.cancelWrite();
        }
        setChanged();
    }

    /**
     * Calls {@link Module#onDisposed()} on all modules.
     * <p>
     * Used by the casing when it is being unloaded.
     */
    public void onDisposed() {
        for (final Module module : modules) {
            if (module != null) {
                module.onDisposed();
            }
        }
    }

    /**
     * Advance the logic of all modules by calling {@link Module#step()} on them.
     */
    public void stepModules() {
        for (final Module module : modules) {
            if (module != null) {
                module.step();
            }
        }
    }

    /**
     * Set the module for the specified face of the casing.
     * <p>
     * This is automatically called by the casing tile entity when items are
     * added or removed and as a special case directly for forwarder modules.
     * <p>
     * This calls {@link Module#onEnabled()} and {@link Module#onDisabled()}
     * appropriately if the casing is enabled or disabled, respectively.
     *
     * @param face   the face to install the module on.
     * @param module the module to install on the face, or {@code null} for none.
     */
    public void setModule(final Face face, @Nullable final Module module) {
        if (getModule(face) == module) {
            return;
        }

        // End-of-life notification for module if it was active.
        final Module oldModule = getModule(face);
        if (blockEntity.isEnabled() && oldModule != null && !getCasingLevel().isClientSide()) {
            oldModule.onDisabled();
        }

        // Remember for below.
        final boolean hadRedstone = oldModule instanceof ModuleWithRedstone;

        // Apply new module before adjust remaining state.
        modules[face.ordinal()] = module;

        // Reset redstone output if the previous module was redstone capable.
        if (hadRedstone) {
            if (!getCasingLevel().isClientSide()) {
                blockEntity.setChanged();
                getCasingLevel().updateNeighborsAt(getPosition(), blockEntity.getBlockState().getBlock());
            }
        }

        // Reset pipe state if module is removed. Don't reset when one is set,
        // because it might be set via a load or scan, in which case we
        // absolutely do not want to reset our state!
        if (module == null) {
            for (final Port port : Port.VALUES) {
                getReceivingPipe(face, port).cancelRead();
                getSendingPipe(face, port).cancelWrite();
            }
        }

        // Activate the module if the controller is active.
        if (blockEntity.isEnabled() && module != null && !getCasingLevel().isClientSide()) {
            module.onEnabled();
        }

        if (getCasingLevel().isClientSide())
            CasingBlockEntityRenderer.update(blockEntity);

        blockEntity.setChanged();
    }

    /**
     * Locks the casing and returns the key for unlocking it.
     *
     * @param stack the item to store the key for unlocking on.
     * @throws IllegalStateException if the casing is already locked.
     */
    public void lock(final ItemStack stack) {
        if (isLocked()) {
            throw new IllegalStateException("Casing is already locked.");
        }
        if (Items.is(stack, Items.KEY_CREATIVE)) {
            lock = UUID.randomUUID();
        } else {
            final UUID key = stack.getOrDefault(DataComponentTypes.KEY_COMPONENT, UUID.randomUUID());
            stack.set(DataComponentTypes.KEY_COMPONENT, key);
            lock = key;
        }
    }

    /**
     * Try to unlock the casing with the key stored on the specified item.
     *
     * @param stack the item containing the key.
     * @return <code>true</code> if the casing was successfully unlocked; <code>false</code> otherwise.
     */
    public boolean unlock(final ItemStack stack) {
        if (Items.is(stack, Items.KEY_CREATIVE)) {
            lock = null;
            return true;
        } else {
            var uuid = stack.get(DataComponentTypes.KEY_COMPONENT);
            return uuid != null && unlock(uuid);
        }
    }

    /**
     * Try to unlock the casing with the specified key.
     *
     * @param key the key to use to unlock the casing.
     * @return <code>true</code> if the casing was successfully unlocked; <code>false</code> otherwise.
     */
    public boolean unlock(final UUID key) {
        if (key.equals(lock)) {
            lock = null;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Restore data of all modules and pipes from the specified tag.
     */
    public void load(final ValueInput input) {
        for (int index = 0; index < blockEntity.getContainerSize(); index++) {
            // We replace *all* modules to be sure we have the right types in the right slots,
            // so make sure we dispose the old instances we may have, first.
            if (modules[index] != null) {
                modules[index].onDisposed();
            }

            final ItemStack stack = blockEntity.getItem(index);
            if (stack.isEmpty()) {
                modules[index] = null;
                continue;
            }

            final Face face = Face.VALUES[index];
            final Optional<ModuleProvider> provider = ModuleProviders.getProviderFor(stack, blockEntity, face);
            if (provider.isEmpty()) {
                modules[index] = null;
                continue;
            }

            final Module module = provider.get().createModule(stack, blockEntity, face);
            modules[index] = module;
        }

        final var modulesData = input.childrenList(TAG_MODULES);
        if (modulesData.isPresent()) {
            var iter = modulesData.get().iterator();
            for (Module module : modules) {
                if (module != null && iter.hasNext()) {
                    module.load(iter.next());
                }
            }
        }

        lock = input.read(TAG_KEY, UUIDUtil.CODEC).orElse(null);
    }

    /**
     * Write the state of all modules and pipes to the specified tag.
     */
    public void save(final ValueOutput input) {
        var modulesData = input.childrenList(TAG_MODULES);
        for (final Module module : modules) {
            if (module != null) {
                module.save(modulesData.addChild());
            }
        }

        input.storeNullable(TAG_KEY, UUIDUtil.CODEC, lock);
    }

    @Override
    public Level getCasingLevel() {
        return blockEntity.getBlockEntityLevel();
    }

    // --------------------------------------------------------------------- //
    // Casing

    @Override
    public BlockPos getPosition() {
        return blockEntity.getBlockPos();
    }

    @Override
    public void setChanged() {
        blockEntity.setChanged();
    }

    @Override
    public boolean isEnabled() {
        return blockEntity.isCasingEnabled();
    }

    @Override
    public boolean isLocked() {
        return lock != null;
    }

    public void setLocked(final boolean locked) {
        if (locked) {
            lock = UUID.randomUUID();
        } else {
            lock = null;
        }
    }

    @Override
    @Nullable
    public Module getModule(final Face face) {
        return modules[face.ordinal()];
    }

    @Override
    public Pipe getReceivingPipe(final Face face, final Port port) {
        return blockEntity.getReceivingPipe(face, port);
    }

    @Override
    public Pipe getSendingPipe(final Face face, final Port port) {
        return blockEntity.getSendingPipe(face, port);
    }

    @Override
    public void sendData(final Face face, final CompoundTag data, final byte type) {
        Network.sendModuleData(blockEntity, face, data, type);
    }

    @Override
    public void sendData(final Face face, final CompoundTag data) {
        sendData(face, data, (byte) -1);
    }

    @Override
    public void sendData(Face face, Consumer<ValueOutput> data, byte type) {
        try (var reporter = new ProblemReporter.ScopedCollector(LOGGER)) {
            var output = TagValueOutput.createWithContext(reporter, blockEntity.getCasingLevel().registryAccess());
            data.accept(output);
            sendData(face, output.buildResult(), type);
        }
    }

    @Override
    public void sendData(Face face, Consumer<ValueOutput> data) {
        sendData(face, data, (byte) -1);
    }

    @Override
    public void sendData(final Face face, final ByteBuf data, final byte type) {
        Network.sendModuleData(blockEntity, face, data, type);
    }

    @Override
    public void sendData(final Face face, final ByteBuf data) {
        sendData(face, data, (byte) -1);
    }
}
