package ArchipelagoMW.game.ui;

import com.evacipated.cardcrawl.modthespire.lib.SpireInstrumentPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.ui.DialogWord;
import com.megacrit.cardcrawl.vfx.SpeechBubble;
import com.megacrit.cardcrawl.vfx.SpeechTextEffect;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

public class SpeechBubblePatch {
    public static boolean skipLowLevelText = false;
    @SpirePatch(clz= SpeechBubble.class, method=SpirePatch.CONSTRUCTOR,
            paramtypez = {float.class, float.class, float.class, String.class, boolean.class})
    public static class TopLevelTextPatch
    {
        @SpireInstrumentPatch
        public static ExprEditor instrument()
        {
            return new ExprEditor() {
                @Override
                public void edit(MethodCall method) throws CannotCompileException
                {
                    if(method.getClassName().equals("java.util.ArrayList") && method.getMethodName().equals("add"))
                    {
                        method.replace("if(!ArchipelagoMW.game.ui.SpeechBubblePatch.skipLowLevelText) {$_ = $proceed($$);}");
                    }
                }
            };
        }

        @SpirePostfixPatch()
        public static void topLevelEffect(SpeechBubble __instance, float x, float y, float duration, String msg, boolean isPLayer)
        {
            if(skipLowLevelText) {
                AbstractDungeon.topLevelEffectsQueue.add(
                        new SpeechTextEffect(x + 170f * Settings.scale,
                                y + 124f * Settings.scale,
                                duration,
                                msg,
                                DialogWord.AppearEffect.BUMP_IN
                        ));
            }
        }
    }
}
