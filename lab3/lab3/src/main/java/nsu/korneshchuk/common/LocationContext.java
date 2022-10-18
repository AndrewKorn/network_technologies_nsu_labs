package nsu.korneshchuk.common;

import java.util.HashMap;

public class LocationContext {
    private final HashMap<Integer, LocationInfo> map;
    private LocationInfo selectedLocation;

    public LocationContext(HashMap<Integer, LocationInfo> map) {
        this.map = map;
    }

    public void setSelectedLocation(LocationInfo selectedLocation) {
        this.selectedLocation = selectedLocation;
    }

    public HashMap<Integer, LocationInfo> getMap() {
        return map;
    }

    public LocationInfo getSelectedLocation() {
        return selectedLocation;
    }
}
