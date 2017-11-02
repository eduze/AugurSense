package org.eduze.fyp.impl;

import org.eduze.fyp.api.resources.PersonLocation;
import org.eduze.fyp.impl.db.dao.ZoneDAO;
import org.eduze.fyp.impl.db.model.Zone;

import java.awt.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ZoneMapper {
    private ZoneDAO zoneDAO = null;
    private List<Zone> zonesList = null;
    private HashMap<Zone, Polygon> zonePolygons = null;

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

    public ZoneMapper(ZoneDAO zoneDAO){
        this.zoneDAO = zoneDAO;
        this.zonesList = zoneDAO.list();
        this.zonePolygons = new LinkedHashMap<>();
        for(Zone zone : zonesList){
            int[] xCoordinates = zone.getXCoordinates();
            int[] yCoordinates = zone.getYCoordinates();
            Polygon p = new Polygon(xCoordinates,yCoordinates,xCoordinates.length);
            zonePolygons.put(zone,p);
        }
    }

    public List<Zone> getZonesList() {
        return zonesList;
    }


    public void processPersonLocations(List<PersonLocation> personLocations) {
        personLocations.forEach(personLocation -> {
            zonePolygons.forEach((zone,polygon) -> {
                if(polygon.contains(new Point((int)personLocation.getSnapshot().getX(),(int)personLocation.getSnapshot().getY()))){
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
        });
    }
}
