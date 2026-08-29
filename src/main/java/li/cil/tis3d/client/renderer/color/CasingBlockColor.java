package li.cil.tis3d.client.renderer.color;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.ModuleRenderer;
import li.cil.tis3d.client.renderer.block.entity.CasingBlockEntityRenderer;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.OptionalInt;

public final class CasingBlockColor implements BlockColor {
    @Override
    public int getColor(final BlockState state, @Nullable final BlockAndTintGetter level, @Nullable final BlockPos pos, final int tintIndex) {
        if (level == null || pos == null) {
            return -1;
        }

        int result = -1;
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof final CasingBlockEntity casing) {
            for (final Face face : Face.VALUES) {
                final Module module = casing.getModule(face);
                if (module != null) {
                    final OptionalInt optional = ModuleRenderer.findRenderer(module).getTintColor(level, pos, tintIndex, module);
                    if (optional.isPresent()) {
                        result = optional.getAsInt();

                        // Ex. dirt block and log facade, we should still try to get the dirt one if the log doesnt use any
                        if (result != -1)
                             return result;
                    }
                }
            }
        }

        return result;
    }
}
