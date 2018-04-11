package org.eduze.fyp.core;

import org.eduze.fyp.core.resources.PersonLocation;
import org.eduze.fyp.api.Constants;
import org.eduze.fyp.api.model.Zone;

import java.awt.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ZoneMapper {

    private List<Zone> zones;
    private Zone world;
    private Map<Zone, Polygon> zonePolygons;

    private boolean initialized = false;
    private int zonePersistentScanCount = 5;
    private int zonePersistentThreshold = 4;

    public ZoneMapper(List<Zone> zones, int zonePersistentScanCount, int zonePersistentThreshold) {
        this.zones = zones;
        this.zonePersistentScanCount = zonePersistentScanCount;
        this.zonePersistentThreshold = zonePersistentThreshold;
        initialize();
    }

    private void initialize() {
        if (initialized) {
            return;
        }

        if (zones == null) {
            return;
        }

        this.zonePolygons = new HashMap<>();
        zones.forEach(zone -> {
            if (Constants.ZONE_NAME_WORLD.equals(zone.getZoneName())) {
                world = zone;
            } else {
                List<Integer> xCoordinates = zone.getXCoordinates();
                List<Integer> yCoordinates = zone.getYCoordinates();
                Polygon p = new Polygon(xCoordinates.stream().mapToInt(i -> i).toArray(),
                        yCoordinates.stream().mapToInt(i -> i).toArray(),
                        xCoordinates.size());
                zonePolygons.put(zone, p);
            }
        });

        initialized = true;
    }

    public void processPersonLocations(List<PersonLocation> personLocations) {
        personLocations.forEach(personLocation -> {
            //TODO: identify rest of the world zone here! Its not contained in any of the polygons
            final Zone[] selectedZone = {null};
            final Polygon[] selectedPolygon = {null};
            zonePolygons.forEach((zone, polygon) -> {
                if (polygon.contains(new Point((int) personLocation.getSnapshot().getX(), (int) personLocation.getSnapshot().getY()))) {
                    selectedZone[0] = zone;
                    selectedPolygon[0] = polygon;
                }
            });

            if (selectedZone[0] == null) {
                // identify rest of the world zone here! Its not contained in any of the polygons
                selectedZone[0] = world;
            }

            if (selectedZone[0] != null) {
                Zone zone = selectedZone[0];
                personLocation.getSnapshot().setInstanceZone(zone);
                // Obtain majority of last 5 zones
                Map<Zone, Integer> polularityMap = new LinkedHashMap<>();
                personLocation.getSnapshots().stream()
                        .limit(zonePersistentScanCount)
                        .forEach(personSnapshot -> {
                                    if (polularityMap.containsKey(personSnapshot.getInstanceZone())) {
                                        polularityMap.put(personSnapshot.getInstanceZone(), polularityMap.get(personSnapshot.getInstanceZone()) + 1);
                                    } else {
                                        polularityMap.put(personSnapshot.getInstanceZone(), 1);
                                    }
                                }
                        );

                final int[] mostPopularCount = {-1};
                final Zone[] mostPopularZone = {null};

                polularityMap.forEach((zone1, p) -> {
                    if (p > mostPopularCount[0]) {
                        mostPopularCount[0] = p;
                        mostPopularZone[0] = zone1;
                    }
                });

                if (personLocation.getSnapshot().getPersistantZone() == null) {
                    personLocation.getSnapshot().setPersistantZone(mostPopularZone[0]);
                    //Report transition
                } else {
                    if (mostPopularCount[0] > zonePersistentThreshold) {
                        personLocation.getSnapshot().setPersistantZone(mostPopularZone[0]);
                    }
                }
            }
        });
    }

    public int getZonePersistentScanCount() {
        return zonePersistentScanCount;
    }

    public void setZonePersistentScanCount(int zonePersistentScanCount) {
        this.zonePersistentScanCount = zonePersistentScanCount;
    }

    public int getZonePersistentThreshold() {
        return zonePersistentThreshold;
    }

    public void setZonePersistentThreshold(int zonePersistentThreshold) {
        this.zonePersistentThreshold = zonePersistentThreshold;
    }

    public List<Zone> getZones() {
        return zones;
    }
}
