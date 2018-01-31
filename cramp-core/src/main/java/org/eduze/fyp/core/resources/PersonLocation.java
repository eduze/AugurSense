/*
 * Copyright 2017 Eduze
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

package org.eduze.fyp.core.resources;

import org.eduze.fyp.api.resources.Coordinate;
import org.eduze.fyp.api.resources.PersonSnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class PersonLocation {

    private static final int HISTORY_SIZE = 20;

    private int currentTrackSegmentIndex = 0;

    private final Set<Integer> ids = new HashSet<>();
    private Map<Integer, Coordinate> contributingCoordinates = new HashMap<>();
    private LinkedList<PersonSnapshot> snapshots = new LinkedList<>();

    public PersonLocation() {
    }

    public PersonLocation(int id) {
        ids.add(id);
    }

    public PersonSnapshot addPoint(int cameraId, Coordinate coordinate) {
        Coordinate p = contributingCoordinates.get(cameraId);
        if (p == null) {
            contributingCoordinates.put(cameraId, coordinate);
            return updateSnapshot(coordinate.getTimestamp());
        } else if (p.getTimestamp() < coordinate.getTimestamp()) {
            contributingCoordinates.put(cameraId, coordinate);
            return updateSnapshot(coordinate.getTimestamp());
        }
        return null;
    }

    private synchronized PersonSnapshot updateSnapshot(long timestamp) {
        // Remove all the entries which are older than this timestamp. This will help to keep us up to date.
        // Only problem is when a camera responds after the time interval for asking the next frame
        contributingCoordinates.entrySet().removeIf(next -> next.getValue().getTimestamp() < timestamp);

        List<Coordinate> coordinates = contributingCoordinates.values()
                .stream()
                .filter(c -> c.getTimestamp() == timestamp)
                .collect(Collectors.toList());

        double x = 0, y = 0, sitProbability = 0, standProbability = 0, headDirectionX = 0, headDirectionY = 0;
        for (Coordinate c : coordinates) {
            x += c.getX();
            y += c.getY();
            sitProbability += c.getSitProbability();
            standProbability += c.getStandProbability();
            headDirectionX = c.getHeadDirectionX();
            headDirectionY = c.getHeadDirectionY();
        }
        x = x / coordinates.size();
        y = y / coordinates.size();
        sitProbability = sitProbability / coordinates.size();
        standProbability = standProbability / coordinates.size();
        headDirectionX = headDirectionX / coordinates.size();
        headDirectionY = headDirectionY / coordinates.size();

        Coordinate coordinate = new Coordinate(x, y, timestamp, sitProbability, standProbability, headDirectionX, headDirectionY);

        double headDirectionRadius = Math.sqrt(Math.pow(coordinate.getHeadDirectionX(), 2) + Math.pow(coordinate.getHeadDirectionY(), 2));
        if (headDirectionRadius > 0) {
            coordinate.setHeadDirectionX(coordinate.getHeadDirectionX() / headDirectionRadius);
            coordinate.setHeadDirectionY(coordinate.getHeadDirectionY() / headDirectionRadius);
        }

        // TODO: 9/21/17 All have the same reference to IDs
        PersonSnapshot result = null;
        if (snapshots.size() > 0) {
            result = new PersonSnapshot(ids, coordinate, null, snapshots.get(0).getPersistantZone(),
                    snapshots.get(0).getPersistantZone(), currentTrackSegmentIndex);
        } else {
            result = new PersonSnapshot(ids, coordinate, null, null, null, currentTrackSegmentIndex);
        }

        // Another camera may have sent a person location earlier, which should now be replaced
        if (snapshots.getFirst().getTimestamp() == timestamp) {
            snapshots.removeFirst();
        }
        snapshots.addFirst(result);

        // Finally sort by timestamp in the decreasing order
        snapshots.sort(Comparator.comparingLong(PersonSnapshot::getTimestamp).reversed());
        return result;
    }

    public void incrementTrackSegmentIndex() {
        this.currentTrackSegmentIndex++;
        if (this.snapshots.size() > 0) {
            snapshots.get(0).setTrackSegmentIndex(this.currentTrackSegmentIndex);
        }
    }

    public void addId(int id) {
        ids.add(id);
    }

    public int getCurrentTrackSegmentIndex() {
        return currentTrackSegmentIndex;
    }

    public void setCurrentTrackSegmentIndex(int currentTrackSegmentIndex) {
        this.currentTrackSegmentIndex = currentTrackSegmentIndex;
    }

    public Set<Integer> getIds() {
        return ids;
    }

    public Map<Integer, Coordinate> getContributingCoordinates() {
        return contributingCoordinates;
    }

    public List<PersonSnapshot> getSnapshots() {
        return new ArrayList<>(snapshots);
    }

    public PersonSnapshot getSnapshot() {
        if (snapshots.size() > 0) {
            return snapshots.getFirst();
        }

        return null;
    }
}