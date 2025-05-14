package ArchipelagoMW.game.locations.patches.campfire;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.helpers.FontHelper;
import javassist.CtBehavior;

public class FontHelperPatch {

    public static boolean forceWrap = false;

    // TODO: don't actually know if this is needed
//    @SpirePatch(clz= FontHelper.class, method="renderFontCenteredTopAligned")
    public static class RenderPatch
    {
//        @SpireInsertPatch(locator=Locator.class)
        public static SpireReturn<Void> renderWrap(SpriteBatch sb, BitmapFont font, String msg, float x, float y, Color c)
        {
            if(forceWrap)
            {
                font.draw(sb, msg, x, y + FontHelper.layout.height /2.0F, 0.0F, 1, true);
                return SpireReturn.Return();
            }
            return SpireReturn.Continue();
        }

        public static class Locator extends SpireInsertLocator
        {
            @Override
            public int[] Locate(CtBehavior ctBehavior) throws Exception {
                Matcher matcher = new Matcher.MethodCallMatcher(BitmapFont.class, "draw");
                return LineFinder.findInOrder(ctBehavior, matcher);
            }
        }
    }

}
