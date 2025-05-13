package ArchipelagoMW.game.ui.Components;

import ArchipelagoMW.game.ui.APTextures;
import ArchipelagoMW.game.locations.LocationTracker;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.TipHelper;
import com.megacrit.cardcrawl.rewards.chests.AbstractChest;
import dev.koifysh.archipelago.parts.NetworkItem;

public class APChest extends AbstractChest {

    private final NetworkItem item;

    public APChest() {
        this.img = APTextures.AP_CHEST;
        this.openedImg = APTextures.AP_CHEST;
        this.hb = new Hitbox(256.0F * Settings.scale, 256.0F * Settings.scale);
        this.hb.move(CHEST_LOC_X, CHEST_LOC_Y - 100.0F * Settings.scale);
        this.item = LocationTracker.sendBossRelic(AbstractDungeon.actNum);
        AbstractDungeon.overlayMenu.proceedButton.setLabel(TEXT[0]);
    }

    public void open(boolean bossChest) {
        CardCrawlGame.sound.play("CHEST_OPEN");
    }

    @Override
    public void render(SpriteBatch sb) {
        super.render(sb);
        if (!this.isOpen) {
            TipHelper.renderGenericTip(CHEST_LOC_X - hb.width / 2, CHEST_LOC_Y + hb.height / 2f, item.itemName, item.playerName);
        }
    }

}

