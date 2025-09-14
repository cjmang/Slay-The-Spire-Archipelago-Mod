package ArchipelagoMW.game.items.patches;

import ArchipelagoMW.client.APContext;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;

public class LoadSavePatch {

    @SpirePatch(clz= CardCrawlGame.class, method="loadPlayerSave", paramtypez = AbstractPlayer.class)
    public static class AscensionDownPatch
    {
        public static void Postfix()
        {
            APContext.getContext().getAscensionManager().checkAndDecrementAscensions();
        }
    }
}
