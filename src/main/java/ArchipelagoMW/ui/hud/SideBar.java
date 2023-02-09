package ArchipelagoMW.ui.hud;

import ArchipelagoMW.APSettings;
import ArchipelagoMW.Archipelago;
import ArchipelagoMW.teams.TeamManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.neow.NeowEvent;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import downfall.events.HeartEvent;
import javassist.CtBehavior;

import java.util.Calendar;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;

public class SideBar {

    @SpirePatch(clz = AbstractDungeon.class, method = "update")
    public static class updatePlayerPanels {
        @SpirePostfixPatch
        public static void Postfix(AbstractDungeon __instance) {
            Archipelago.sideBar.update();
        }
    }

    @SpirePatch(clz = AbstractDungeon.class, method = "render")
    public static class RenderPlayerPanel {

        @SpireInsertPatch(locator = Locator.class)
        public static void insert(AbstractDungeon __instance, SpriteBatch sb) {
            Archipelago.sideBar.render(sb);
        }

        @SpirePostfixPatch
        public static void postfix(AbstractDungeon __instance, SpriteBatch sb) {
            Archipelago.sideBar.topRender(sb);
        }

        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctBehavior) throws Exception {
                Matcher match = new Matcher.MethodCallMatcher(AbstractRoom.class, "render");
                return LineFinder.findInOrder(ctBehavior, match);
            }
        }
    }

    public final CopyOnWriteArrayList<PlayerPanel> playerPanels = new CopyOnWriteArrayList<>();

    // Group UI
    public boolean showTeamButton = true;
    public APTeamsButton APTeamsButton;
    public boolean showTeamPanel = false;

    public static boolean useTeams = false;

    public APTeamsPanel APTeamsPanel;

    private final float x;
    private final float y;

    public SideBar() {
        this.x = 0;
        this.y = Settings.HEIGHT - (280f * Settings.scale);

        APTeamsButton = new APTeamsButton("Teams");
        APTeamsButton.set(x + PlayerPanel.getWidth() - APTeamsButton.getWidth(), y);

        APTeamsPanel = new APTeamsPanel();
        APTeamsPanel.setPos(this.x + PlayerPanel.getWidth() + 5f * Settings.scale, y);
    }

    public void update() {
        for (PlayerPanel player : playerPanels) {
            player.update();
        }

        if (Loader.isModLoaded("downfall"))
            showTeamButton = AbstractDungeon.currMapNode.room.event instanceof NeowEvent || AbstractDungeon.currMapNode.room.event instanceof HeartEvent;
        else
            showTeamButton = AbstractDungeon.currMapNode.room.event instanceof NeowEvent;

        //just disable it for now
        showTeamButton = false;

        if (showTeamButton && !AbstractDungeon.isScreenUp && useTeams) {
            APTeamsButton.update();
        }

        if (showTeamPanel && !AbstractDungeon.isScreenUp) {
            APTeamsPanel.update();
        }
    }


    public void sortPlayers() {
        try {
            Collections.sort(playerPanels);
        } catch (Exception e){
            e.printStackTrace();
            return;
        }

        int i = 0;
        for (PlayerPanel playerPanel : playerPanels) {
            //always set the position to where it WOULD be if it rendered. (makes animations for if it needs to render later nicer
            playerPanel.setPos(x, y - 80 * Settings.scale - (85 * Math.min(i, 5) * Settings.scale));

            if(playerPanel.getPlayer().getName().equals(CardCrawlGame.playerName)) {
                i++;
                playerPanel.shouldRender = true;
                continue;
            }

            String team = playerPanel.getPlayer().team;
            if (team != null && TeamManager.myTeam != null && team.equals(TeamManager.myTeam.name)) {
                playerPanel.shouldRender = true;
                i++;
                continue; // render this panel and increment index
            }

            Calendar lastInteraction = Calendar.getInstance();
            lastInteraction.setTimeInMillis(playerPanel.getPlayer().timestamp);
            Calendar tenMinutesAgo = Calendar.getInstance();
            tenMinutesAgo.add(Calendar.MINUTE, -10);

            if (tenMinutesAgo.after(lastInteraction) && APSettings.playerFilter != APSettings.FilterType.ALL) {
                playerPanel.shouldRender = false;
                continue; // skip if more than 10 minutes ago and player filter is not all.
            }
            //it's within 10 minutes
            if (APSettings.playerFilter == APSettings.FilterType.RECENT) {
                i++;
                playerPanel.shouldRender = true;
            } else
                playerPanel.shouldRender = false;

        }
    }

    public void render(SpriteBatch sb) {
        renderPlayers(sb);
        if (showTeamButton && useTeams) {
            APTeamsButton.render(sb);
        }
    }

    public void topRender(SpriteBatch sb) {
        if (showTeamButton && showTeamPanel && !AbstractDungeon.isScreenUp) {
            APTeamsPanel.render(sb);
        }
    }

    public void renderPlayers(SpriteBatch sb) {
        int i = 0;
        for (PlayerPanel playerPanel : playerPanels) {
            if (playerPanel.shouldRender) {
                playerPanel.render(sb);
                i++;
            }
            if (i >= 6)
                return;
        }
    }
}
