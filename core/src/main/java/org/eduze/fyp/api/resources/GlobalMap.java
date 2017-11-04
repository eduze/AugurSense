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

package org.eduze.fyp.api.resources;

import org.apache.commons.math3.util.Pair;
import org.eduze.fyp.Constants;
import org.eduze.fyp.impl.PhotoMapper;
import org.eduze.fyp.impl.ZoneMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents historical data related to the global map including people in the map and etc.
 *
 * @author Imesha Sudasingha
 */
public class GlobalMap {

    private static final Logger logger = LoggerFactory.getLogger(GlobalMap.class);

    private List<PersonLocation> personLocations = new ArrayList<>();
    private volatile int id = 0;

    private PhotoMapper photoMapper = null;
    private ZoneMapper zoneMapper = null;

    public void setZoneMapper(ZoneMapper zoneMapper) {
        this.zoneMapper = zoneMapper;
    }

    public ZoneMapper getZoneMapper() {
        return zoneMapper;
    }

    public synchronized List<List<PersonSnapshot>> getSnapshot() {
        return personLocations.stream()
                .map(PersonLocation::getSnapshots)
                .collect(Collectors.toList());
    }

    public synchronized void update(LocalMap localMap) {
        logger.debug("Updating global map with {} coordinates in local map", localMap.getPersonCoordinates().size());

        List<LocationPair> tuples = new ArrayList<>();
        localMap.getPersonCoordinates().forEach(personCoordinate -> {
            tuples.add(new LocationPair(personCoordinate, null));
            personLocations.forEach(personLocation -> {
                tuples.add(new LocationPair(personCoordinate, personLocation));
            });
        });

        Collections.sort(tuples);
        Set<PersonCoordinate> usedKNew = new HashSet<>();
        Set<PersonLocation> usedVPL = new HashSet<>();
        Set<PersonLocation> newPeople = new HashSet<>();
        tuples.forEach(pair -> {
            if (!usedKNew.contains(pair.getKey()) && !usedVPL.contains(pair.getValue()) && pair.distance() < Constants.DISTANCE_THRESHOLD) {
                if (pair.getKey().getImage() != null) {
                    logger.debug("Found an image for person {}", pair.getValue().getIds());
                    // TODO: 10/3/17 Do the re-id part here
                    photoMapper.addSnapshot(pair.getKey(),pair.getValue().getIds());
                }
                usedKNew.add(pair.getKey());
                usedVPL.add(pair.getValue());
                pair.getValue().addPoint(localMap.getCameraId(), pair.getKey().toCoordinate());
            } else if (!usedKNew.contains(pair.getKey()) && pair.getValue() == null) {
                if (pair.getKey().getImage() != null) {
                    logger.debug("Found a new person with image");
                    photoMapper.addSnapshot(pair.getKey(),pair.getValue().getIds());
                    // TODO: 10/3/17 DO the re-id part here
                }
                int id = this.id++;
                PersonLocation newPL = new PersonLocation(id);
                newPL.addPoint(localMap.getCameraId(), pair.getKey().toCoordinate());
                newPeople.add(newPL);
                usedKNew.add(pair.getKey());
            }
        });

        personLocations.addAll(newPeople);

        if(this.zoneMapper != null){
            zoneMapper.processPersonLocations(personLocations);
        }
    }

    public synchronized void refresh(long minTimestamp) {
        logger.debug("Refreshing global map with minimum timestamp {}", minTimestamp);

        Iterator<PersonLocation> iterator = personLocations.iterator();
        while (iterator.hasNext()) {
            PersonLocation personLocation = iterator.next();
            Map<Integer, Coordinate> coordinates = personLocation.getContributingCoordinates();
            List<Integer> toRemove = coordinates.keySet().stream()
                    .filter(id -> coordinates.get(id).getTimestamp() < minTimestamp)
                    .collect(Collectors.toList());
            toRemove.forEach(coordinates::remove);

            if (personLocation.getContributingCoordinates().size() == 0) {
                logger.debug("Removing outdated person location {}", personLocation);

                iterator.remove();
            }
        }
    }

    public PhotoMapper getPhotoMapper() {
        return photoMapper;
    }

    public void setPhotoMapper(PhotoMapper photoMapper) {
        this.photoMapper = photoMapper;
    }

    private class LocationPair extends Pair<PersonCoordinate, PersonLocation> implements Comparable<LocationPair> {
        public LocationPair(PersonCoordinate personCoordinate, PersonLocation personLocation) {
            super(personCoordinate, personLocation);
            if (personCoordinate == null && personLocation == null) {
                throw new IllegalArgumentException("Both points cannot be null");
            }
        }

        public LocationPair(Pair<? extends PersonCoordinate, ? extends PersonLocation> entry) {
            this(entry.getKey(), entry.getValue());
        }

        public double distance() {
            PersonSnapshot snapshot = null;
            if (getValue() != null) {
                snapshot = getValue().getSnapshot();
            }

            if (getKey() == null || getValue() == null || snapshot == null) {
                // To match bounds
                return Float.MAX_VALUE;
            }

            double x = getKey().getX() - snapshot.getX();
            double y = getKey().getY() - snapshot.getY();

            return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
        }

        @Override
        public int compareTo(LocationPair pair) {
            return (int) Math.ceil(this.distance() - pair.distance());
        }
    }
}
