package li.cil.tis3d.common.module;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.prefab.module.AbstractModuleWithRotation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * The timer module can be used to wait for a specific amount of game time.
 * It is configured by writing a value to any of its ports, and can be
 * waited on by reading from any of its port - which will only be written to
 * once the timer has expired (reached zero).
 * <p>
 * This module will receive data on all ports and push back a value while the
 * timer is zero.
 */
public final class TimerModule extends AbstractModuleWithRotation {
    // --------------------------------------------------------------------- //
    // Persisted data

    // NBT data names.
    private static final String TAG_TIMER = "timer";

    // --------------------------------------------------------------------- //
    // Computed data
    // Data packet types.
    private static final byte DATA_TYPE_UPDATE = 0;
    // The value written to all ports once the timer has reached zero.
    private static final short OUTPUT_VALUE = 1;
    // The game time the timer elapses at.
    private long timer;
    // Cached elapsed state.
    private boolean hasElapsed;

    // --------------------------------------------------------------------- //

    public TimerModule(final Casing casing, final Face face) {
        super(casing, face);
    }

    // --------------------------------------------------------------------- //
    // Module

    @Override
    public void step() {
        if (!hasElapsed) {
            final long gameTime = getCasing().getCasingLevel().getGameTime();
            if (gameTime >= timer) {
                hasElapsed = true;
            }
        }

        stepOutput();
        stepInput();
    }

    @Override
    public void onDisabled() {
        // Clear timer on shutdown.
        timer = 0L;
        hasElapsed = true;

        sendData();
    }

    @Override
    public void onWriteComplete(final Port port) {
        // Start writing again right away to write as fast as possible.
        stepOutput();
    }

    @Override
    public void onData(final ByteBuf data) {
        timer = data.readLong();
        hasElapsed = false; // Recompute in render().
    }

    @Override
    public void load(final ValueInput input) {
        super.load(input);

        timer = input.getLongOr(TAG_TIMER, 0);
    }

    @Override
    public void save(final ValueOutput output) {
        super.save(output);

        output.putLong(TAG_TIMER, timer);
    }

    // --------------------------------------------------------------------- //

    public boolean hasElapsed() {
        return hasElapsed;
    }

    public long getTimer() {
        return timer;
    }

    /**
     * Set the timer to the specified value.
     *
     * @param value the value to set the timer to.
     */
    private void setTimer(final short value) {
        final long gameTime = getCasing().getCasingLevel().getGameTime();
        timer = gameTime + (value & 0xFFFF);
        hasElapsed = timer == gameTime;

        if (!hasElapsed) {
            cancelWrite();
        }

        sendData();

        getCasing().setChanged();
    }

    public void elapsed() {
        hasElapsed = true;
    }

    /**
     * Update the outputs of the timer, pushing a value if it has elapsed.
     */
    private void stepOutput() {
        // Don't write if the timer is still running.
        if (!hasElapsed) {
            return;
        }

        for (final Port port : Port.VALUES) {
            final Pipe sendingPipe = getCasing().getSendingPipe(getFace(), port);
            if (!sendingPipe.isWriting()) {
                sendingPipe.beginWrite(OUTPUT_VALUE);
            }
        }
    }

    /**
     * Update the inputs of the timer, setting the new timer value to read values.
     */
    private void stepInput() {
        for (final Port port : Port.VALUES) {
            // Continuously read from all ports, set timer to last received value.
            final Pipe receivingPipe = getCasing().getReceivingPipe(getFace(), port);
            if (!receivingPipe.isReading()) {
                receivingPipe.beginRead();
            }
            if (receivingPipe.canTransfer()) {
                // Set the value.
                setTimer(receivingPipe.read());
            }
        }
    }

    private void sendData() {
        final ByteBuf data = Unpooled.buffer();
        data.writeLong(timer);
        getCasing().sendData(getFace(), data, DATA_TYPE_UPDATE);
    }
}
