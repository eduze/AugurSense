package org.eduze.fyp.core;

import org.eduze.fyp.api.ConfigurationManager;
import org.eduze.fyp.api.listeners.ConfigurationListener;
import org.eduze.fyp.core.resources.PersonLocation;
import org.eduze.fyp.core.db.dao.ZoneDAO;
import org.eduze.fyp.api.model.Zone;

import java.awt.*;
import java.util.*;
import java.util.List;

public class ZoneMapper implements ConfigurationListener {
    private ZoneDAO zoneDAO = null;
    private List<Zone> zonesList = null;
    private HashMap<Zone, Polygon> zonePolygons = null;

    private boolean initialized = false;
    private int zonePersistantScanCount = 5;

    private int zonePersistantThreshold = 4;

    public int getZonePersistantScanCount() {
        return zonePersistantScanCount;
    }

    public void setZonePersistantScanCount(int zonePersistantScanCount) {
        this.zonePersistantScanCount = zonePersistantScanCount;
    }

    public int getZonePersistantThreshold() {
        return zonePersistantThreshold;
    }

    public void setZonePersistantThreshold(int zonePersistantThreshold) {
        this.zonePersistantThreshold = zonePersistantThreshold;
    }

    public ZoneMapper(ConfigurationManager configurationManager){
        this.zonesList = configurationManager.getZones();
        initialize();
    }

    private void initialize(){
        if(initialized)
            return;
        if(zonesList == null)
            return;

        this.zonePolygons = new LinkedHashMap<>();
        for(Zone zone : zonesList){
            int[] xCoordinates = zone.getXCoordinates();
            int[] yCoordinates = zone.getYCoordinates();
            Polygon p = new Polygon(xCoordinates,yCoordinates,xCoordinates.length);
            zonePolygons.put(zone,p);
        }

        initialized = true;
    }

    public List<Zone> getZonesList() {
        return zonesList;
    }


    public void processPersonLocations(List<PersonLocation> personLocations) {
        initialize();

        personLocations.forEach(personLocation -> {
            //TODO: identify rest of the world zone here! Its not contained in any of the polygons
            final Zone[] selectedZone = {null};
            final Polygon[] selectedPolygon = {null};
            zonePolygons.forEach((zone,polygon) -> {
                if(polygon.contains(new Point((int)personLocation.getSnapshot().getX(),(int)personLocation.getSnapshot().getY()))){
                    selectedZone[0] = zone;
                    selectedPolygon[0] = polygon;

                }
            });

            if(selectedZone[0] == null)
            {
                // identify rest of the world zone here! Its not contained in any of the polygons
               zonesList.stream().filter(zone -> zone.getId() == 0).findFirst().ifPresent((c)->selectedZone[0]=c);
            }

            if(selectedZone[0] != null)
            {
                Zone zone = selectedZone[0];
                personLocation.getSnapshot().setInstanceZone(zone);
                // Obtain majority of last 5 zones
                Map<Zone,Integer> polularityMap = new LinkedHashMap<>();
                personLocation.getSnapshots().stream().limit(zonePersistantScanCount).forEach(personSnapshot -> {
                            if (polularityMap.containsKey(personSnapshot.getInstanceZone())) {
                                polularityMap.put(personSnapshot.getInstanceZone(), polularityMap.get(personSnapshot.getInstanceZone()) + 1);
                            }
                            else{
                                polularityMap.put(personSnapshot.getInstanceZone(),1);
                            }
                        }
                );

                final int[] mostPopularCount = {-1};
                final Zone[] mostPopularZone = {null};

                polularityMap.forEach((zone1, p) -> {
                    if(p > mostPopularCount[0]){
                        mostPopularCount[0] = p;
                        mostPopularZone[0] = zone1;
                    }
                });

                if(personLocation.getSnapshot().getPersistantZone() == null){
                    personLocation.getSnapshot().setPersistantZone(mostPopularZone[0]);
                    //Report transition
                }
                else{
                    if(mostPopularCount[0] > zonePersistantThreshold){
                        personLocation.getSnapshot().setPersistantZone(mostPopularZone[0]);
                    }
                }
            }
        });
    }

    @Override
    public void configurationChanged(ConfigurationManager configurationManager) {
        this.zonesList = configurationManager.getZones();
    }
}
