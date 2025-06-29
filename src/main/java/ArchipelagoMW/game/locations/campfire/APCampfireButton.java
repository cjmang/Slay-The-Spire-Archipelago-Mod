package ArchipelagoMW.game.locations.campfire;

import ArchipelagoMW.client.APContext;
import ArchipelagoMW.game.locations.LocationTracker;
import ArchipelagoMW.game.ui.APTextures;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.ui.campfire.AbstractCampfireOption;
import com.megacrit.cardcrawl.vfx.campfire.CampfireRecallEffect;
import dev.koifysh.archipelago.parts.NetworkItem;

public class APCampfireButton  extends AbstractCampfireOption {

    private final long locationId;
    public APCampfireButton(long locationId, NetworkItem scoutedItem)
    {
        this.locationId = locationId;
        if(scoutedItem != null) {
            String[] itemNameTokens = scoutedItem.itemName.split("\\s+");
            StringBuilder sb = new StringBuilder(scoutedItem.playerName).append("'s \n ");
            int count = 0;
            for(String token : itemNameTokens)
            {
                count += token.length();
                sb.append(token);
                if(count > 16)
                {
                    sb.append("\n");
                    count = 0;
                }
                else
                {
                    sb.append(' ');
                }
            }
            this.label = sb.toString();
        }
        else
        {
            this.label = "What is this?";
        }
        this.img = APTextures.AP_CAMPFIRE;

        if(scoutedItem == null)
        {
            this.description = "???";
        }
        else if((dev.koifysh.archipelago.flags.NetworkItem.TRAP & scoutedItem.flags) > 0)
        {
            this.description = "Seems important?";
        }
        else if((dev.koifysh.archipelago.flags.NetworkItem.ADVANCEMENT & scoutedItem.flags) > 0)
        {
            this.description = "Seems important!";
        }
        else if((dev.koifysh.archipelago.flags.NetworkItem.USEFUL & scoutedItem.flags) > 0)
        {
            this.description = "Might be helpful.";
        }
        else
        {
            this.description = "Who left this here?";
        }

    }

    @Override
    public void useOption() {
        APContext.getContext().getLocationTracker().sendCampfireCheck(locationId);
        AbstractDungeon.effectList.add(new APCampfireEffect());
    }
}
