package ArchipelagoMW.apEvents;

import ArchipelagoMW.patches.SavePatch;
import ArchipelagoMW.ui.mainMenu.NewMenuButtons;
import gg.archipelago.client.events.ArchipelagoEventListener;
import gg.archipelago.client.events.RetrievedEvent;

public class DataStorageGet {

    @ArchipelagoEventListener
    public void dataStorageGet(RetrievedEvent event) {
        if (event.getString(SavePatch.AP_SAVE_STRING) != null || !event.getString(SavePatch.AP_SAVE_STRING).isEmpty()) {
            SavePatch.compressedSave = event.getString(SavePatch.AP_SAVE_STRING);
            NewMenuButtons.connectionInfoScreen.addressPanel.resumeSave.show();
        } else {
            ConnectionResult.Connect();
        }
    }
}
