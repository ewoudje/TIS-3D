package li.cil.tis3d.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Optional;

/**
 * Utility method for wrapping enum serialization against exceptions.
 */
public final class EnumUtils {
    private EnumUtils() {
    }

    public static <T extends Enum<T>> T load(final Class<T> clazz, final String tagName, final CompoundTag tag) {
        //TODO
        return clazz.getEnumConstants()[0];
    }

    public static <T extends Enum<T>> Optional<T> load(final Class<T> clazz, final String tagName, final ValueInput input) {
        var value = input.getByteOr(tagName, (byte) -1);
        if (value == -1) return Optional.empty();
        if (value >= clazz.getEnumConstants().length) return Optional.empty();

        return Optional.of(clazz.getEnumConstants()[value]);
    }

    // --------------------------------------------------------------------- //

    public static <T extends Enum<T>> void save(final Enum<T> value, final String tagName, final CompoundTag tag) {
        tag.putByte(tagName, (byte) value.ordinal()); //TODO
    }

    public static <T extends Enum<T>> void save(final Enum<T> value, final String tagName, final ValueOutput output) {
        output.putByte(tagName, (byte) value.ordinal());
    }
}
