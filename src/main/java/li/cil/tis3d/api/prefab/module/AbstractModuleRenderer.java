package li.cil.tis3d.api.prefab.module;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.ModuleRenderer;
import li.cil.tis3d.api.util.ModuleRenderContext;
import li.cil.tis3d.client.models.ModuleModelData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Objects;

public abstract class AbstractModuleRenderer<T extends Module> implements ModuleRenderer<T> {

    /**
     * Utility method for determining whether the player is currently looking at this module.
     *
     * @param hitResult the current hit result.
     * @return <tt>true</tt> if the observer is looking at the module, <tt>false</tt> otherwise.
     */
    protected boolean isHitFace(final T module, ModuleRenderContext ctx) {
        HitResult hitResult = ctx.getHitResult();
        if (!(hitResult instanceof final BlockHitResult blockHitResult)) {
            return false;
        }

        final BlockPos pos = blockHitResult.getBlockPos();
        return Objects.equals(module.getCasing().getPosition(), pos) &&
            blockHitResult.getDirection() == Face.toDirection(module.getFace());
    }

    /**
     * Utility method for determining the hit coordinate on the module's face the player is
     * looking at. This will return {@code null} if the player is not currently looking
     * at the module.
     * <p>
     * Note that this will return the unadjusted X, Y and Z components. To transform this
     * coordinate to a UV coordinate mapped to the module's face, pass this into
     * {@link #hitToUV}. Note that this method is overridden in {@link AbstractModuleWithRotation}
     * to also take into account the module's rotation.
     *
     * @param hitResult the current hit result.
     * @return the UV coordinate the observer is looking at as the X and Y components.
     */
    @Nullable
    protected Vec3 getLocalHitPosition(final T module, ModuleRenderContext ctx) {
        HitResult hitResult = ctx.getHitResult();
        if (!(hitResult instanceof final BlockHitResult blockHitResult)) {
            return null;
        }

        final BlockPos pos = blockHitResult.getBlockPos();
        if (!Objects.equals(module.getCasing().getPosition(), pos)) {
            return null;
        }
        if (blockHitResult.getDirection() != Face.toDirection(module.getFace())) {
            return null;
        }

        return hitResult.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public @Nullable ModuleModelData getModelData(@Nullable Level level, BlockPos blockPos, BlockState blockState, T module) {
        return ModuleModelData.CLASSIC;
    }
}
