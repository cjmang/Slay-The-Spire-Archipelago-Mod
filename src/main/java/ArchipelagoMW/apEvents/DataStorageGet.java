package ArchipelagoMW.apEvents;

import ArchipelagoMW.patches.SavePatch;
import ArchipelagoMW.ui.connection.ArchipelagoPreGameScreen;
import ArchipelagoMW.ui.mainMenu.ArchipelagoMainMenuButton;
import dev.koifysh.archipelago.events.ArchipelagoEventListener;
import dev.koifysh.archipelago.events.RetrievedEvent;

public class DataStorageGet {

    public static int loadRequestId;

    @ArchipelagoEventListener
    public void dataStorageGet(RetrievedEvent event) {
        if (event.getRequestID() == loadRequestId) {
            if (event.getString(SavePatch.AP_SAVE_STRING) != null && !event.getString(SavePatch.AP_SAVE_STRING).isEmpty()) {
                SavePatch.compressedSave = event.getString(SavePatch.AP_SAVE_STRING);
                ArchipelagoMainMenuButton.archipelagoPreGameScreen.connectionPanel.resumeSave.show();
            } else {
                ArchipelagoMainMenuButton.archipelagoPreGameScreen.screen = ArchipelagoPreGameScreen.APScreen.charSelect;
            }
        }
    }
}

