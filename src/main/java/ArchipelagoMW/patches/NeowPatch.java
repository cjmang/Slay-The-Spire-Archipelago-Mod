package ArchipelagoMW.patches;

import basemod.BaseMod;
import basemod.DevConsole;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.audio.MusicMaster;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.neow.NeowEvent;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.rewards.chests.AbstractChest;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

public class NeowPatch {

    public static final Logger logger = LogManager.getLogger(NeowPatch.class.getName());
    public static boolean act2portalAvailable = false;
    public static boolean act3portalAvailable = false;

    public NeowPatch() {}

    @SpirePatch(clz = NeowEvent.class, method = SpirePatch.CLASS)
    public static class CustomFields {
        public static SpireField<Boolean> finished = new SpireField<>(() -> false);
    }
    @SpirePatch(clz = NeowEvent.class, method = "buttonEffect")
    public static class createPortalOptions {

        @SpirePrefixPatch
        public static SpireReturn<Void> Prefix(NeowEvent __instance, int buttonPressed, int ___screenNum) {
            if(___screenNum == 99) {
                boolean portalEngaged = false;
                switch (buttonPressed) {
                    case 0:
                        BaseMod.console.setText("act TheBeyond");
                        portalEngaged = true;
                        break;
                    case 1:
                        BaseMod.console.setText("act TheCity");
                        portalEngaged = true;
                        break;
                }
                if(portalEngaged){
                    CustomFields.finished.set(__instance, true);
                    __instance.roomEventText.clear();
                    CardCrawlGame.music.fadeOutBGM();
                    DevConsole.execute();
                    return SpireReturn.Return();
                }
            }
            return SpireReturn.Continue();
        }
        @SpirePostfixPatch
        public static void Postfix(NeowEvent __instance, int buttonPressed, int ___screenNum) {
            boolean finished = CustomFields.finished.get(__instance);
            if(finished){
                return;
            }
            if(___screenNum == 99) {
                __instance.roomEventText.clear();
                __instance.roomEventText.addDialogOption("Portal to act 3",!act3portalAvailable);
                __instance.roomEventText.addDialogOption("Portal to act 2",!act2portalAvailable);
                __instance.roomEventText.addDialogOption("[Leave]");
            }
        }
    }
}
