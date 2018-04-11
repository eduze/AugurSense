/*
 * <Paste your header here>
 */
package org.augur.sense.core.conditions;

import org.augur.sense.api.Condition;
import org.augur.sense.api.model.CameraGroup;
import org.augur.sense.api.model.Zone;
import org.augur.sense.api.Condition;
import org.augur.sense.api.model.CameraGroup;
import org.augur.sense.api.model.Zone;
import org.augur.sense.api.resources.Notification;
import org.augur.sense.api.resources.PersonSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Condition to check whether the zones have have exceeding number of people in it
 *
 * @author Imesha Sudasingha
 */
public class ZoneLimitCondition implements Condition {

    @Override
    public List<Notification> evaluate(CameraGroup cameraGroup, List<List<PersonSnapshot>> snapshots) {
        Map<Zone, List<List<PersonSnapshot>>> byZone = new HashMap<>();

        Map<Integer, Zone> zones = new HashMap<>();
        cameraGroup.getZones().forEach(zone -> zones.put(zone.getId(), zone));

        snapshots.forEach(snapshotList -> {
            if (snapshotList.size() > 0 && snapshotList.get(0).getInstanceZone() != null) {
                Zone z = snapshotList.get(0).getInstanceZone();
                byZone.computeIfAbsent(zones.get(z.getId()), key -> new ArrayList<>())
                        .add(snapshotList);
            }
        });

        List<Notification> notifications = new ArrayList<>();
        byZone.forEach((zone, snapshotLists) -> {
            if (zone.getZoneLimit() < snapshotLists.size()) {
                String title = String.format("'%s' is overcrowded", zone.getZoneName());
                String message = String.format("Zone '%s' has %d people in it", zone.getZoneName(), snapshotLists.size());
                long timestamp = snapshotLists.get(0).get(0).getTimestamp();
                Notification notification = new Notification(title, message, timestamp);
                notifications.add(notification);
            }
        });

        return notifications;
    }
}
