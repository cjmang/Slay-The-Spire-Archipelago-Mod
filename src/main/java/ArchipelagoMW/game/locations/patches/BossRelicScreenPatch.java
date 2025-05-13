package ArchipelagoMW.game.locations.patches;

import ArchipelagoMW.game.locations.ui.RewardMenu.ArchipelagoRewardScreen;
import ArchipelagoMW.game.items.patches.RewardItemPatch;
import basemod.BaseMod;
import com.evacipated.cardcrawl.modthespire.lib.SpireInstrumentPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.rewards.chests.AbstractChest;
import com.megacrit.cardcrawl.rooms.TreasureRoomBoss;
import com.megacrit.cardcrawl.screens.select.BossRelicSelectScreen;
import javassist.CannotCompileException;
import javassist.NotFoundException;
import javassist.expr.Cast;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;

import java.util.ArrayList;

public class BossRelicScreenPatch {

    @SpirePatch(clz = BossRelicSelectScreen.class, method = "render")
    public static class RenderPatch {

        @SpireInstrumentPatch
        public static ExprEditor InstrumentCast() {
            return new ExprEditor() {
                public void edit(Cast cast) throws CannotCompileException {
                    try {
                        if (cast.getType().getName().equals(TreasureRoomBoss.class.getName())) {
                            cast.replace("if ($1 instanceof " + TreasureRoomBoss.class.getName() + ") {$_ = $proceed($$);}");
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

    @SpirePatch(clz = BossRelicSelectScreen.class, method = "relicObtainLogic")
    public static class ObtainLogic {

        @SpireInstrumentPatch
        public static ExprEditor InstrumentCast() {
            return new ExprEditor() {
                public void edit(Cast cast) throws CannotCompileException {
                    try {
                        if (cast.getType().getName().equals(TreasureRoomBoss.class.getName())) {
                            cast.replace("if ($1 instanceof " + TreasureRoomBoss.class.getName() + ") {$_ = $proceed($$);}");
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


        @SpirePatch(clz = BossRelicSelectScreen.class, method = "relicObtainLogic")
        public static class RelicObtainLogicPatch {
            //the only one that works reliably with current logic

            @SpirePrefixPatch
            public static void PrefixPatch(BossRelicSelectScreen __instance, AbstractRelic r) {
                RewardItem reward = findRewardItem(r);
                ArchipelagoRewardScreen.rewards.remove(reward);
                ((ArchipelagoRewardScreen) BaseMod.getCustomScreen(ArchipelagoRewardScreen.Enum.ARCHIPELAGO_REWARD_SCREEN)).positionRewards();
            }

            public static RewardItem findRewardItem(AbstractRelic bossRelic) {
                //logger.info("looking for relic:" + bossRelic.name);
                for (RewardItem rewardItem : ArchipelagoRewardScreen.rewards) {
                    ArrayList<AbstractRelic> list = RewardItemPatch.CustomFields.bossRelics.get(rewardItem);
                    if (list == null) {
                        continue;
                    }
                    if (list.contains(bossRelic)) {
                        return rewardItem;
                    }
                }
                return null;
            }
        }
    }
}
