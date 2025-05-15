package ArchipelagoMW.client.util;

import ArchipelagoMW.client.APClient;
import ArchipelagoMW.client.APContext;
import ArchipelagoMW.game.teams.TeamManager;
import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.screens.DeathScreen;
import com.megacrit.cardcrawl.screens.GameOverScreen;
import com.megacrit.cardcrawl.screens.options.ConfirmPopup;
import dev.koifysh.archipelago.events.ArchipelagoEventListener;
import dev.koifysh.archipelago.events.DeathLinkEvent;
import dev.koifysh.archipelago.helper.DeathLink;
import gremlin.actions.GremlinSwapAction;
import gremlin.characters.GremlinCharacter;
import gremlin.orbs.GremlinStandby;
import javassist.CtBehavior;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DeathLinkHelper {

    public static float damagePercent = 0;

    public DeathLinkHelper(int deathLink) {
        damagePercent = deathLink / 100f;
    }

    @ArchipelagoEventListener
    public void onDeathLink(DeathLinkEvent event) {
        //ohh hey! look its a death link event.. *hopefully*
        int damage = (int) (AbstractDungeon.player.maxHealth * damagePercent);
        update.pendingDamage += damage;
        update.cause = event.cause;
    }

    @SpirePatch(clz = AbstractDungeon.class, method = "update")
    public static class update {
        public static int pendingDamage = 0;
        public static String cause;
        public static boolean sendDeath = true;

        public static void Prefix() {
            if (pendingDamage <= 0)
                return;

            if (Loader.isModLoaded("downfall") && AbstractDungeon.player instanceof GremlinCharacter) {
                GremlinCharacter p = (GremlinCharacter) AbstractDungeon.player;

                p.currentHealth -= pendingDamage;
                p.healthBarUpdatedEvent();
                p.damageGremlins(pendingDamage);

                if (AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT) {
                    for (int i = 0; i < p.maxOrbs; ++i) {
                        if (p.orbs.get(i) instanceof GremlinStandby) {
                            GremlinStandby gs = (GremlinStandby) p.orbs.get(i);
                            gs.hp -= pendingDamage;
                        }
                    }

                    if (p.currentHealth <= 0) {
                        boolean anotherGrem = false;
                        for (int i = 0; i < p.maxOrbs; ++i) {
                            if (p.orbs.get(i) instanceof GremlinStandby) {
                                GremlinStandby gs = (GremlinStandby) p.orbs.get(i);
                                if (gs.hp > 0) {
                                    anotherGrem = true;
                                    break;
                                }
                            }
                        }
                        if (anotherGrem) {
                            (new GremlinSwapAction()).update();
                            pendingDamage = 0;
                            return;
                        }
                    }
                } else if (p.mobState.gremlinHP.get(p.mobState.gremlins.indexOf(p.currentGremlin)) <= 0) {

                    p.gremlinDeathSFX();
                    String newGremlin = null;
                    int newIndex = 0;
                    for (int i = 0; i < 5; ++i) {
                        if (p.mobState.gremlinHP.get(i) > 0) {
                            newGremlin = p.mobState.gremlins.get(i);
                            newIndex = i;
                            break;
                        }
                    }
                    if (newGremlin != null) {
                        p.currentHealth = p.mobState.gremlinHP.get(newIndex);
                        p.currentGremlin = newGremlin;
                        pendingDamage = 0;
                        return;
                    }
                }
            } else {
                AbstractDungeon.player.currentHealth -= pendingDamage;
                AbstractDungeon.player.healthBarUpdatedEvent();
            }

            if (AbstractDungeon.player.currentHealth <= 0 && !AbstractDungeon.player.isDead) {
                AbstractDungeon.player.currentHealth = 0;
                AbstractDungeon.player.isDead = true;
                sendDeath = false;
                AbstractDungeon.deathScreen = new DeathScreen(null);
                ReflectionHacks.setPrivate(AbstractDungeon.deathScreen, DeathScreen.class, "deathText", cause);
                AbstractDungeon.screen = AbstractDungeon.CurrentScreen.DEATH;
            } else {
                sendDeath = true;
            }
            pendingDamage = 0;
        }
    }

    @SpirePatch(clz = ConfirmPopup.class, method = "yesButtonEffect")
    public static class abandon {
        @SpireInsertPatch(locator = Locator.class)
        public static void Insert() {
            update.sendDeath = false;
        }

        private static class Locator extends SpireInsertLocator {

            @Override
            public int[] Locate(CtBehavior ctBehavior) throws Exception {
                Matcher deathScreenMather = new Matcher.FieldAccessMatcher(AbstractDungeon.class, "deathScreen");
                return LineFinder.findInOrder(ctBehavior, new ArrayList<>(), deathScreenMather);
            }
        }
    }

    @SpirePatch(clz = DeathScreen.class, method = SpirePatch.CONSTRUCTOR)
    public static class death {
        public static void Postfix(DeathScreen __instance) {
            TeamManager.leaveTeam();
            if (GameOverScreen.isVictory || !update.sendDeath || damagePercent <= 0) {
                //APClient.apClient.disconnect();
                return;
            }
            APClient client = APContext.getContext().getClient();

            MonsterGroup monsters = ReflectionHacks.getPrivate(__instance, DeathScreen.class, "monsters");
            if (monsters == null) {
                DeathLink.SendDeathLink(client.getAlias(), null);
                //APClient.apClient.disconnect();
                return;
            }
            HashMap<String, Integer> mobs = new HashMap<>();
            for (AbstractMonster monster : monsters.monsters) {
                if (mobs.containsKey(monster.name)) {
                    mobs.put(monster.name, mobs.get(monster.name) + 1);
                } else {
                    mobs.put(monster.name, 1);
                }
            }

            if (!mobs.isEmpty()) {
                StringBuilder sb = new StringBuilder(client.getAlias())
                        .append(" was slaughtered by ");
                for (Map.Entry<String, Integer> entry : mobs.entrySet()) {
                    if (entry.getValue() > 1) {
                        sb.append(entry.getValue());
                        sb.append(" ");
                        sb.append(entry.getKey());
                        sb.append("s ");
                    } else {
                        sb.append(entry.getKey());
                        sb.append(" ");
                    }
                }
                DeathLink.SendDeathLink(client.getAlias(), sb.toString());
                //APClient.apClient.disconnect();
                return;
            }
            DeathLink.SendDeathLink(client.getAlias(), null);
            //APClient.apClient.disconnect();
        }
    }
}
