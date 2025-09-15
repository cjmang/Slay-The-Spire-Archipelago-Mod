package ArchipelagoMW.game.start.patches;

import ArchipelagoMW.client.APClient;
import ArchipelagoMW.client.APContext;
import ArchipelagoMW.game.items.AscensionManager;
import ArchipelagoMW.game.victory.patches.VictoryScreenPatch;
import ArchipelagoMW.mod.Archipelago;
import ArchipelagoMW.client.config.CharacterConfig;
import ArchipelagoMW.game.CharacterManager;
import ArchipelagoMW.game.save.ui.ConfirmPopupPatch;
import ArchipelagoMW.client.util.DeathLinkHelper;
import basemod.CustomCharacterSelectScreen;
import basemod.ReflectionHacks;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.ModHelper;
import com.megacrit.cardcrawl.helpers.SeedHelper;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.controller.CInputHelper;
import com.megacrit.cardcrawl.screens.charSelect.CharacterOption;
import com.megacrit.cardcrawl.screens.charSelect.CharacterSelectScreen;
import com.megacrit.cardcrawl.screens.options.ConfirmPopup;
import io.github.archipelagomw.events.RetrievedEvent;
import downfall.downfallMod;
import downfall.patches.EvilModeCharacterSelect;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CharacterSelectScreenPatch {
    private static final Logger logger = Logger.getLogger(CharacterSelectScreen.class.getName());

    private static ConfirmPopup resumeSave;
    public static CustomCharacterSelectScreen charSelectScreen;
    private static ArrayList<CharacterOption> options;
    private static final Map<String, Texture> originalImage = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public static void lockChars() {
        APContext ctx = APContext.getContext();
        APClient client = ctx.getClient();
        Map<String, Boolean> tmpChars = Collections.emptyMap();
        try {
            RetrievedEvent getData = client.dataStorageGetFuture(Collections.singletonList(VictoryScreenPatch.VictoryCheck.createVictoryKey(client))).get();
            tmpChars = (Map<String, Boolean>) getData.getValueAsObject(VictoryScreenPatch.VictoryCheck.createVictoryKey(client),Map.class);
            if(tmpChars == null)
            {
                tmpChars = Collections.emptyMap();
            }
        }
        catch(Exception ex)
        {
            logger.log(Level.WARNING, "Error while getting goaled characters", ex);
        }
        Map<String, Boolean> goaledCharacters = tmpChars;
        logger.log(Level.INFO, "Completed characters: {0}", goaledCharacters);
        CharacterManager charManager = ctx.getCharacterManager();
        charSelectScreen.options = new ArrayList<>(options);
        Map<String, Boolean> unlockedChars = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        Map<Long, String> unlockItemIds = new HashMap<>();
        charManager.markUnrecognziedCharacters();
        charManager.getCharacters().values().forEach(c -> {
            unlockedChars.put(c.officialName, !c.locked);
            unlockItemIds.put(14L + (20L * c.charOffset), c.officialName);
        });

        ctx.getItemManager().getReceivedItemIDs().forEach(id -> {
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

            CompletedChar.completed.set(o, goaledCharacters.getOrDefault(o.c.chosenClass.name(), false));
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
            ctx.getTrapManager().initialize();
            // updateButtons is where game start happens, more or less
            SeedHelper.setSeed(config.seed);
            ctx.getAscensionManager().initializeRunStart();
            __instance.ascensionLevel = AbstractDungeon.ascensionLevel;
            __instance.isAscensionMode = __instance.ascensionLevel > 0;
            Settings.isFinalActAvailable = config.finalAct;

            if (Loader.isModLoaded("downfall")) {
                EvilModeCharacterSelect.evilMode = config.downfall;
            }
            if (APContext.getContext().getSlotData().deathLink > 0) {
                APContext.getContext().getClient().setDeathLinkEnabled(true);
            }

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

    @SpirePatch(clz=CharacterOption.class, method=SpirePatch.CLASS)
    public static class CompletedChar
    {
        public static SpireField<Boolean> completed = new SpireField<>(() -> false);
    }

    public static final Color GREEN_OUTLINE_COLOR = new Color(0.0f, 1.0f, 0.0f, 0.5f);

    @SpirePatch(clz=CharacterOption.class, method="renderOptionButton")
    public static class CompletedOutline
    {
        @SpireInstrumentPatch
        public static ExprEditor markCompletedCharacter() {
            return new ExprEditor() {
                @Override
                public void edit(MethodCall method) throws CannotCompileException
                {
                    if(method.getClassName().equals("com.badlogic.gdx.graphics.g2d.SpriteBatch") && method.getMethodName().equals("setColor") && method.getLineNumber() == 244)
                    {
                        method.replace("{$1 = ((Boolean)ArchipelagoMW.game.start.patches.CharacterSelectScreenPatch.CompletedChar.completed.get(this)).booleanValue() ? ArchipelagoMW.game.start.patches.CharacterSelectScreenPatch.GREEN_OUTLINE_COLOR : BLACK_OUTLINE_COLOR; $proceed($$);}");
                    }
                }
            };
        }
    }

    // TODO: BaseMod's issue is it doesn't handle alt left/right
//    @SpirePatch(clz= CustomCharacterSelectScreen.class, requiredModId = "basemod", method="updateCharSelectController")
//    public static class FixBaseModCharSelect
//    {
//        public SpireReturn<Boolean> Replace(CustomCharacterSelectScreen __instance, )
//        {
//            int index = -1;
//            for(int i = 0; i < __instance.options.size(); i++)
//            {
//                CharacterOption o = __instance.options.get(i);
//                if(o.hb.hovered) {
//                    index = i;
//                    break;
//                }
//            }
//            if(index < 0)
//            {
//                return SpireReturn.Return(false);
//            }
//            int selectIndex = ReflectionHacks.getPrivate(__instance, CustomCharacterSelectScreen.class, "selectIndex");
//            int optionsPerIndex = ReflectionHacks.getPrivate(__instance, CustomCharacterSelectScreen.class, "optionsPerIndex");
//            int maxSelectIndex = ReflectionHacks.getPrivate(__instance, CustomCharacterSelectScreen.class, "maxSelectIndex");
//            boolean startOfPage = index == 0 && selectIndex != 0;
//            if(startOfPage)
//            {
//                if(CInputHelper.isJustPressed(CInputActionSet.left.keycode) || CInputHelper.isJustPressed(CInputActionSet.altLeft.keycode))
//                {
//                    ReflectionHacks.
//                }
//            }
//            boolean endOfPage = (index + 1) % optionsPerIndex == 0 && selectIndex != maxSelectIndex;
//
//        }
//    }
}
