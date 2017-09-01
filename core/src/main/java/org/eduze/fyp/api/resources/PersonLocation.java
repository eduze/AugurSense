/*
 * Copyright to Eduze@UoM 2017
 */

package org.eduze.fyp.api.resources;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PersonLocation {

    private final Set<Integer> ids = new HashSet<>();
    private Map<Integer, Coordinate> contributingPoints = new HashMap<>();
    private volatile Coordinate snapshot = new Coordinate();

    public PersonLocation() {
    }

    public PersonLocation(int id) {
        ids.add(id);
    }

    public synchronized void addPoint(int cameraId, Coordinate coordinate) {
        Coordinate p = contributingPoints.get(cameraId);
        if (p == null) {
            contributingPoints.put(cameraId, coordinate);
            updateSnapshot(coordinate);
        } else if (p.getTimestamp() < coordinate.getTimestamp()) {
            contributingPoints.put(cameraId, coordinate);
            updateSnapshot(coordinate);
        }
    }

    private synchronized void updateSnapshot(Coordinate original) {
        long timestamp = snapshot.getTimestamp() < original.getTimestamp() ?
                original.getTimestamp() : snapshot.getTimestamp();

        Coordinate snapshot = contributingPoints.values().stream()
                .reduce(new Coordinate(0, 0, timestamp),
                        (p1, p2) -> new Coordinate(p1.getX() + p2.getX(), p1.getY() + p2.getY(), timestamp));
        snapshot.setX(snapshot.getX() / contributingPoints.size());
        snapshot.setY(snapshot.getY() / contributingPoints.size());

        this.snapshot = snapshot;
    }

    public void addId(int id) {
        ids.add(id);
    }

    public Set<Integer> getIds() {
        return ids;
    }

    public Map<Integer, Coordinate> getContributingPoints() {
        return contributingPoints;
    }

    public Coordinate getSnapshot() {
        return snapshot;
    }
}
