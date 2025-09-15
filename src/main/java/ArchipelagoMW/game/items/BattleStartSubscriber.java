package ArchipelagoMW.game.items;

import ArchipelagoMW.client.APContext;
import basemod.interfaces.OnStartBattleSubscriber;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

public class BattleStartSubscriber implements OnStartBattleSubscriber {
    @Override
    public void receiveOnBattleStart(AbstractRoom abstractRoom) {
        APContext.getContext().getTrapManager().checkAndApplyTraps(abstractRoom);
    }
}
