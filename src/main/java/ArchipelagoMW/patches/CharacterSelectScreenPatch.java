package ArchipelagoMW.patches;

import ArchipelagoMW.APClient;
import ArchipelagoMW.Archipelago;
import ArchipelagoMW.apEvents.ConnectionResult;
import ArchipelagoMW.util.DeathLinkHelper;
import basemod.CustomCharacterSelectScreen;
import basemod.ReflectionHacks;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.ModHelper;
import com.megacrit.cardcrawl.helpers.SeedHelper;
import com.megacrit.cardcrawl.screens.charSelect.CharacterOption;
import com.megacrit.cardcrawl.screens.charSelect.CharacterSelectScreen;
import dev.koifysh.archipelago.helper.DeathLink;
import downfall.downfallMod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class CharacterSelectScreenPatch {

    public static CustomCharacterSelectScreen charSelectScreen;
    private static ArrayList<CharacterOption> options;

    public static void lockNonAPChars() {
        charSelectScreen.options = new ArrayList<>(options);
        Archipelago.logger.info("Character Options {}", options.stream().map(o-> o.c.chosenClass.name()).collect(Collectors.toList()));
        charSelectScreen.options.stream().filter(o -> !ConnectionResult.availableAPChars.contains(o.c.chosenClass.name()))
                .forEach(o -> {
                    o.locked = true;
                    ReflectionHacks.setPrivate(o, CharacterOption.class, "buttonImg", ImageMaster.CHAR_SELECT_LOCKED);
                });
        Archipelago.logger.info("Available AP Chars: {}", ConnectionResult.availableAPChars);
        if (charSelectScreen.options.stream().allMatch(o -> o.locked))
        {
            // Something went very wrong, force Ironclad
            options.stream()
                    .filter(o -> o.c.chosenClass.name().equals(AbstractPlayer.PlayerClass.IRONCLAD.name()))
                    .forEach(o -> {
                        o.locked = false;
                        ReflectionHacks.setPrivate(o, CharacterOption.class, "buttonImg", ImageMaster.CHAR_SELECT_IRONCLAD);
                    });
        }
    }
    @SpirePatch(clz = CustomCharacterSelectScreen.class, method="initialize")
    public static class InitPatch {

        @SpirePostfixPatch
        public static void captureCharSelect(CustomCharacterSelectScreen __instance,  ArrayList<CharacterOption> ___allOptions)
        {
            charSelectScreen = __instance;
            options = new ArrayList<>(___allOptions);
        }
    }

    @SpirePatch(clz = CharacterSelectScreen.class, method="update")
    public static class UpdatePatch
    {
        // TODO: change to locator
        @SpireInsertPatch(rloc=191-166)
        public static void disableAscensionUnlocked(CharacterSelectScreen __instance, @ByRef boolean[] ___isAscensionModeUnlocked)
        {
            ___isAscensionModeUnlocked[0] = false;
        }
    }

    @SpirePatch(clz= CharacterSelectScreen.class, method="updateButtons")
    public static class UpdateButtonsPatch
    {

        @SpirePrefixPatch
        public static void initializeAPSettings(CharacterSelectScreen __instance)
        {
            SeedHelper.setSeed(APClient.slotData.seed);
            __instance.isAscensionMode = APClient.slotData.ascension > 0;
            __instance.ascensionLevel = APClient.slotData.ascension;
            Settings.isFinalActAvailable = APClient.slotData.finalAct == 1;

            if (APClient.slotData.deathLink > 0) {
                DeathLink.setDeathLinkEnabled(true);
            }

            DeathLinkHelper.update.sendDeath = false;

            for (CharacterOption o : __instance.options) {
                if(o.selected)
                {
                    //TODO: cleanup
                    ConnectionResult.character = o.c;
                    break;
                }
            }

        }
    }

    @SpirePatch(clz=CharacterSelectScreen.class, method="renderSeedSettings")
    public static class PreventSeedRender
    {
        public static void Replace(CharacterSelectScreen __instance, SpriteBatch sb)
        {
            // Don't render seed selection
        }
    }

    @SpirePatch(clz= CustomCharacterSelectScreen.class, method="initialize")
    public static class ForceDownfallCrossover
    {
        @SpirePrefixPatch
        public static void force()
        {
            if(ModHelper.isModEnabled("downfall"))
            {
                downfallMod.crossoverCharacters = true;
                downfallMod.crossoverModCharacters = true;
            }
        }
    }
}
