package ArchipelagoMW.game.save;

import ArchipelagoMW.client.APClient;
import ArchipelagoMW.client.APContext;
import ArchipelagoMW.game.teams.TeamManager;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.AsyncSaver;
import com.megacrit.cardcrawl.rooms.TreasureRoomBoss;
import com.megacrit.cardcrawl.saveAndContinue.SaveAndContinue;
import com.megacrit.cardcrawl.saveAndContinue.SaveFile;
import javassist.CtBehavior;

public class SavePatch {


    @SpirePatch(clz = SaveAndContinue.class, method = "loadSaveString", paramtypez = {String.class})
    public static class LoadSaveString {

        @SpirePrefixPatch
        public static SpireReturn<String> Prefix() {
            APContext ctx = APContext.getContext();
            return SpireReturn.Return(ctx.getSaveManager().loadSaveString(ctx.getCharacterManager().getCurrentCharacter().chosenClass.name()));
        }
    }

    @SpirePatch(clz = SaveAndContinue.class, method = "save")
    public static class Save {

        @SpireInsertPatch(locator = Locator.class, localvars = {"data", "save"})
        public static SpireReturn<Void> Insert(String data, SaveFile save) {
            // hopefully Save data?
            if (save.current_room.equals(TreasureRoomBoss.class.getName()) && TeamManager.myTeam == null && AbstractDungeon.actNum < 3) {

                String character = APContext.getContext().getCharacterManager().getCurrentCharacter().chosenClass.name();
                APClient.logger.info("Attempting to save character {}", character);
                APContext.getContext().getSaveManager().saveString(character, data);
                return SpireReturn.Return();
            }
            return SpireReturn.Continue();
        }

        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctBehavior) throws Exception {
                Matcher match = new Matcher.MethodCallMatcher(AsyncSaver.class, "save");
                return LineFinder.findAllInOrder(ctBehavior, match);
            }
        }
    }
}
