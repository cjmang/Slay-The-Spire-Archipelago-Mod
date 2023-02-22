package ArchipelagoMW.apEvents;

import ArchipelagoMW.patches.SavePatch;
import ArchipelagoMW.ui.mainMenu.NewMenuButtons;
import gg.archipelago.client.events.ArchipelagoEventListener;
import gg.archipelago.client.events.RetrievedEvent;

public class DataStorageGet {

    public static int loadRequestId;

    @ArchipelagoEventListener
    public void dataStorageGet(RetrievedEvent event) {
        if (event.getRequestID() == loadRequestId) {
            if (event.getString(SavePatch.AP_SAVE_STRING) != null && !event.getString(SavePatch.AP_SAVE_STRING).isEmpty()) {
                SavePatch.compressedSave = event.getString(SavePatch.AP_SAVE_STRING);
                NewMenuButtons.connectionInfoScreen.connectionPanel.resumeSave.show();
            } else {
                ConnectionResult.start();
            }
        }
    }
}

