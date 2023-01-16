package ArchipelagoMW.ui.hud;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import javassist.CtBehavior;

import java.util.ArrayList;
import java.util.Collections;

public class SidePanel {

    @SpirePatch(clz = AbstractDungeon.class, method="update")
    public static class updatePlayerPanels {
        @SpirePostfixPatch
        public static void Postfix(AbstractDungeon __instance) {
            for (PlayerPanel player : SidePanel.playerPanels) {
                player.update();
            }
        }
    }

    @SpirePatch(clz = AbstractDungeon.class, method="render")
    public static class RenderPlayerPanel {

        @SpireInsertPatch(locator = Locator.class)
        public static void PostFix(AbstractDungeon __instance, SpriteBatch sb) {
            SidePanel.renderPlayers(sb);
        }

        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctBehavior) throws Exception {
                Matcher match = new Matcher.MethodCallMatcher(AbstractRoom.class, "render");
                return LineFinder.findInOrder(ctBehavior, match);
            }
        }
    }

    public static final ArrayList<PlayerPanel> playerPanels = new ArrayList<>();


    public static void updatePlayer(PlayerInfo player) {
        boolean found = false;
        for (PlayerPanel playerPanel : playerPanels) {
            if( playerPanel.getName().equals(player.getName())) {
                found = true;
                playerPanel.getPlayer().update(player);
            }
        }
        if(!found)
            playerPanels.add(new PlayerPanel(player));
        sortPlayers();
    }

    public static void sortPlayers() {
        Collections.sort(SidePanel.playerPanels);

        for (int i = 0; i < playerPanels.size(); i++) {
            playerPanels.get(i).setIndex(i);
        }
    }

    public static void renderPlayers(SpriteBatch sb) {
        for (int i = 0; i < Math.min(playerPanels.size(),6); i++) {
            playerPanels.get(i).render(sb);
        }
    }

}
