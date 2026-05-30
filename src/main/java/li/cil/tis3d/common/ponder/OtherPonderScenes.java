package li.cil.tis3d.common.ponder;

import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class OtherPonderScenes {
    public static void makeTIS(SceneBuilder scene, SceneBuildingUtil util) {
        var controller = util.select().position(1, 1, 1);
        var casing = util.select().position(1, 2, 1);

        scene.title("make_a_tis", "Building a tessellated intelligence system!");
        scene.showBasePlate();
        scene.idle(10);

        scene.world().showSection(controller, Direction.DOWN);
        scene.overlay().showOutlineWithText(controller, 30)
            .text("The controller is the centerpiece of your tessellated intelligence system.");
        scene.idle(35);
        scene.overlay().showOutlineWithText(controller, 30)
            .text("It can can support up to sixteen casings!");
        scene.idle(35);

        scene.world().showSection(casing, Direction.DOWN);
        scene.addKeyframe();
        scene.overlay().showOutlineWithText(casing, 20)
            .text("Casings are also a must-have!");
        scene.idle(25);
        scene.overlay().showOutlineWithText(casing, 40)
            .text("In all open faces you can place a module");
        scene.idle(10);

        scene.overlay()
            .showControls(util.vector().blockSurface(new BlockPos(1, 2, 1), Direction.NORTH), Pointing.LEFT, 20)
            .rightClick()
            .withItem(li.cil.tis3d.common.item.Items.EXECUTION_MODULE.get().getDefaultInstance());
        scene.idle(10);
        scene.world().modifyBlockEntity(util.grid().at(1, 2, 1), CasingBlockEntity.class, casingBe ->
            casingBe.setItem(Direction.NORTH.ordinal(), li.cil.tis3d.common.item.Items.EXECUTION_MODULE.get().getDefaultInstance())
        );
        scene.idle(35);

        scene.addKeyframe();
        scene.idle(40);
        scene.markAsFinished();
    }
}
