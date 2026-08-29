package li.cil.tis3d.api.prefab.module;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.traits.ModuleWithRotation;
import li.cil.tis3d.api.util.TransformUtil;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * This is a utility implementation of a rotatable module.
 * <p>
 * Rotatable modules can face one of four directions, the default being
 * {@link Port#UP}. Most modules will either not need
 * this at all, or only use this when installed in the top or bottom faces
 * of casings. In some cases you may also merely want to use this for
 * graphical purposes (e.g. the built-in redstone and stack modules do
 * this).
 */
public abstract class AbstractModuleWithRotation extends AbstractModule implements ModuleWithRotation {
    // --------------------------------------------------------------------- //
    // Persisted data

    // NBT tag names.
    private static final String FACING_TAG = "facing";

    // --------------------------------------------------------------------- //
    // Computed data
    private Port facing = Port.UP;

    // --------------------------------------------------------------------- //

    protected AbstractModuleWithRotation(final Casing casing, final Face face) {
        super(casing, face);
    }

    // --------------------------------------------------------------------- //
    // General utility

    @Override
    public Vec3 hitToUV(final Vec3 hitPos) {
        return TransformUtil.hitToUV(getFace(), getFacing(), hitPos);
    }

    // --------------------------------------------------------------------- //
    // Module

    @Override
    public void load(final ValueInput tag) {
        super.load(tag);

        facing = Port.VALUES[Math.max(0, tag.getByteOr(FACING_TAG, (byte) 0)) % Port.VALUES.length];
    }

    @Override
    public void save(final ValueOutput tag) {
        super.save(tag);

        tag.putByte(FACING_TAG, (byte) facing.ordinal());
    }

    // --------------------------------------------------------------------- //
    // Rotatable

    @Override
    public Port getFacing() {
        return facing;
    }

    @Override
    public void setFacing(final Port facing) {
        this.facing = facing;
    }
}
