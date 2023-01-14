package ArchipelagoMW.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.dungeons.TheBeyond;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.rooms.EmptyRoom;

import java.util.ArrayList;

public class TheBeyondPatch {

    public TheBeyondPatch() {
    }

    @SpirePatch(clz = TheBeyond.class, method = SpirePatch.CONSTRUCTOR, paramtypez = {
            AbstractPlayer.class,
            ArrayList.class
    })
    public static class RemoveLastRoom {

        @SpirePostfixPatch
        public static void Postfix(TheBeyond __instance, AbstractPlayer p, ArrayList<String> theList) {
            AbstractDungeon.currMapNode = new MapRoomNode(0, -1);
            AbstractDungeon.currMapNode.room = new EmptyRoom();
        }
    }
}
