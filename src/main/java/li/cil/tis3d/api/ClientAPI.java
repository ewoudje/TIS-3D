package li.cil.tis3d.api;

import li.cil.manual.api.render.FontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class ClientAPI {
    public static FontRenderer normalFontRenderer;
    public static FontRenderer smallFontRenderer;

    public static @Nullable Level getClientLevel() {
        return Minecraft.getInstance().level;
    }
}
