/*
 * Copyright 2017 Eduze
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to
 * whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.eduze.fyp.api.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PersonLocation {

    private static final int HISTORY_SIZE = 20;

    private final Set<Integer> ids = new HashSet<>();
    private Map<Integer, Coordinate> contributingCoordinates = new HashMap<>();
    private LinkedList<PersonSnapshot> snapshots = new LinkedList<>();

    public PersonLocation() {
    }

    public PersonLocation(int id) {
        ids.add(id);
    }

    public void addPoint(int cameraId, Coordinate coordinate) {
        Coordinate p = contributingCoordinates.get(cameraId);
        if (p == null) {
            contributingCoordinates.put(cameraId, coordinate);
            updateSnapshot(coordinate);
        } else if (p.getTimestamp() < coordinate.getTimestamp()) {
            contributingCoordinates.put(cameraId, coordinate);
            updateSnapshot(coordinate);
        }
    }

    private void updateSnapshot(Coordinate original) {
        long timestamp;
        if (snapshots.isEmpty()) {
            timestamp = original.getTimestamp();
        } else {
            // TODO: 9/22/17 Should we ignore this scenario?
            PersonSnapshot snapshot = snapshots.getFirst();
            timestamp = snapshot.getTimestamp() < original.getTimestamp() ? original.getTimestamp() : snapshot.getTimestamp();
        }

        Coordinate coordinate = contributingCoordinates.values()
                .stream()
                .reduce(new Coordinate(0, 0, timestamp),
                        (p1, p2) -> new Coordinate(p1.getX() + p2.getX(), p1.getY() + p2.getY(), timestamp));
        coordinate.setX(coordinate.getX() / contributingCoordinates.size());
        coordinate.setY(coordinate.getY() / contributingCoordinates.size());

        if (snapshots.size() == HISTORY_SIZE) {
            snapshots.removeLast();
        }

        // TODO: 9/21/17 All have the same reference to IDs

        if(snapshots.size() > 0){
            snapshots.addFirst(new PersonSnapshot(ids, coordinate,null,snapshots.get(0).getPersistantZone(),snapshots.get(0).getPersistantZone()));
        }else{
            snapshots.addFirst(new PersonSnapshot(ids, coordinate,null,null, null));
        }

    }

    public void addId(int id) {
        ids.add(id);
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
