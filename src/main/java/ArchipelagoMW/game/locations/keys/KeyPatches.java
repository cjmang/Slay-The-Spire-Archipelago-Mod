package ArchipelagoMW.game.locations.keys;

import ArchipelagoMW.client.APContext;
import ArchipelagoMW.game.CharacterManager;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.rooms.MonsterRoomElite;
import io.github.archipelagomw.LocationManager;

import java.util.ArrayList;

public class KeyPatches {

    @SpirePatch(clz= AbstractDungeon.class, method="setEmeraldElite")
    public static class EmeraldPatch {

        @SpirePrefixPatch
        public static SpireReturn<Void> Prefix() {
            CharacterManager charManager = APContext.getContext().getCharacterManager();
            if(charManager.getCurrentCharacterConfig().finalAct && !charManager.getCurrentCharacterConfig().keySanity ||
                    (Loader.isModLoaded("downfall") && charManager.getCurrentCharacterConfig().downfall))
            {
                return SpireReturn.Continue();
            }
            LocationManager manager = APContext.getContext().getLocationManager();
            if (manager.getMissingLocations().contains(96L + (200L * charManager.getCurrentCharacterConfig().charOffset))) {
                // Copied from the corresponding method in Abstract Dungeon.
                ArrayList<MapRoomNode> eliteNodes = new ArrayList<>();
                for (int i = 0; i < AbstractDungeon.map.size(); i++) {
                    for (int j = 0; j < (AbstractDungeon.map.get(i)).size(); j++) {
                        if (((AbstractDungeon.map.get(i)).get(j)).room instanceof MonsterRoomElite)
                            eliteNodes.add((AbstractDungeon.map.get(i)).get(j));
                    }
                }
                MapRoomNode chosenNode = eliteNodes.get(AbstractDungeon.mapRng.random(0, eliteNodes.size() - 1));
                chosenNode.hasEmeraldKey = true;
            }
            return SpireReturn.Return();
        }
    }

    @SpirePatch(cls= "downfall.patches.ui.map.EmeraldElite", method="alternate", requiredModId = "downfall")
    public static class DownfallEmeraldPatch {

        @SpirePrefixPatch
        public static SpireReturn<SpireReturn<?>> Prefix() {
            CharacterManager charManager = APContext.getContext().getCharacterManager();
            if(charManager.getCurrentCharacterConfig().finalAct && !charManager.getCurrentCharacterConfig().keySanity || !charManager.getCurrentCharacterConfig().downfall)
            {
                return SpireReturn.Continue();
            }
            LocationManager manager = APContext.getContext().getLocationManager();
            if (manager.getMissingLocations().contains(96L + (200L * charManager.getCurrentCharacterConfig().charOffset))) {
                // Copied from the corresponding method in downfall

                ArrayList<MapRoomNode> eliteNodes = new ArrayList();

                for(int i = 0; i < AbstractDungeon.map.size() - 5; ++i) {
                    for(int j = 0; j < ((ArrayList)AbstractDungeon.map.get(i)).size(); ++j) {
                        if (((MapRoomNode)((ArrayList)AbstractDungeon.map.get(i)).get(j)).room instanceof MonsterRoomElite) {
                            eliteNodes.add((MapRoomNode)((ArrayList)AbstractDungeon.map.get(i)).get(j));
                        }
                    }
                }

                if (eliteNodes.isEmpty()) {
                    for(int i = 0; i < AbstractDungeon.map.size(); ++i) {
                        for(int j = 0; j < ((ArrayList)AbstractDungeon.map.get(i)).size(); ++j) {
                            if (((MapRoomNode)((ArrayList)AbstractDungeon.map.get(i)).get(j)).room instanceof MonsterRoomElite) {
                                eliteNodes.add((MapRoomNode)((ArrayList)AbstractDungeon.map.get(i)).get(j));
                            }
                        }
                    }
                }

                MapRoomNode chosenNode = (MapRoomNode)eliteNodes.get(AbstractDungeon.mapRng.random(0, eliteNodes.size() - 1));
                chosenNode.hasEmeraldKey = true;
            }
            return SpireReturn.Return(SpireReturn.Return());
        }
    }
}
