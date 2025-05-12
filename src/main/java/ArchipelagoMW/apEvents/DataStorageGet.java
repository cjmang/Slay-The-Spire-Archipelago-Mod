package ArchipelagoMW.apEvents;

import ArchipelagoMW.APClient;
import ArchipelagoMW.patches.SavePatch;
import ArchipelagoMW.ui.connection.ArchipelagoPreGameScreen;
import ArchipelagoMW.ui.mainMenu.ArchipelagoMainMenuButton;
import dev.koifysh.archipelago.events.ArchipelagoEventListener;
import dev.koifysh.archipelago.events.RetrievedEvent;

import java.util.Collection;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;

public class DataStorageGet {

    private static final TransferQueue<RetrievedEvent> transferQueue = new LinkedTransferQueue<>();

    public static int loadRequestId;
    private static volatile int transferId = -1;

    @ArchipelagoEventListener
    public void dataStorageGet(RetrievedEvent event) {
        if (event.getRequestID() == loadRequestId) {
            if (event.getString(SavePatch.AP_SAVE_STRING) != null && !event.getString(SavePatch.AP_SAVE_STRING).isEmpty()) {
                SavePatch.compressedSave = event.getString(SavePatch.AP_SAVE_STRING);
                SavePatch.savedChar = event.getString(SavePatch.AP_SAVE_CHAR);
                APClient.logger.info("Got saved character {}", SavePatch.savedChar);
                ArchipelagoMainMenuButton.archipelagoPreGameScreen.connectionPanel.resumeSave.show();
            } else {
                ArchipelagoMainMenuButton.archipelagoPreGameScreen.screen = ArchipelagoPreGameScreen.APScreen.charSelect;
            }
        }
        else if (event.getRequestID() == transferId)
        {
            transferQueue.tryTransfer(event);
            transferId = -1;
        }
    }

    public static RetrievedEvent getData(Collection<String> keys)
    {
        if(transferId >= 0)
        {
            return null;
        }
        transferId = APClient.apClient.dataStorageGet(keys);
        try
        {
            return transferQueue.poll(15, TimeUnit.SECONDS);
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
            return null;
        }
    }
}

