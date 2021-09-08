package ArchipelagoMW.patches;

import ArchipelagoMW.ArchipelagoMW;
import ArchipelagoMW.ui.RewardMenu.ArchipelagoRewardScreen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rewards.RewardItem;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

public class RewardItemPatch {

    private static final Logger logger = LogManager.getLogger(RewardItemPatch.class.getName()); // This is our logger! It prints stuff out in the console.

    public static class RewardType
    {
        @SpireEnum
        public static RewardItem.RewardType BOSS_RELIC;
        @SpireEnum
        public static RewardItem.RewardType ARCHIPELAGO_LOCATION;
    }

    @SpirePatch(clz = RewardItem.class, method = SpirePatch.CLASS)
    public static class CustomFields {
        public static SpireField<ArrayList<AbstractRelic>> bossRelics = new SpireField<>(() -> null);
        public static SpireField<Boolean> apReward = new SpireField<>(() -> false);
    }

    @SpirePatch(clz= RewardItem.class,method="render")
    public static class TextPatch {

        @SpireInstrumentPatch
        public static ExprEditor Instrument() {
            return new ExprEditor() {
                public void edit(MethodCall m) throws CannotCompileException {
                    if(m.getClassName().equals(FontHelper.class.getName()) && m.getMethodName().equals("renderSmartText")) {
                        m.replace("");
                    }
                }
            };
        }

        @SpireInsertPatch(rloc = 597-385, localvars = {"color"})
        public static void Insert(RewardItem __instance, SpriteBatch sb, String ___text, float ___REWARD_TEXT_X, float ___y, Color color) {
            if(___text.contains("[] NL")) {
                //float lineHeight = FontHelper.getSmartWidth(FontHelper.cardDescFont_N, ___text, 1000.0F * Settings.scale, 30.0F);
                FontHelper.renderSmartText(sb, FontHelper.cardDescFont_N, ___text, ___REWARD_TEXT_X, ___y + 35.5F * Settings.scale , 1000.0F * Settings.scale, 26.0F * Settings.scale, color);
            }
            else {
                FontHelper.renderSmartText(sb, FontHelper.cardDescFont_N, ___text, ___REWARD_TEXT_X, ___y + 5.0F * Settings.scale, 1000.0F * Settings.scale, 0.0F, color);
            }
        }

        @SpireInsertPatch(rloc = 575-385)
        public static void InsertBossRelicIcon(RewardItem __instance, SpriteBatch sb, RewardItem.RewardType ___type, float ___REWARD_ITEM_X, float ___y) {
            if (___type == RewardItemPatch.RewardType.BOSS_RELIC) {
                sb.draw(ImageMaster.RUN_HISTORY_MAP_ICON_BOSS_CHEST, ___REWARD_ITEM_X - 22.0F, ___y - 40.0F - 2.0F * Settings.scale, 32.0F, 32.0F, 64.0F, 64.0F, Settings.scale * 1.5F, Settings.scale * 1.5F, 0.0F, 0, 0, 64, 64, false, false);
            }
            if (___type == RewardType.ARCHIPELAGO_LOCATION) {
                sb.draw(ArchipelagoMW.AP_ICON,  ___REWARD_ITEM_X - 32.0F, ___y - 32.0F - 2.0F * Settings.scale, 32.0F, 32.0F, 64.0F, 64.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 64, 64, false, false);
            }
        }
    }

    @SpirePatch(clz=RewardItem.class, method = "claimReward")
    public static class ClaimRewardPatch {

        @SpirePrefixPatch
        public static SpireReturn<Boolean> PrefixPatch(RewardItem __instance){
            if(__instance.type == RewardType.BOSS_RELIC) {
                //logger.info("boss Relic list: " + BossRelics.bossRelics.get(__instance));
                AbstractDungeon.bossRelicScreen.open(CustomFields.bossRelics.get(__instance));
                return SpireReturn.Return(true);
            }
            if(__instance.type == RewardType.ARCHIPELAGO_LOCATION) {
                //logger.info("boss Relic list: " + BossRelics.bossRelics.get(__instance));
                //AbstractDungeon.bossRelicScreen.open(BossRelics.bossRelics.get(__instance));
                ArchipelagoRewardScreen.rewards.remove(__instance);
                return SpireReturn.Return(true);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = RewardItem.class, method = "claimReward")
    public static class ScrollUpdate {

        @SpireInsertPatch(rloc = 356 - 290)
        public static void Insert(RewardItem __instance, ArrayList<AbstractCard> ___cards) {
            if (AbstractDungeon.screen == ArchipelagoRewardScreen.Enum.ARCHIPELAGO_REWARD) {
                AbstractDungeon.cardRewardScreen.open(___cards, __instance, ArchipelagoRewardScreen.TEXT[4]);
                AbstractDungeon.previousScreen = ArchipelagoRewardScreen.Enum.ARCHIPELAGO_REWARD;
            }
        }

    }
}
