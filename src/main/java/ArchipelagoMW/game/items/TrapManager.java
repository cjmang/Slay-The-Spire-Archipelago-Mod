package ArchipelagoMW.game.items;

import ArchipelagoMW.client.APClient;
import ArchipelagoMW.client.APContext;
import ArchipelagoMW.saythespire.SayTheSpire;
import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDrawPileAction;
import com.megacrit.cardcrawl.actions.common.SpawnMonsterAction;
import com.megacrit.cardcrawl.actions.unique.IncreaseMaxHpAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.status.*;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.beyond.Nemesis;
import com.megacrit.cardcrawl.monsters.city.Byrd;
import com.megacrit.cardcrawl.monsters.city.ShelledParasite;
import com.megacrit.cardcrawl.monsters.city.SphericGuardian;
import com.megacrit.cardcrawl.monsters.exordium.*;
import com.megacrit.cardcrawl.potions.RegenPotion;
import com.megacrit.cardcrawl.powers.*;
import com.megacrit.cardcrawl.powers.watcher.*;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import io.github.archipelagomw.network.client.SetPacket;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TrapManager {
    private static final Random random = new Random();

    private final APClient client;
    private final MiscItemTracker itemTracker;
    private final Map<Long, Integer> trapsHandled = new ConcurrentHashMap<>();
    private final List<APItemID> trapIds = Arrays.asList(
            APItemID.GREMLIN_TRAP,
            APItemID.DEBUFF_TRAP,
            APItemID.STRONG_DEBUFF_TRAP,
            APItemID.KILLER_DEBUFF_TRAP,
            APItemID.BUFF_TRAP,
            APItemID.STRONG_BUFF_TRAP,
            APItemID.STATUS_CARD_TRAP,
            // Yeah this isn't a trap, but the logic is basically the same
            APItemID.COMBAT_BUFF
    );

    public TrapManager(APContext ctx) {
        client = ctx.getClient();
        itemTracker = ctx.getItemTracker();
    }

    private String getTrapStorageString()
    {
        return "spire_" + client.getTeam() + "_" + client.getSlot() + "_traps_handled";
    }

    public void initialize() {
        trapsHandled.clear();
        client.asyncDSGet(Collections.singletonList(getTrapStorageString()), e -> {
            Map<String, Number> result = (Map<String, Number>) e.data.get(getTrapStorageString());
            APClient.logger.info("Got Datastorage information {}", e.data);
            if(result == null)
            {
                return;
            }
            result.forEach((k,v) -> {
                try {
                    trapsHandled.put(APItemID.valueOf(k).value, v.intValue());
                }
                catch(Exception ex)
                {
                    APClient.logger.info("Failed to initialize trap storage", ex);
                }
            });
        });
    }

    public void checkAndApplyTraps(AbstractRoom room) {
        for (APItemID trapId : trapIds) {
            if(checkAndApplyTrap(room, trapId))
            {
                saveTrapsHandled();
                break;
            }
        }
    }

    private void saveTrapsHandled()
    {
        Map<String, Integer> sendMe = new HashMap<>();
        trapsHandled.forEach((k,v) -> {
            sendMe.put(APItemID.fromLong(k).name(), v);
        });
        SetPacket setPacket = new SetPacket(getTrapStorageString(), new HashMap<>());
        setPacket.addDataStorageOperation(SetPacket.Operation.REPLACE, sendMe);
        client.dataStorageSetFuture(setPacket);
    }

    private boolean checkAndApplyTrap(AbstractRoom room, APItemID trap) {
        int debuffCount = itemTracker.getCount(trap);
        int numberHandled = trapsHandled.getOrDefault(trap.value, 0);
        APClient.logger.info("For trap type {} received {} handled {}", trap, debuffCount, numberHandled);
        boolean sentTrap = false;
        if (debuffCount > numberHandled) {
            if (applyTrap(room, trap)) {
                trapsHandled.put(trap.value, numberHandled + 1);
                sentTrap = true;
            }
        }
        return sentTrap;
    }

    private boolean applyTrap(AbstractRoom room, APItemID trap) {
        APClient.logger.info("Current room status: {}", room.phase);
        switch (trap) {
            default:
                APClient.logger.error("Got unsupported trap {}", trap);
                return false;
            case GREMLIN_TRAP:
                SayTheSpire.sts.output("Adding a gremlin to the combat");
                applyGremlinTrap();
                break;
            case DEBUFF_TRAP:
                SayTheSpire.sts.output("Applying debuff trap");
                applyDebuffTrap();
                break;
            case STRONG_DEBUFF_TRAP:
                SayTheSpire.sts.output("Applying strong debuff trap");
                applyStrongDebuffTrap();
                break;
            case KILLER_DEBUFF_TRAP:
                SayTheSpire.sts.output("Applying killer debuff trap");
                applyKillerDebuffTrap();
                break;
            case BUFF_TRAP:
                SayTheSpire.sts.output("Applying enemy buff trap");
                applyBuffTrap(room);
                break;
            case STRONG_BUFF_TRAP:
                SayTheSpire.sts.output("Applying strong enemy buff trap");
                applyStrongBuffTrap(room);
                break;
            case STATUS_CARD_TRAP:
                SayTheSpire.sts.output("Adding status cards to the deck from a trap");
                applyStatusTrap();
                break;
            case COMBAT_BUFF:
                SayTheSpire.sts.output("Applying player combat buff");
                applyCombatBuff();
                break;
        }
        return true;
    }

    private void applyDebuffTrap() {
        AbstractPlayer p = AbstractDungeon.player;
        switch (random.nextInt(3)) {
            case 0:
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(p, p, new WeakPower(p, 1, false)));
                break;
            case 1:
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(p, p, new FrailPower(p, 1, false)));
                break;
            case 2:
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(p, p, new DrawReductionPower(p,1),1));
                break;
        }
    }

    private void applyStrongDebuffTrap() {
        AbstractPlayer p = AbstractDungeon.player;
        switch (random.nextInt(6)) {
            case 0:
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(p, p, new VulnerablePower(p, 1, false)));
                break;
            case 1:
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(p, p, new DexterityPower(p, -1)));
                break;
            case 2:
                if (p.chosenClass == AbstractPlayer.PlayerClass.DEFECT) {
                    AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(p, p, new FocusPower(p, -1)));
                } else {
                    AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(p, p, new StrengthPower(p, -1)));
                }
                break;
            case 3:
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(p, p, new GainStrengthPower(p, 3)));
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(p, p, new StrengthPower(p, -3)));
                break;
            case 4:
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(p, p, new ConstrictedPower(p, p, AbstractDungeon.actNum + 1)));
                break;
            case 5:
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(p, p, new ChokePower(p, 1)));
                break;
        }
    }

    private void applyKillerDebuffTrap() {
        AbstractPlayer p = AbstractDungeon.player;
        switch (random.nextInt(5)) {
            case 0:
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(p, p, new HexPower(p, 1)));
                break;
            case 1:
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(p, p, new WraithFormPower(p, -1)));
                break;
            case 2:
                if (p.chosenClass == AbstractPlayer.PlayerClass.DEFECT) {
                    AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(p, p, new BiasPower(p, -1)));
                } else {
                    AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(p, p, new EntanglePower(p)));
                }
                break;
            case 3:
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(p, p, new EnergyDownPower(p, 1, true)));
                break;
            case 4:
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(p, p, new ConfusionPower(p)));
                break;
        }
    }

    private void applyBuffTrap(AbstractRoom room) {
//        switch (random.nextInt(5)) {
            switch (4) {
            case 0:
                for (AbstractMonster m : room.monsters.monsters) {
                    AbstractDungeon.actionManager.addToBottom(new IncreaseMaxHpAction(m, 0.1F, true));
                }
                break;
            case 1:
                for (AbstractMonster m : room.monsters.monsters) {
                    AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(m, m, new StrengthPower(m, (AbstractDungeon.actNum + 3)/ 3)));
                }
                break;
            case 2:
                for (AbstractMonster m : room.monsters.monsters) {
                    AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(m, m, new MetallicizePower(m,  AbstractDungeon.actNum), AbstractDungeon.actNum));
                }

                break;
            case 3:
                for (AbstractMonster m : room.monsters.monsters) {
                    AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(m, m, new RegenerateMonsterPower(m, AbstractDungeon.actNum), AbstractDungeon.actNum));
                }
                break;
            case 4:
                for (AbstractMonster m : room.monsters.monsters) {
                    if(Byrd.ID.equals(m.id))
                    {
                        AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(m, m, new MetallicizePower(m,  AbstractDungeon.actNum), AbstractDungeon.actNum));
                    }
                    else
                    {
                        AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(m, m, new FlightPower(m, AbstractDungeon.actNum), AbstractDungeon.actNum));
                    }
                }
                break;
        }
    }

    private void applyStrongBuffTrap(AbstractRoom room)
    {
        switch (random.nextInt(5)) {
            case 0:
                for (AbstractMonster m : room.monsters.monsters) {
                    AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(m, m, new BufferPower(m, AbstractDungeon.actNum), AbstractDungeon.actNum));
                }
                break;
            case 1:
                for (AbstractMonster m : room.monsters.monsters) {
                    if(Nemesis.ID.equals(m.id))
                    {
                        AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(m, m, new ArtifactPower(m, 3), 3));
                    }
                    else {
                        IntangiblePower power = new IntangiblePower(m, 1);
                        ReflectionHacks.setPrivate(power, IntangiblePower.class, "justApplied", false);
                        AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(m, m, power));
                    }
                }
                break;
            case 2:
                for (AbstractMonster m : room.monsters.monsters) {
                    if(Objects.equals(m.id, ShelledParasite.ID) || Objects.equals(m.id, SphericGuardian.ID))
                    {
                        AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(m, m, new ThornsPower(m, AbstractDungeon.actNum), AbstractDungeon.actNum));
                    }
                    else {
                        AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(m, m, new PlatedArmorPower(m, AbstractDungeon.actNum*4), AbstractDungeon.actNum*5));
                    }
                }
                break;
            case 3:
                for (AbstractMonster m : room.monsters.monsters) {
                    AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(m, m, new ThornsPower(m, AbstractDungeon.actNum), AbstractDungeon.actNum));
                }
                break;
            case 4:
                for (AbstractMonster m : room.monsters.monsters) {
                    AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(m, m, new RitualPower(m, AbstractDungeon.actNum, false), AbstractDungeon.actNum));
                    AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(m, m, new StrengthPower(m, AbstractDungeon.actNum), AbstractDungeon.actNum));
                }
                break;
        }
    }

    private void applyStatusTrap()
    {
        AbstractPlayer p = AbstractDungeon.player;
        AbstractCard card;
        int amount = AbstractDungeon.actNum;
        switch (random.nextInt(5)) {
            default:
                // fall through
            case 0:
                card = new Burn();
                break;
            case 1:
                card = new Slimed();
                break;
            case 2:
                card = new Dazed();
                break;
            case 3:
                card = new Wound();
                break;
            case 4:
                card = new VoidCard();
                amount = 1;
                break;
        }
        AbstractDungeon.actionManager.addToBottom(new MakeTempCardInDrawPileAction(card, amount, true, true));
    }

    private void applyGremlinTrap()
    {
        AbstractMonster gremlin;
        switch (random.nextInt(5)) {
            default:
                // fall through
            case 0:
                gremlin = new GremlinThief(-550,0);
                break;
            case 1:
                gremlin = new GremlinFat(-550,0);
                break;
            case 2:
                gremlin = new GremlinWizard(-550,0);
                break;
            case 3:
                gremlin = new GremlinTsundere(-550,0);
                break;
            case 4:
                gremlin = new GremlinWarrior(-550,0);
                break;
        }
        AbstractDungeon.actionManager.addToTop(new SpawnMonsterAction(gremlin, false));
    }

    private void applyCombatBuff() {
        AbstractPlayer p = AbstractDungeon.player;
        switch (4) {
            case 0:
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(p, p, new StrengthPower(p, 1),1));
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(p, p, new LoseStrengthPower(p, 1),1));
                break;
            case 1:
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(p, p, new DexterityPower(p, 1),1));
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(p, p, new LoseDexterityPower(p, 1),1));
                break;
            case 2:
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(p,p, new NextTurnBlockPower(p, 4), 4));
                break;
            case 3:
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(p, p, new FlightPower(p, 1), 1));
                break;
            case 4:
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(p, p, new ThornsPower(p, 1), 1));
                break;
            case 5:
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(p, p, new RegenPower(p, 2), 2));
                break;
            case 6:
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(p, p, new DrawCardNextTurnPower(p, 1), 1));
                break;
            case 7:
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(p, p, new VigorPower(p, 4), 4));
                break;
            case 8:
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(p, p, new FlameBarrierPower(p, 2), 2));
                break;
            case 9:
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(p, p, new RagePower(p, 1), 1));
                break;
            case 10:
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(p, p, new WaveOfTheHandPower(p, 1), 1));
                break;
            case 11:
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(p, p, new BlurPower(p, 1), 1));
                break;
            case 12:
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(p, p, new OmegaPower(p, 1), 1));
                break;
            case 13:
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(p, p, new PlatedArmorPower(p, 2), 2));
                break;
        }
    }
}
