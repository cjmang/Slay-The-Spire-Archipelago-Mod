package ArchipelagoMW.client.util;

import ArchipelagoMW.client.APClient;
import ArchipelagoMW.client.APContext;
import ArchipelagoMW.game.TalkQueue;
import ArchipelagoMW.game.teams.TeamManager;
import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.screens.DeathScreen;
import com.megacrit.cardcrawl.screens.GameOverScreen;
import com.megacrit.cardcrawl.screens.options.ConfirmPopup;
import com.megacrit.cardcrawl.vfx.combat.FlashAtkImgEffect;
import io.github.archipelagomw.events.ArchipelagoEventListener;
import io.github.archipelagomw.events.DeathLinkEvent;
import gremlin.actions.GremlinSwapAction;
import gremlin.characters.GremlinCharacter;
import gremlin.orbs.GremlinStandby;
import javassist.CtBehavior;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class DeathLinkHelper {

    public float damagePercent = 0f;
    private final AtomicBoolean sendDeath = new AtomicBoolean(true);
    private final boolean initializedWithDeath;

    public DeathLinkHelper(int deathLink) {
        damagePercent = deathLink / 100f;
        if(damagePercent <= 0 )
        {
            damagePercent = 0f;
            initializedWithDeath = false;
            sendDeath.set(false);
        }
        else
        {
            initializedWithDeath = true;
        }
    }

    @ArchipelagoEventListener
    public void onDeathLink(DeathLinkEvent event) {
        APClient.logger.info("Deathlink received: sauce: {}, time: {}", event.source, event.time);
        //ohh hey! look its a death link event.. *hopefully*
        int damage = (int) Math.ceil(AbstractDungeon.player.maxHealth * damagePercent);
        update.cause.set(event.cause);
        update.source.set(event.source);
        update.pendingDamage.addAndGet(damage);
    }

    public boolean shouldSendDeath() {
        return sendDeath.get();
    }

    public void setSendDeath(boolean sendDeath) {
        this.sendDeath.set(sendDeath && initializedWithDeath);
    }

    @SpirePatch(clz = AbstractDungeon.class, method = "update")
    public static class update {
        public static final AtomicInteger pendingDamage = new AtomicInteger(0);
        public static final AtomicReference<String> cause = new AtomicReference<>();
        public static final AtomicReference<String> source = new AtomicReference<>();

        public static void Prefix() {
            int incomingDmg = pendingDamage.getAndSet(0);
            if (incomingDmg <= 0)
                return;
            DeathLinkHelper deathLink = APContext.getContext().getDeathLinkHelper();
            APClient.logger.info("Incoming damage: {}", incomingDmg);
            AbstractPlayer player = AbstractDungeon.player;
            String causeStr = cause.get();
            String sourceStr = source.get();
            StringBuilder sb = new StringBuilder("#p");
            sb.append(sourceStr).append(" Died!");
            if(causeStr != null && !causeStr.isEmpty())
            {
                sb.append(" NL ");
                TalkQueue.AbstractDungeonPatch.perWord(sb, causeStr, "#r", "@");
            }
            AbstractDungeon.topLevelEffectsQueue.add(new FlashAtkImgEffect(player.hb.cX, player.hb.cY, AbstractGameAction.AttackEffect.BLUNT_HEAVY, false));
            TalkQueue.topLevelTalk(player.dialogX - 500F, player.dialogY, 5.0f, sb.toString(), true, true);

            if (Loader.isModLoaded("downfall") && AbstractDungeon.player instanceof GremlinCharacter) {
                GremlinCharacter p = (GremlinCharacter) AbstractDungeon.player;

                p.currentHealth -= incomingDmg;
                p.healthBarUpdatedEvent();
                p.damageGremlins(incomingDmg);

                if (AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT) {
                    for (int i = 0; i < p.maxOrbs; ++i) {
                        if (p.orbs.get(i) instanceof GremlinStandby) {
                            GremlinStandby gs = (GremlinStandby) p.orbs.get(i);
                            gs.hp -= incomingDmg;
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
                        return;
                    }
                }
            } else {
                AbstractDungeon.player.currentHealth -= incomingDmg;
                AbstractDungeon.player.healthBarUpdatedEvent();
            }

            if (AbstractDungeon.player.currentHealth <= 0 && !AbstractDungeon.player.isDead) {
                APClient.logger.info("Player died from deathlink.");
                AbstractDungeon.player.currentHealth = 0;
                AbstractDungeon.player.isDead = true;
                deathLink.setSendDeath(false);
                AbstractDungeon.deathScreen = new DeathScreen(null);
                ReflectionHacks.setPrivate(AbstractDungeon.deathScreen, DeathScreen.class, "deathText", causeStr);
                AbstractDungeon.screen = AbstractDungeon.CurrentScreen.DEATH;
            } else {
                deathLink.setSendDeath(true);
            }
        }
    }

    @SpirePatch(clz = ConfirmPopup.class, method = "yesButtonEffect")
    public static class abandon {
        @SpireInsertPatch(locator = Locator.class)
        public static void Insert() {
            APContext.getContext().getDeathLinkHelper().setSendDeath(false);
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
            DeathLinkHelper deathLinkHelper = APContext.getContext().getDeathLinkHelper();;
            if (GameOverScreen.isVictory || !deathLinkHelper.shouldSendDeath() || deathLinkHelper.damagePercent <= 0) {
                //APClient.apClient.disconnect();
                deathLinkHelper.setSendDeath(true);
                return;
            }
            APClient.logger.info("Player is on deathscreen");
            APClient client = APContext.getContext().getClient();

            MonsterGroup monsters = ReflectionHacks.getPrivate(__instance, DeathScreen.class, "monsters");
            if (monsters == null) {
                APClient.logger.info("Sending deathlink");
                client.sendDeathlink(client.getAlias(), null);
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
                APClient.logger.info("Sending deathlink");
                client.sendDeathlink(client.getAlias(), sb.toString());
                //APClient.apClient.disconnect();
                return;
            }
            APClient.logger.info("Sending deathlink");
            client.sendDeathlink(client.getAlias(), null);
            //APClient.apClient.disconnect();
        }
    }
}
