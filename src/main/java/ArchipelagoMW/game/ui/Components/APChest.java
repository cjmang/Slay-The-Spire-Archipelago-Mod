package ArchipelagoMW.game.ui.Components;

import ArchipelagoMW.client.APContext;
import ArchipelagoMW.game.ui.APTextures;
import ArchipelagoMW.saythespire.SayTheSpire;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.TipHelper;
import com.megacrit.cardcrawl.rewards.chests.AbstractChest;
import io.github.archipelagomw.parts.NetworkItem;

public class APChest extends AbstractChest {

    private final NetworkItem item;

    public APChest() {
        this.img = APTextures.AP_CHEST;
        this.openedImg = APTextures.AP_CHEST;
        this.hb = new Hitbox(256.0F * Settings.scale, 256.0F * Settings.scale);
        this.hb.move(CHEST_LOC_X, CHEST_LOC_Y - 100.0F * Settings.scale);
        this.item = APContext.getContext().getLocationTracker().sendBossRelic(AbstractDungeon.actNum);
        AbstractDungeon.overlayMenu.proceedButton.setLabel(TEXT[0]);
        SayTheSpire.sts.output(item.itemName + " sent to " + item.playerName);
    }

    public void open(boolean bossChest) {
        CardCrawlGame.sound.play("CHEST_OPEN");
    }

    @Override
    public void update()
    {
        super.update();
        if(hb.justHovered)
        {
            SayTheSpire.sts.output(item.itemName + " sent to " + item.playerName);
        }
    }

    @Override
    public void render(SpriteBatch sb) {
        super.render(sb);
        if (!this.isOpen) {
            TipHelper.renderGenericTip(CHEST_LOC_X - hb.width / 2, CHEST_LOC_Y + hb.height / 2f, item.itemName, item.playerName);
        }
    }

}

