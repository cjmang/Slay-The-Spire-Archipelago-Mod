package ArchipelagoMW.game.items.ui;

import ArchipelagoMW.client.APContext;
import ArchipelagoMW.client.config.CharacterConfig;
import ArchipelagoMW.game.CharacterManager;
import ArchipelagoMW.game.items.APItemID;
import ArchipelagoMW.game.start.patches.CharacterSelectScreenPatch;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.TipHelper;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.screens.charSelect.CharacterOption;
import io.github.archipelagomw.ItemManager;
import javassist.CtBehavior;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class ItemStatusPatch {
    private static CharacterOption option;
    private static String text = "";

    @SpirePatch(cls="downfall.patches.EvilModeCharacterOption", method="Prefix", requiredModId = "downfall", paramtypez = CharacterOption.class)
    public static class EvilOptionPatchPatch
    {
        @SpireInsertPatch(locator= EvilOptionPatchPatch.Locator.class)
        public static void insert(CharacterOption __instance)
        {
            CharacterManager charManager = APContext.getContext().getCharacterManager();
            if(__instance.hb.hovered && __instance.locked && charManager.getAvailableAPChars().contains(__instance.c.chosenClass.name()))
            {
                TipHelper.renderGenericTip(InputHelper.mX + 70.F* Settings.scale,
                        InputHelper.mY - 10.0F * Settings.scale, "Locked",
                        "Locked until you are sent an unlock item in the Multiworld.");
            }
        }
        private static class Locator extends SpireInsertLocator {

            @Override
            public int[] Locate(CtBehavior ctBehavior) throws Exception {
                Matcher matcher = new Matcher.FieldAccessMatcher(CharacterOption.class, "locked");
                return LineFinder.findInOrder(ctBehavior, new ArrayList<>(), matcher);
            }
        }
    }

    @SpirePatch(clz= CharacterOption.class, method="updateHitbox")
    public static class HitboxPatch
    {

        @SpireInsertPatch(locator=Locator.class)
        public static void insert(CharacterOption __instance)
        {
            CharacterManager charManager = APContext.getContext().getCharacterManager();
            if(__instance.hb.hovered && __instance.locked && charManager.getAvailableAPChars().contains(__instance.c.chosenClass.name()))
            {
                TipHelper.renderGenericTip(InputHelper.mX + 70.F* Settings.scale,
                        InputHelper.mY - 10.0F * Settings.scale, "Locked",
                        "Locked until you are sent an unlock item in the Multiworld.");
            }

        }


        private static class Locator extends SpireInsertLocator {

            @Override
            public int[] Locate(CtBehavior ctBehavior) throws Exception {
                Matcher matcher = new Matcher.FieldAccessMatcher(CharacterOption.class, "locked");
                return LineFinder.findInOrder(ctBehavior, new ArrayList<>(), matcher);
            }
        }

        @SpirePostfixPatch
        public static void Postfix(CharacterOption __instance)
        {
            if(!__instance.hb.hovered || __instance.locked || CharacterSelectScreenPatch.CompletedChar.completed.get(__instance))
            {
                return;
            }
            CharacterManager charManager = APContext.getContext().getCharacterManager();
            ItemManager itemManager = APContext.getContext().getItemManager();
            CharacterConfig config = charManager.getCharacters().get(__instance.c.chosenClass.name());
            if(config == null)
            {
                return;
            }

            if(option != __instance) {
                StringBuilder sb = new StringBuilder();
                Map<String, Integer> countMap = new LinkedHashMap<>();
                countMap.put("Boss Relics", 0);
                countMap.put("Relics", 0);
                countMap.put("Card Rewards", 0);
                countMap.put("Gold", 0);
                itemManager.getReceivedItemIDs().stream()
                        .filter(id -> charManager.isItemIDForCharacter(id, config))
                        .filter(id -> !APItemID.isGeneric(id))
                        .forEach(id -> {
                            APItemID itemID = APItemID.fromLong(id);
                            switch(itemID)
                            {
                                case BOSS_RELIC:
                                    countMap.compute("Boss Relics", (a,b) -> b == null ? 1 : b+1);
                                    break;
                                case RELIC:
                                    countMap.compute("Relics", (a,b) -> b == null ? 1 : b+1);
                                    break;
                                case RARE_CARD_DRAW:
                                case CARD_DRAW:
                                    countMap.compute("Card Rewards", (a,b) -> b == null ? 1 : b+1);
                                    break;
                                case ONE_GOLD:
                                    countMap.compute("Gold", (a,b) -> b == null ? 1 : b+1);
                                    break;
                                case FIVE_GOLD:
                                    countMap.compute("Gold", (a,b) -> b == null ? 5 : b+5);
                                    break;
                                case FIFTEEN_GOLD:
                                    countMap.compute("Gold", (a,b) -> b == null ? 15 : b+15);
                                    break;
                                case THIRTY_GOLD:
                                    countMap.compute("Gold", (a,b) -> b == null ? 30: b+30);
                                    break;
                                case BOSS_GOLD:
                                    // yeah yeah
                                    countMap.compute("Gold", (a,b) -> b == null ? 75: b+75);
                                    break;
                                case CARD_REMOVE_SLOT:
                                case CARD_SLOT:
                                case NEUTRAL_CARD_SLOT:
                                case POTION_SLOT:
                                case RELIC_SLOT:
                                    countMap.compute("Shop Slots", (a,b) -> b == null ? 1: b+1);
                                    break;
                                case PROGRESSIVE_REST:
                                    countMap.compute("Progressive Rest", (a,b) -> b == null ? 1: b+1);
                                    break;
                                case PROGRESSIVE_SMITH:
                                    countMap.compute("Progressive Smith", (a,b) -> b == null ? 1: b+1);
                                    break;
                                case ASCENSION_DOWN:
                                    countMap.compute("Ascension Down", (a,b) -> b == null ? 1: b+1);
                                    break;
                                case RUBY_KEY:
                                case EMERALD_KEY:
                                case SAPPHIRE_KEY:
                                    countMap.compute("Keys", (a,b) -> b == null ? 1 : b+1);
                                    break;
                            }
                });
                for(Map.Entry<String, Integer> entry: countMap.entrySet())
                {
                    sb.append(entry.getValue())
                            .append(" ")
                            .append(entry.getKey())
                            .append(" NL ");
                }
                sb.setLength(sb.length() - 4);
                text = sb.toString();
            }
            option = __instance;
            if(text.isEmpty())
            {
                return;
            }
            TipHelper.renderGenericTip(InputHelper.mX + 70.0F * Settings.xScale, InputHelper.mY - 10.0F * Settings.scale,
                    "Items Received", text);
        }

    }
}
