package ArchipelagoMW.game;

import ArchipelagoMW.client.APClient;
import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.vfx.SpeechBubble;
import dev.koifysh.archipelago.Print.APPrintColor;
import dev.koifysh.archipelago.Print.APPrintPart;
import dev.koifysh.archipelago.events.PrintJSONEvent;
import dev.koifysh.archipelago.flags.NetworkItem;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TalkQueue {
    private static final float SPEECH_DURATION = 5.0f;


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
//            if(AbstractDungeon.getCurrRoom().phase != AbstractRoom.RoomPhase.COMBAT || talkQueue.isEmpty())
//            {
//                return;
//            }
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
//            switch(event.type)
//            {
//                case ItemSend:
//                case ItemCheat:
//                case Chat:
//                    AbstractPlayer player = AbstractDungeon.player;
////                    AbstractDungeon.actionManager.addToBottom(new TalkAction(AbstractDungeon.player, event.apPrint.getPlainText()));
//                    SpeechBubble bubble = new SpeechBubble(player.dialogX - 300F, player.dialogY, 20.F, transformMessage(event), true);
//                    ReflectionHacks.setPrivate(bubble, SpeechBubble.class, "facingRight", true);
//                    AbstractDungeon.effectList.add(bubble);
//                    AbstractDungeon.effectList.add(new SpeechBubble(player.dialogX - 300F, player.dialogY - 210F, SPEECH_DURATION, event.apPrint.getPlainText() + " Message 2", true));
//                    AbstractDungeon.effectList.add(new SpeechBubble(player.dialogX - 300F, player.dialogY + 210F, SPEECH_DURATION, event.apPrint.getPlainText() + " Message 3", true));
////                    AbstractDungeon.effectList.add(new ThoughtBubble(player.dialogX, player.dialogY, event.apPrint.getPlainText(), true));
//                default:
//            }
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

        private static void perWord(StringBuilder sb, String text, String prefix, String wrapper)
        {
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
                    perWord(sb, part.text, "#b", "~");
                }
                else
                {
                    perWord(sb, part.text, "#b", "");
                }
                return;
            }
            if((NetworkItem.USEFUL & part.flags) > 0)
            {
                perWord(sb, part.text, "#y", "");
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
                SpeechBubble bubble = new SpeechBubble(player.dialogX - 300F, player.dialogY + yOffset, SPEECH_DURATION, transformMessage(event), true);
                ReflectionHacks.setPrivate(bubble, SpeechBubble.class, "facingRight", true);
                AbstractDungeon.effectList.add(bubble);
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
                case magenta:
                case magenta_bg:
                case purple_bg:
                    return "#b";
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
