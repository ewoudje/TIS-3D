package li.cil.tis3d.common.module;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.prefab.module.AbstractModuleWithRotation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class KeypadModule extends AbstractModuleWithRotation {
    // --------------------------------------------------------------------- //
    // Persisted data

    // Rendering info.
    public static final float KEYS_U0 = 5 / 32f;

    // --------------------------------------------------------------------- //
    // Computed data
    public static final float KEYS_V0 = 5 / 32f;
    public static final float KEYS_SIZE_U = 5 / 32f;
    public static final float KEYS_SIZE_V = 5 / 32f;
    public static final float KEYS_SIZE_V_LAST = 4 / 32f;
    public static final float KEYS_STEP_U = 6 / 32f;
    public static final float KEYS_STEP_V = 6 / 32f;
    // NBT tag names.
    private static final String TAG_VALUE = "value";
    // Data packet types.
    private static final byte DATA_TYPE_VALUE = 0;
    // Pitch lookup for click feedback sound cue per value, 0-9.
    // Roughly based on telephone keypad frequencies, except we have to mush
    // both tones into one, so obviously some fidelity is lost, but eh.
    private static final float[] VALUE_TO_PITCH = new float[]{0.9125f, 0.7f, 0.75f, 0.825f, 0.725f, 0.8f, 0.875f, 0.775f, 0.85f, 0.95f};
    /**
     * The current value being input.
     */
    private Optional<Short> value = Optional.empty();

    // --------------------------------------------------------------------- //

    public KeypadModule(final Casing casing, final Face face) {
        super(casing, face);
    }

    public static short buttonToNumber(final int button) {
        return (short) ((button + 1) % 10);
    }

    // --------------------------------------------------------------------- //
    // Module

    public Optional<Short> getValue() {
        return value;
    }

    @Override
    public void step() {
        stepOutput();
    }

    @Override
    public void onDisabled() {
        if (value.isPresent()) {
            // Clear the value (that was being written).
            value = Optional.empty();

            // Tell clients we can input again.
            getCasing().sendData(getFace(), new CompoundTag(), DATA_TYPE_VALUE);
        }
    }

    @Override
    public void onBeforeWriteComplete(final Port port) {
        // Pop the value (that was being written).
        value = Optional.empty();
        getCasing().setChanged();

        // If one completes, cancel all other writes to ensure a value is only
        // written once.
        cancelWrite();
    }

    @Override
    public void onWriteComplete(final Port port) {
        // Tell clients we can input again.
        getCasing().sendData(getFace(), new CompoundTag(), DATA_TYPE_VALUE);
    }

    @Override
    public boolean use(final Player player, final InteractionHand hand, final Vec3 hit) {
        if (player.isShiftKeyDown()) {
            return false;
        }

        // Reasoning: don't remove module from casing while activating the
        // module while the casing is disabled. Could be frustrating.
        if (!getCasing().isEnabled()) {
            return true;
        }

        // Only allow inputting one value.
        if (value.isPresent()) {
            return true;
        }

        // Handle input on the client and send it to the server for higher
        // hit position resolution (MC sends this to the server at a super
        // low resolution for some reason).
        final Level level = getCasing().getCasingLevel();
        if (level.isClientSide()) {
            final Vec3 uv = hitToUV(hit);
            final int button = uvToButton((float) uv.x, (float) uv.y);
            if (button == -1) {
                // No button here.
                return true;
            }
            final short number = buttonToNumber(button);

            final CompoundTag tag = new CompoundTag();
            tag.putShort(TAG_VALUE, number);
            getCasing().sendData(getFace(), tag, DATA_TYPE_VALUE);
        }

        return true;
    }

    @Override
    public void onData(final ValueInput data) {
        final Level level = getCasing().getCasingLevel();
        if (level.isClientSide()) {
            // Got state on which key is currently 'pressed'.
            value = data.getInt(TAG_VALUE).map(Integer::shortValue);
        } else if (value.isEmpty() && (value = data.getInt(TAG_VALUE).map(Integer::shortValue)).isPresent()) {
            getCasing().sendData(getFace(), o -> o.putShort(TAG_VALUE, value.get()), DATA_TYPE_VALUE);
            getCasing().getCasingLevel().playSound(null, getCasing().getPosition(), SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3f, VALUE_TO_PITCH[value.get()]);
            getCasing().setChanged();
        }
    }

    @Override
    public void load(final ValueInput input) {
        super.load(input);

        value = input.getInt(TAG_VALUE).map(Integer::shortValue);
    }

    // --------------------------------------------------------------------- //

    @Override
    public void save(final ValueOutput output) {
        super.save(output);

        value.ifPresent(x -> output.putShort(TAG_VALUE, x));
    }

    private void stepOutput() {
        if (value.isEmpty()) {
            return;
        }

        for (final Port port : Port.VALUES) {
            final Pipe sendingPipe = getCasing().getSendingPipe(getFace(), port);
            if (!sendingPipe.isWriting()) {
                sendingPipe.beginWrite(value.get());
            }
        }
    }

    public int uvToButton(final float u, final float v) {
        if (u < KEYS_U0 || u > KEYS_U0 + KEYS_STEP_U * 2 + KEYS_SIZE_U) {
            return -1;
        }
        if (v < KEYS_V0 || v > KEYS_V0 + KEYS_STEP_V * 3 + KEYS_SIZE_V) {
            return -1;
        }

        // Pretty meh, but cba to math right now. Mostly because skipping the
        // gaps and floating point modulo and special case for zero. Ugh.
        int row = 0;
        float v0 = v - KEYS_V0;
        while (v0 > ((row == 3) ? KEYS_SIZE_V_LAST : KEYS_SIZE_V)) {
            row++;
            v0 -= KEYS_STEP_V;
        }
        if (v0 < 0) {
            // Looking at a gap.
            return -1;
        }

        int column = row == 3 ? -1 : 0;
        float u0 = u - KEYS_U0;
        while (u0 > KEYS_SIZE_U) {
            column++;
            u0 -= KEYS_STEP_U;
        }
        if (u0 < 0 && row != 3 && column != 1) {
            // Looking at a gap.
            return -1;
        }
        if (column < 0) {
            // Left side of zero button.
            column = 0;
        }

        final int button = row * 3 + column;
        if (button > 9) {
            // Past the last button.
            return -1;
        }

        return button;
    }
}
