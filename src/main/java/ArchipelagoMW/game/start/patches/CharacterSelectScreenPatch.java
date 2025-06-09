package ArchipelagoMW.game.start.patches;

import ArchipelagoMW.client.APClient;
import ArchipelagoMW.client.APContext;
import ArchipelagoMW.mod.Archipelago;
import ArchipelagoMW.client.config.CharacterConfig;
import ArchipelagoMW.game.CharacterManager;
import ArchipelagoMW.game.save.ui.ConfirmPopupPatch;
import ArchipelagoMW.client.util.DeathLinkHelper;
import basemod.CustomCharacterSelectScreen;
import basemod.ReflectionHacks;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.ModHelper;
import com.megacrit.cardcrawl.helpers.SeedHelper;
import com.megacrit.cardcrawl.screens.charSelect.CharacterOption;
import com.megacrit.cardcrawl.screens.charSelect.CharacterSelectScreen;
import com.megacrit.cardcrawl.screens.options.ConfirmPopup;
import dev.koifysh.archipelago.helper.DeathLink;
import downfall.downfallMod;
import downfall.patches.EvilModeCharacterSelect;

import java.util.*;
import java.util.stream.Collectors;

public class CharacterSelectScreenPatch {


    private static ConfirmPopup resumeSave;
    public static CustomCharacterSelectScreen charSelectScreen;
    private static ArrayList<CharacterOption> options;
    private static final Map<String, Texture> originalImage = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public static void lockChars() {
        CharacterManager charManager = APContext.getContext().getCharacterManager();
        charSelectScreen.options = new ArrayList<>(options);
        Map<String, Boolean> unlockedChars = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        Map<Long, String> unlockItemIds = new HashMap<>();
        charManager.markUnrecognziedCharacters();
        charManager.getCharacters().values().forEach(c -> {
            unlockedChars.put(c.officialName, !c.locked);
            unlockItemIds.put(14L + (20L * c.charOffset), c.officialName);
        });

        APContext.getContext().getItemManager().getReceivedItemIDs().forEach(id -> {
            String name = unlockItemIds.get(id);
            if(name != null)
            {
                unlockedChars.put(name, true);
            }
        });
        Archipelago.logger.info("Character Options {}", options.stream().map(o -> o.c.chosenClass.name()).collect(Collectors.toList()));
        for(Map.Entry<String, Boolean> entry : unlockedChars.entrySet())
        {
            Archipelago.logger.info("Character {} is unlocked: {}", entry.getKey(), entry.getValue());
        }
        charSelectScreen.options.forEach(o -> {
            Texture originalTexture = ReflectionHacks.getPrivate(o, CharacterOption.class, "buttonImg");
            if(originalTexture == ImageMaster.CHAR_SELECT_LOCKED)
            {
                originalTexture = ImageMaster.loadImage("images/ui/charSelect/crowbotButton.png");
            }
            originalImage.putIfAbsent(o.c.chosenClass.name(), originalTexture);
            CharacterConfig config = charManager.getCharacters().get(o.c.chosenClass.name());
            if (config == null || !charManager.getAvailableAPChars().contains(config.officialName) || !unlockedChars.getOrDefault(config.officialName, false)) {
                o.locked = true;
                ReflectionHacks.setPrivate(o, CharacterOption.class, "buttonImg", ImageMaster.CHAR_SELECT_LOCKED);
            } else {
                o.locked = false;
                ReflectionHacks.setPrivate(o, CharacterOption.class, "buttonImg", originalImage.get(config.officialName));
            }
        });

        Archipelago.logger.info("Available AP Chars: {}", charManager.getAvailableAPChars());
        // Fallbacks; two cases possible: either all characters are unrecognized, or all the ones which are
        // recognized are locked
        if (charManager.getCharacters().size() == charManager.getUnrecognizedCharacters().size()) {
            // Something went very wrong, force Ironclad
            options.stream()
                    .filter(o -> o.c.chosenClass.name().equals(AbstractPlayer.PlayerClass.IRONCLAD.name()))
                    .forEach(o -> {
                        o.locked = false;
                        ReflectionHacks.setPrivate(o, CharacterOption.class, "buttonImg", ImageMaster.CHAR_SELECT_IRONCLAD);
                    });
            charManager.handleIroncladOverride(charManager.getUnrecognizedCharacters().get(0));
        } else if(charSelectScreen.options.stream().allMatch(o -> o.locked)) {
            // Unlock the first recognized one
            for(CharacterOption option : charSelectScreen.options)
            {
                if(charManager.getAvailableAPChars().contains(option.c.chosenClass.name()))
                {
                    option.locked = false;
                    ReflectionHacks.setPrivate(option, CharacterOption.class, "buttonImg", originalImage.get(option.c.chosenClass.name()));
                    break;
                }
            }
        }
    }


    @SpirePatch(clz = CustomCharacterSelectScreen.class, method = "initialize")
    public static class InitPatch {

        @SpirePostfixPatch
        public static void captureCharSelect(CustomCharacterSelectScreen __instance, ArrayList<CharacterOption> ___allOptions) {
            charSelectScreen = __instance;
            APClient.logger.info("Intializing custom character select screen");
            options = new ArrayList<>(___allOptions);
            resumeSave = new ConfirmPopup("Resume?", "Archipelago Save Detected would you like to resume?", ConfirmPopupPatch.AP_SAVE_RESUME);
        }
    }

    @SpirePatch(clz = CharacterSelectScreen.class, method = "update")
    public static class UpdatePatch {
        @SpireInsertPatch(rloc = 191 - 166)
        public static void disableAscensionUnlocked(CharacterSelectScreen __instance, @ByRef boolean[] ___isAscensionModeUnlocked) {
            ___isAscensionModeUnlocked[0] = false;
        }

        @SpirePostfixPatch
        public static void updateResumeSave(CharacterSelectScreen __instance) {
            resumeSave.update();
        }
    }

    @SpirePatch(clz = CharacterSelectScreen.class, method = "render", paramtypez = {SpriteBatch.class})
    public static class RenderPatch {
        @SpirePostfixPatch
        public static void renderConfirm(CharacterSelectScreen __instance, SpriteBatch sb) {
            resumeSave.render(sb);
        }
    }

    @SpirePatch(clz = CharacterSelectScreen.class, method = "updateButtons")
    public static class UpdateButtonsPatch {

        @SpireInsertPatch(rloc = 297 - 280)
        public static SpireReturn<Void> showSaveConfirm(CharacterSelectScreen __instance) {
            if (!__instance.confirmButton.hb.clicked) {
                return SpireReturn.Continue();
            }
            APContext ctx = APContext.getContext();
            CharacterManager characterManager = ctx.getCharacterManager();
            for (CharacterOption o : __instance.options) {
                if (o.selected) {
                    //TODO: cleanup
                    //ConnectionResult.character = o.c;
                    if (!characterManager.selectCharacter(o.c.chosenClass.name())) {
                        throw new RuntimeException("Attempting to play AP with an unrecognized character " + o.c.chosenClass.name());
                    }
                    break;
                }
            }
            if (ctx.getSaveManager().hasSave(characterManager.getCurrentCharacter().chosenClass.name())) {
                resumeSave.show();
                __instance.confirmButton.hb.clicked = false;
                return SpireReturn.Return();
            }
            return SpireReturn.Continue();
        }

        @SpireInsertPatch(rloc = 298 - 280)
        public static void initializeAPSettings(CharacterSelectScreen __instance) {
            APContext ctx = APContext.getContext();
            CharacterManager charManager = ctx.getCharacterManager();
            CharacterConfig config = charManager.getCurrentCharacterConfig();
            ctx.getItemTracker().initialize(ctx.getItemManager().getReceivedItemIDs());
            // updateButtons is where game start happens, more or less
            SeedHelper.setSeed(config.seed);
            __instance.isAscensionMode = config.ascension > 0;
            __instance.ascensionLevel = config.ascension;
            Settings.isFinalActAvailable = config.finalAct;

            if (Loader.isModLoaded("downfall")) {
                EvilModeCharacterSelect.evilMode = config.downfall;
            }

            if (APContext.getContext().getSlotData().deathLink > 0) {
                DeathLink.setDeathLinkEnabled(true);
            }

            DeathLinkHelper.update.sendDeath = false;
            APContext.getContext().getLocationTracker().sendPressStart(config);

        }
    }

    @SpirePatch(clz = CharacterSelectScreen.class, method = "renderSeedSettings")
    public static class PreventSeedRender {
        public static void Replace(CharacterSelectScreen __instance, SpriteBatch sb) {
            // Don't render seed selection
        }
    }

    @SpirePatch(clz = CustomCharacterSelectScreen.class, method = "initialize")
    public static class ForceDownfallCrossover {
        @SpirePrefixPatch
        public static void forceFullCrossover() {
            if (ModHelper.isModEnabled("downfall")) {
                downfallMod.crossoverCharacters = true;
                downfallMod.crossoverModCharacters = true;
            }
        }
    }
}
