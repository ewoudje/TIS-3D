package li.cil.tis3d.common.module;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.traits.ModuleWithRedstone;
import li.cil.tis3d.api.prefab.module.AbstractModuleWithRotation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class RedstoneModule extends AbstractModuleWithRotation implements ModuleWithRedstone {
    // --------------------------------------------------------------------- //
    // Persisted data

    // NBT tag names.
    private static final String TAG_OUTPUT = "output";
    private static final String TAG_INPUT = "input";

    // --------------------------------------------------------------------- //
    // Computed data
    // Data packet types.
    private static final byte DATA_TYPE_UPDATE = 0;
    private short output = 0;
    private short input = 0;
    /**
     * The last tick we updated. Used to avoid changing output multiple times a
     * tick, which is usually pointless and really bad for performance.
     */
    private long lastStep = 0L;

    /**
     * Something changed last tick after the first neighbor block update, so
     * we need to update again in the next tick (if we don't anyway).
     */
    private boolean scheduledNeighborUpdate = false;

    // --------------------------------------------------------------------- //

    public RedstoneModule(final Casing casing, final Face face) {
        super(casing, face);
    }

    // --------------------------------------------------------------------- //
    // Module

    @Override
    public void step() {
        final Level level = getCasing().getCasingLevel();

        for (final Port port : Port.VALUES) {
            stepOutput(port);
            stepInput(port);
        }

        if (scheduledNeighborUpdate && level.getGameTime() > lastStep) {
            notifyNeighbors();
        }

        lastStep = level.getGameTime();
    }

    @Override
    public void onDisabled() {
        input = 0;
        output = 0;

        notifyNeighbors();

        sendData();
    }

    @Override
    public void onEnabled() {
        sendData();
    }

    @Override
    public void onWriteComplete(final Port port) {
        // Start writing again right away to write as fast as possible.
        stepOutput(port);
    }

    @Override
    public void onData(final ByteBuf data) {
        input = data.readShort();
        output = data.readShort();
    }

    @Override
    public void load(final ValueInput input) {
        super.load(input);

        output = (short) Math.clamp(input.getShortOr(TAG_OUTPUT, (short) 0), 0, 15);
        this.input = (short) Math.clamp(input.getShortOr(TAG_INPUT, (short) 0), 0, 15);
    }

    @Override
    public void save(final ValueOutput output) {
        super.save(output);

        output.putInt(TAG_OUTPUT, this.output);
        output.putInt(TAG_INPUT, input);
    }

    // --------------------------------------------------------------------- //
    // Redstone

    public int getRedstoneInput() {
        return input;
    }

    @Override
    public void setRedstoneInput(final short value) {
        // We never call this on the client side, but other might...
        final Level level = getCasing().getCasingLevel();
        if (level.isClientSide()) {
            return;
        }

        // Clamp to valid redstone range.
        final short validatedValue = (short) Math.max(0, Math.min(15, value));
        if (validatedValue == input) {
            return;
        }

        input = validatedValue;

        // If the value changed, make sure we're saved.
        getCasing().setChanged();

        // The value changed, cancel our output to make sure it's up-to-date.
        cancelWrite();

        // Update client representation.
        sendData();
    }

    @Override
    public short getRedstoneOutput() {
        return output;
    }

    // --------------------------------------------------------------------- //

    /**
     * Update the redstone signal we're outputting.
     *
     * @param value the new output value.
     */
    private void setRedstoneOutput(final short value) {
        // Clamp to valid redstone range.
        final short validatedValue = (short) Math.max(0, Math.min(15, value));
        if (validatedValue == output) {
            return;
        }

        output = validatedValue;

        // If the value changed, make sure we're saved.
        getCasing().setChanged();

        // Notify neighbors, avoid multiple block updates per tick.
        scheduledNeighborUpdate = true;

        sendData();
    }

    /**
     * Update the output of the module, pushing a value read from any pipe.
     */
    private void stepOutput(final Port port) {
        final Pipe sendingPipe = getCasing().getSendingPipe(getFace(), port);
        if (!sendingPipe.isWriting()) {
            sendingPipe.beginWrite(input);
        }
    }

    /**
     * Update the input of the module, pushing the current input to any pipe.
     */
    private void stepInput(final Port port) {
        // Continuously read from all ports, set output to last received value.
        final Pipe receivingPipe = getCasing().getReceivingPipe(getFace(), port);
        if (!receivingPipe.isReading()) {
            receivingPipe.beginRead();
        }
        if (receivingPipe.canTransfer()) {
            setRedstoneOutput(receivingPipe.read());
        }
    }

    /**
     * Notify all neighbors of a block update, to let them realize our output changed.
     */
    private void notifyNeighbors() {
        final Level level = getCasing().getCasingLevel();

        scheduledNeighborUpdate = false;
        final Block blockType = level.getBlockState(getCasing().getPosition()).getBlock();
        level.updateNeighborsAt(getCasing().getPosition(), blockType);
    }

    /**
     * Send the current state of the module (to the client).
     */
    private void sendData() {
        final ByteBuf data = Unpooled.buffer();
        data.writeShort(input);
        data.writeShort(output);
        getCasing().sendData(getFace(), data, DATA_TYPE_UPDATE);
    }
}
