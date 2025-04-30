package ArchipelagoMW.apEvents;

import ArchipelagoMW.LocationTracker;
import dev.koifysh.archipelago.events.ArchipelagoEventListener;
import dev.koifysh.archipelago.events.LocationInfoEvent;

public class LocationInfo {
    @ArchipelagoEventListener
    public void onLocationInfo(LocationInfoEvent event) {
        LocationTracker.addToScoutedLocations(event.locations);
    }
}
