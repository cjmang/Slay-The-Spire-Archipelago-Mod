package ArchipelagoMW.patches;

import ArchipelagoMW.ArchipelagoMW;
import ArchipelagoMW.ui.RewardMenu.BossRelicRewardScreen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rewards.chests.AbstractChest;
import com.megacrit.cardcrawl.rewards.chests.BossChest;
import com.megacrit.cardcrawl.rooms.TreasureRoomBoss;
import com.megacrit.cardcrawl.screens.select.BossRelicSelectScreen;
import javassist.CannotCompileException;
import javassist.NotFoundException;
import javassist.expr.Cast;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

public class BossRelicScreenPatch {

    private static final Logger logger = LogManager.getLogger(BossRelicScreenPatch.class.getName()); // This is our logger! It prints stuff out in the console.
    @SpirePatch(clz= BossRelicSelectScreen.class,method="render")
    public static class RenderPatch {

        @SpireInsertPatch(rloc = 329 - 311)
        public static void Insert(BossRelicSelectScreen __instance, SpriteBatch sb, ArrayList<AbstractRelic> ___relics) {
            if(___relics.isEmpty()) {
              sb.draw(ArchipelagoMW.AP_ICON, Settings.WIDTH/2.0F, Settings.HEIGHT/2.0F);
            }
        }


        @SpireInstrumentPatch
        public static ExprEditor InstrumentCast() {
            return new ExprEditor() {
                public void edit(Cast cast) throws CannotCompileException {
                    try {
                        if(cast.getType().getName().equals(TreasureRoomBoss.class.getName())) {
                            cast.replace("if ($1 instanceof "+ TreasureRoomBoss.class.getName() +") {$_ = $proceed($$);}");
                        }
                    } catch (NotFoundException e) {
                        e.printStackTrace();
                    }
                }
            };
        }

        @SpireInstrumentPatch
        public static ExprEditor InstrumentField() {
            return new ExprEditor() {
                public void edit(FieldAccess field) throws CannotCompileException {
                    if (field.getClassName().equals(TreasureRoomBoss.class.getName()) && field.getFieldName().equals("chest")) {
                        field.replace("if ($0 != null) {$_ = $proceed($$);}");
                    }
                }
            };
        }

        @SpireInstrumentPatch
        public static ExprEditor InstrumentRender() {
            return new ExprEditor() {
                public void edit(MethodCall method) throws CannotCompileException {
                    if (method.getClassName().equals(AbstractChest.class.getName()) && method.getMethodName().equals("render")) {
                        method.replace("if ($0 != null) {$_ = $proceed($$);}");
                    }
                }
            };
        }
    }
    @SpirePatch(clz=BossRelicSelectScreen.class, method = "relicObtainLogic")
    public static class ObtainLogic {

        @SpireInstrumentPatch
        public static ExprEditor InstrumentCast() {
            return new ExprEditor() {
                public void edit(Cast cast) throws CannotCompileException {
                    try {
                        if(cast.getType().getName().equals(TreasureRoomBoss.class.getName())) {
                            cast.replace("if ($1 instanceof "+ TreasureRoomBoss.class.getName() +") {$_ = $proceed($$);}");
                        }
                    } catch (NotFoundException e) {
                        e.printStackTrace();
                    }
                }
            };
        }

        @SpireInstrumentPatch
        public static ExprEditor InstrumentField() {
            return new ExprEditor() {
                public void edit(FieldAccess field) throws CannotCompileException {
                    if (field.getClassName().equals(TreasureRoomBoss.class.getName()) && field.getFieldName().equals("choseRelic")) {
                        field.replace("if ($0 != null) {$_ = $proceed($$);}");
                    }
                }
            };
        }
    }

    @SpirePatch(clz = BossRelicSelectScreen.class, method = "open")
    public static class OpenPatch {

        @SpireInsertPatch(rloc = 465 -449, localvars = {"chosenRelics"})
        public static SpireReturn Insert(BossRelicSelectScreen __instance, ArrayList<AbstractRelic> chosenRelics) {
            if(chosenRelics.isEmpty()) {
                return SpireReturn.Return();
            }
            return SpireReturn.Continue();
        }

    }
}
