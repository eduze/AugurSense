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

import org.eduze.fyp.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
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

    public synchronized Set<Coordinate> getSnapshot() {
        return personLocations.stream()
                .map(PersonLocation::getSnapshot)
                .collect(Collectors.toSet());
    }

    public synchronized void update(LocalMap localMap) {
        Set<PersonLocation> newPersons = new HashSet<>();
        logger.debug("Updating global map with {} coordinates in local map", localMap.getPersonCoordinates().size());

        for (PersonCoordinate pc : localMap.getPersonCoordinates()) {
            // For this person coordinate, find the closest person location; if any
            double minDistance = Double.MAX_VALUE;
            PersonLocation closest = null;

            for (PersonLocation pl : personLocations) {
                double distance = Math.sqrt(Math.pow(pc.getX() - pl.getSnapshot().getX(), 2) +
                        Math.pow(pc.getY() - pl.getSnapshot().getY(), 2));
                if (distance < minDistance) {
                    minDistance = distance;
                    closest = pl;
                }
            }

            /*
             * Now we know at least that there are person locations in the map since we have something called
             * closest. But there's not guarantee that it is actually close until we check with a threshold.
             * If the person coordinate comes with an image, then there is a possibility that it is a new
             * person. Other wise, no matter what, we have to attach that person to the closest person location.
             */
            if (pc.getImage() != null) {
                // TODO: 9/3/17 Send to Re-ID system and get an ID
                int id = new Random().nextInt(200) + 1;

                if (closest != null && minDistance < Constants.DISTANCE_THRESHOLD) {
                    logger.debug("Found match for person {} at {}", id, pc);
                    closest.addPoint(localMap.getCameraId(), pc.toCoordinate());
                    closest.addId(id);
                } else {
                    // Then, this is a new person
                    logger.debug("Found new person {} at {}", id, pc);
                    PersonLocation newPL = new PersonLocation(id);
                    newPL.addPoint(localMap.getCameraId(), pc.toCoordinate());
                    newPersons.add(newPL);
                }
            } else {
                // We have to attach to the closest person location. Points that are erroneous will be ignored.
                if (closest != null && minDistance < Constants.DISTANCE_THRESHOLD) {
                    logger.debug("Found existing person at {}", pc);
                    closest.addPoint(localMap.getCameraId(), pc.toCoordinate());
                } else {
                    logger.warn("Found erroneous coordinate {}", pc);
                    PersonLocation newPL = new PersonLocation();
                    newPL.addPoint(localMap.getCameraId(), pc.toCoordinate());
                    newPersons.add(newPL);
                }
            }
        }

        personLocations.addAll(newPersons);
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
}
