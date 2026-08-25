package li.cil.tis3d.common.module;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.prefab.module.AbstractModuleWithRotation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public final class SequencerModule extends AbstractModuleWithRotation {
    public static final float CELLS_OUTER_U0 = 8 / 64f;
    public static final float CELLS_OUTER_V0 = 8 / 64f;
    public static final float CELLS_OUTER_SIZE_U = 6 / 64f;
    public static final float CELLS_OUTER_SIZE_V = 6 / 64f;
    public static final float CELLS_OUTER_STEP_U = CELLS_OUTER_SIZE_U;
    public static final float CELLS_OUTER_STEP_V = CELLS_OUTER_SIZE_V;

    // --------------------------------------------------------------------- //
    // Persisted data
    public static final int COL_COUNT = 8;
    public static final int ROW_COUNT = 8;
    // NBT data names.
    private static final String TAG_CONFIGURATION = "configuration";
    private static final String TAG_POSITION = "position";

    // --------------------------------------------------------------------- //
    // Computed data
    private static final String TAG_DELAY = "delay";
    private static final String TAG_STEPS_REMAINING = "stepsRemaining";
    // Data packet types.
    private static final byte DATA_TYPE_CONFIGURATION = 0;
    private static final byte DATA_TYPE_POSITION = 1;
    private final boolean[][] configuration = new boolean[COL_COUNT][ROW_COUNT];
    private int position = -1;
    private int delay = 4;
    private int stepsRemaining = 0;
    private short output;

    // --------------------------------------------------------------------- //

    public SequencerModule(final Casing casing, final Face face) {
        super(casing, face);
    }

    private static long encodeConfiguration(final boolean[][] configuration) {
        long encodedConfiguration = 0L;
        long mask = 1;
        for (int col = 0; col < COL_COUNT; col++) {
            for (int row = 0; row < ROW_COUNT; row++, mask <<= 1L) {
                if (configuration[col][row]) {
                    encodedConfiguration |= mask;
                }
            }
        }
        return encodedConfiguration;
    }

    private static void decodeConfiguration(final long encodedConfiguration, final boolean[][] configuration) {
        long mask = 1;
        for (int col = 0; col < COL_COUNT; col++) {
            for (int row = 0; row < ROW_COUNT; row++, mask <<= 1L) {
                configuration[col][row] = (encodedConfiguration & mask) != 0;
            }
        }
    }

    public boolean isConfigured(int col, int row) {
        return configuration[col][row];
    }

    public int getPosition() {
        return position;
    }

    // --------------------------------------------------------------------- //
    // Module

    public int getDelay() {
        return delay;
    }

    public int getStepsRemaining() {
        return stepsRemaining;
    }

    @Override
    public void step() {
        stepInput();
        stepOutput();
    }

    @Override
    public void onEnabled() {
        super.onEnabled();
    }

    @Override
    public void onDisabled() {
        position = -1;
        stepsRemaining = 0;
    }

    @Override
    public boolean use(final Player player, final InteractionHand hand, final Vec3 hit) {
        if (player.isShiftKeyDown()) {
            return false;
        }

        // Handle input on the client and send it to the server for higher
        // hit position resolution (MC sends this to the server at a super
        // low resolution for some reason).
        final Level level = getCasing().getCasingLevel();
        if (level.isClientSide()) {
            final Vec3 uv = hitToUV(hit);
            final int col = uvToCol((float) uv.x);
            final int row = uvToRow((float) uv.y);
            if (col >= 0 && row >= 0) {
                configuration[col][row] = !configuration[col][row];
                sendConfiguration(false);
            }
        }

        return true;
    }

    @Override
    public void onData(final ByteBuf data) {
        if (getCasing().getCasingLevel().isClientSide()) {
            if (data.readBoolean()) {
                decodeConfiguration(data.readLong(), configuration);
            } else {
                position = data.readByte();
            }
        } else {
            decodeConfiguration(data.readLong(), configuration);
            sendConfiguration(true);
        }
        getCasing().setChanged();
    }

    // --------------------------------------------------------------------- //

    @Override
    public void load(final ValueInput output) {
        super.load(output);

        decodeConfiguration(output.getLongOr(TAG_CONFIGURATION, 0), configuration);
        position = Math.clamp(output.getIntOr(TAG_POSITION, 0), 0, COL_COUNT - 1);
        delay = Math.clamp(output.getIntOr(TAG_DELAY, 0), 0, 0xFFFF);
        stepsRemaining = Math.clamp(output.getIntOr(TAG_STEPS_REMAINING, 0), 0, 0xFFFF);

        initializeOutput();
    }

    @Override
    public void save(final ValueOutput input) {
        super.save(input);

        input.putLong(TAG_CONFIGURATION, encodeConfiguration(configuration));
        input.putInt(TAG_POSITION, position);
        input.putInt(TAG_DELAY, delay);
        input.putInt(TAG_STEPS_REMAINING, stepsRemaining);
    }

    private void stepOutput() {
        if (stepsRemaining-- <= 0) {
            stepsRemaining = delay;
            cancelWrite();
            position = (position + 1) % COL_COUNT;
            sendPosition();
            getCasing().setChanged();

            initializeOutput();
            for (final Port port : Port.VALUES) {
                final Pipe sendingPipe = getCasing().getSendingPipe(getFace(), port);
                if (!sendingPipe.isWriting()) {
                    sendingPipe.beginWrite(output);
                }
            }
        }
    }

    private void stepInput() {
        for (final Port port : Port.VALUES) {
            final Pipe receivingPipe = getCasing().getReceivingPipe(getFace(), port);
            if (!receivingPipe.isReading()) {
                receivingPipe.beginRead();
            }
            if (receivingPipe.canTransfer()) {
                delay = receivingPipe.read() & 0xFFFF;
                getCasing().setChanged();
            }
        }
    }

    private void sendConfiguration(boolean isClient) {
        final ByteBuf data = Unpooled.buffer();
        if (isClient) {
            data.writeBoolean(true);
        }
        data.writeLong(encodeConfiguration(configuration));
        getCasing().sendData(getFace(), data, DATA_TYPE_CONFIGURATION);
    }

    private void sendPosition() {
        final ByteBuf data = Unpooled.buffer();
        data.writeBoolean(false);
        data.writeByte(position);
        getCasing().sendData(getFace(), data, DATA_TYPE_POSITION);
    }

    private void initializeOutput() {
        output = 0;
        for (int mask = 1, row = 0; row < ROW_COUNT; row++, mask <<= 1) {
            if (configuration[position][row]) {
                output |= mask;
            }
        }
    }

    public int uvToCol(final float u) {
        if (u < CELLS_OUTER_U0 || u > CELLS_OUTER_U0 + CELLS_OUTER_STEP_U * COL_COUNT) {
            return -1;
        }

        final float mappedU = (u - CELLS_OUTER_U0) / (CELLS_OUTER_STEP_U * COL_COUNT);
        return (int) (mappedU * COL_COUNT);
    }

    public int uvToRow(final float v) {
        if (v < CELLS_OUTER_V0 || v > CELLS_OUTER_V0 + CELLS_OUTER_STEP_V * ROW_COUNT) {
            return -1;
        }

        final float mappedV = (v - CELLS_OUTER_V0) / (CELLS_OUTER_STEP_V * ROW_COUNT);
        return (int) (mappedV * COL_COUNT);
    }
}
