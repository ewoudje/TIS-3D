package li.cil.tis3d.client.renderer.font;

import li.cil.manual.api.prefab.renderer.BitmapFontRenderer;
import li.cil.manual.api.render.FontRenderer;
import li.cil.tis3d.api.API;
import net.minecraft.resources.Identifier;

public final class SmallFontRenderer extends BitmapFontRenderer {
    public static final FontRenderer INSTANCE = new SmallFontRenderer();

    private static final Identifier LOCATION_FONT_TEXTURE = API.resource("textures/font/small.png");
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890:#-,?+!=()'.";

    // --------------------------------------------------------------------- //
    // FontRenderer

    private SmallFontRenderer() {
    }

    @Override
    public int charWidth() {
        return 3;
    }

    // --------------------------------------------------------------------- //
    // AbstractFontRenderer

    @Override
    public int lineHeight() {
        return 4;
    }

    @Override
    protected CharSequence getCharacters() {
        return CHARS;
    }

    @Override
    protected Identifier getTextureLocation() {
        return LOCATION_FONT_TEXTURE;
    }

    @Override
    protected int getResolution() {
        return 32;
    }

    @Override
    protected int getGapU() {
        return 1;
    }

    // --------------------------------------------------------------------- //

    @Override
    protected int getGapV() {
        return 1;
    }
}
