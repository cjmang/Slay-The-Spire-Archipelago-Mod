package ArchipelagoMW.client;

import ArchipelagoMW.client.config.SlotData;
import ArchipelagoMW.game.CharacterManager;
import ArchipelagoMW.game.ShopManager;
import ArchipelagoMW.game.items.MiscItemTracker;
import ArchipelagoMW.game.locations.LocationTracker;
import ArchipelagoMW.game.save.SaveManager;
import io.github.archipelagomw.ItemManager;
import io.github.archipelagomw.LocationManager;

public class APContext {

    private static final APContext INSTANCE = new APContext();

    public static APContext getContext()
    {
        return INSTANCE;
    }

    private APClient client;
    private CharacterManager characterManager;
    private MiscItemTracker itemTracker;
    private LocationTracker locationTracker;
    private ShopManager shopManager;
    private SaveManager saveManager;

    public SaveManager getSaveManager() {
        return saveManager;
    }

    public APClient getClient() {
        return client;
    }

    public void setClient(APClient client) {
        if(client == this.client)
        {
            return;
        }
        this.client = client;
        locationTracker = new LocationTracker();
        characterManager = new CharacterManager(this);
        itemTracker = new MiscItemTracker(characterManager);
        shopManager = new ShopManager(this);
        saveManager = new SaveManager(getContext());
    }

    public ItemManager getItemManager()
    {
        return client.getItemManager();
    }

    public CharacterManager getCharacterManager() {
        return characterManager;
    }

    public MiscItemTracker getItemTracker() {
        return itemTracker;
    }

    public LocationTracker getLocationTracker() {
        return locationTracker;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    public LocationManager getLocationManager()
    {
        return client.getLocationManager();
    }

    public int getTeam()
    {
        return client.getTeam();
    }

    public int getSlot()
    {
        return client.getSlot();
    }

    public SlotData getSlotData()
    {
        return client.getSlotData();
    }
}
