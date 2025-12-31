package ArchipelagoMW.game;

import ArchipelagoMW.client.APClient;
import ArchipelagoMW.game.ui.SpeechBubblePatch;
import ArchipelagoMW.mod.APSettings;
import ArchipelagoMW.saythespire.SayTheSpire;
import basemod.ReflectionHacks;
import com.badlogic.gdx.graphics.Color;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.ui.DialogWord;
import com.megacrit.cardcrawl.ui.SpeechWord;
import com.megacrit.cardcrawl.vfx.SpeechBubble;
import io.github.archipelagomw.Print.APPrintColor;
import io.github.archipelagomw.Print.APPrintPart;
import io.github.archipelagomw.events.PrintJSONEvent;
import io.github.archipelagomw.flags.NetworkItem;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TalkQueue {
    private static final float SPEECH_DURATION = 7.0f;

    public static void playerTalk(String msg)
    {
        AbstractPlayer player = AbstractDungeon.player;
        SpeechBubblePatch.skipLowLevelText = true;
        SpeechBubble bubble = new SpeechBubble(player.dialogX, player.dialogY, 3.0f, msg, true);
        AbstractDungeon.topLevelEffects.add(bubble);
        SpeechBubblePatch.skipLowLevelText = false;
    }

    public static void topLevelTalk(float x, float y, float duration, String msg, boolean isPlayer, boolean faceRight)
    {
        SpeechBubblePatch.skipLowLevelText = true;
        SpeechBubble bubble = new SpeechBubble(x, y, duration, msg, isPlayer);
        if(faceRight) {
            ReflectionHacks.setPrivate(bubble, SpeechBubble.class, "facingRight", true);
        }
        AbstractDungeon.topLevelEffects.add(bubble);
        SpeechBubblePatch.skipLowLevelText = false;
    }

    @SpirePatch(clz= SpeechWord.class, method="getColor")
    public static class AddPurplePatch
    {
        @SpirePrefixPatch
        public static SpireReturn<Color> addPurple(SpeechWord __instance, DialogWord.WordColor ___wColor)
        {
            if(___wColor == DialogWord.WordColor.PURPLE)
            {
                return SpireReturn.Return(Settings.PURPLE_COLOR.cpy());
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz=SpeechWord.class, method="identifyWordColor", paramtypez = {String.class})
    public static class FindPurplePatch
    {
        @SpireInsertPatch(rloc=187-186)
        public static SpireReturn<DialogWord.WordColor> findPurple(String word)
        {
            if(word.charAt(1) == 'p')
            {
                return SpireReturn.Return(DialogWord.WordColor.PURPLE);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz= AbstractDungeon.class, method="update")
    public static class AbstractDungeonPatch
    {
        private static final List<BubbleGenerator> speechBubbles = new ArrayList<>();
        public static final LinkedList<PrintJSONEvent> talkQueue = new LinkedList<>();

        static {
            speechBubbles.add(new BubbleGenerator(0.0f));
            speechBubbles.add(new BubbleGenerator(210f));
            speechBubbles.add(new BubbleGenerator(-210f));
        }

        @SpirePostfixPatch
        public static void checkForTalking()
        {
            if(talkQueue.isEmpty())
            {
                return;
            }

            PrintJSONEvent event = talkQueue.getLast();
            for(BubbleGenerator gen : speechBubbles)
            {
                if(gen.canSend())
                {
                    gen.send(AbstractDungeon.player, event);
                    talkQueue.removeLast();
                    break;
                }
            }
        }

        public static String transformMessage(PrintJSONEvent event)
        {
            StringBuilder sb = new StringBuilder();
            int count = 0;
            for(APPrintPart part : event.apPrint.parts)
            {
                if(count >= 16 && !part.text.contains(")"))
                {
                    sb.append(" NL ");
                    count = 0;
                }
                APClient.logger.info("Color: {} Text: {} Player: {} Flags: {} Type: {}", part.color, part.text, part.player, part.flags, part.type);
                count += part.text.length();
                transformPart(sb, part);
            }
            APClient.logger.info(sb.toString());
            return sb.toString();
        }

        public static void transformPart(StringBuilder sb, APPrintPart part)
        {
            if(part.type == null)
            {
                if(part.text != null)
                {
                    sb.append(part.text);
                }
                return;

            }
            switch(part.type)
            {
                case playerID:
                case playerName:
                    perWord(sb, part.text, "#r", "");
                    break;
                case locationID:
                case locationName:
                case text:
                default:
                    sb.append(part.text);
                    break;
                case itemID:
                case itemName:
                    handleItem(sb, part);
                    break;
            }
        }

        public static void perWord(StringBuilder sb, String text, String prefix, String wrapper)
        {
            if(APSettings.hideColorWords() && !"#p".equals(prefix))
            {
                prefix = "";
            }
            for(String word : text.split("\\s+"))
            {
                sb.append(prefix)
                        .append(wrapper)
                        .append(word)
                        .append(wrapper)
                        .append(" ");
            }
        }

        private static void handleItem(StringBuilder sb, APPrintPart part)
        {
            if((NetworkItem.TRAP & part.flags) > 0)
            {
                perWord(sb, part.text, "#r", "@");
                return;
            }
            if((NetworkItem.ADVANCEMENT & part.flags) > 0)
            {
                if((NetworkItem.USEFUL & part.flags) > 0)
                {
                    perWord(sb, part.text, "#p", "~");
                }
                else
                {
                    perWord(sb, part.text, "#p", "");
                }
                return;
            }
            if((NetworkItem.USEFUL & part.flags) > 0)
            {
                perWord(sb, part.text, "#b", "");
                return;
            }
            perWord(sb, part.text, "#g", "");
        }


        private static class BubbleGenerator
        {
            private static final long DEBOUNCE_THRESHOLD = TimeUnit.SECONDS.toNanos((long) SPEECH_DURATION);
            private long lastSent = System.nanoTime();
            private final float yOffset;

            BubbleGenerator(float yOffset)
            {
                this.yOffset = yOffset;
            }

            boolean canSend()
            {
                return (System.nanoTime() - lastSent) >= DEBOUNCE_THRESHOLD;
            }

            void send(AbstractPlayer player, PrintJSONEvent event)
            {
                topLevelTalk(player.dialogX - 300F, player.dialogY + yOffset, SPEECH_DURATION, transformMessage(event), true, true);
                SayTheSpire.sts.output(event.apPrint.getPlainText());
                lastSent = System.nanoTime();
            }
        }


        public static String transformColor(APPrintColor color)
        {
            switch(color)
            {
                case red_bg:
                case red:
                    return "#r";
                case green_bg:
                case green:
                case bold:
                case underline:
                    return "#g";
                case yellow_bg:
                case yellow:
                    return "#y";
                case blue_bg:
                case blue:
                case cyan_bg:
                case cyan:
                    return "#b";
                case magenta:
                case magenta_bg:
                case purple_bg:
                    return "#p";
                case white:
                case black_bg:
//                case magenta_bg:
                case white_bg:
                case black:
                default:
                    return "";
            }
        }
    }
}
