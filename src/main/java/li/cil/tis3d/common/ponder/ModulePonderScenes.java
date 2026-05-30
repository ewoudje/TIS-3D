package li.cil.tis3d.common.ponder;

import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Items;

public class ModulePonderScenes {

    public static void executionBase(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("execution", "Executing code in TIS-3D");
        scene.showBasePlate();
        scene.idle(10);
        scene.world().showSection(util.select().layer(1), Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(util.select().layer(2), Direction.DOWN);
        scene.idle(10);
        scene.overlay().showControls(util.vector().topOf(0, 0, 0), Pointing.DOWN, 40).rightClick()
            .whileSneaking()
            .withItem(Items.SALMON.getDefaultInstance());
        scene.idle(40);
        scene.markAsFinished();
    }
}
