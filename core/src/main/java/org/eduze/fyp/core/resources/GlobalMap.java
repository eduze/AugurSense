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

import org.eduze.fyp.core.reid.ReIdentifier;
import org.eduze.fyp.api.resources.LocalMap;
import org.eduze.fyp.api.resources.PersonCoordinate;
import org.eduze.fyp.api.resources.PersonSnapshot;
import org.eduze.fyp.api.util.ImageUtils;
import org.eduze.fyp.core.ZoneMapper;
import org.eduze.fyp.core.util.AccuracyTester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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

    private Map<Integer, PersonLocation> personLocations = new HashMap<>();
    private ZoneMapper zoneMapper;
    private AccuracyTester accuracyTester;
    private ReIdentifier reIdentifier;

    public GlobalMap() {
        reIdentifier = new ReIdentifier();
    }

    public synchronized void update(LocalMap localMap) {
        logger.debug("Updating global map with {} coordinates in local map", localMap.getPersonCoordinates().size());

        Set<Integer> newIds = new HashSet<>();
        localMap.getPersonCoordinates().forEach(personCoordinate -> {
            try {
                BufferedImage image = ImageUtils.byteArrayToBufferedImage(personCoordinate.getImage());
                // find closeby person locations
                List<Integer> closeByIndices = new ArrayList<>();
                personLocations.forEach((k, v) -> {
                    double x2 = v.getSnapshot().getX();
                    double y2 = v.getSnapshot().getY();

                    double x1 = personCoordinate.getX();
                    double y1 = personCoordinate.getY();

                    double d = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
                    if (d < 100)
                        closeByIndices.add(k);
                });

                int id = reIdentifier.identify(image, closeByIndices);
                if (newIds.contains(id)) {
                    logger.warn("Id conflict. Same person have been matched before - {}", id);
                } else {
                    if (personLocations.containsKey(id)) {
                        logger.debug("Found another point for person - {}", id);
                        personLocations.get(id).addPoint(localMap.getCameraId(), personCoordinate);
                    } else {
                        logger.debug("Identified new person - {}", id);
                        newIds.add(id);
                        PersonLocation newPL = new PersonLocation(id);
                        newPL.addPoint(localMap.getCameraId(), personCoordinate);
                        personLocations.put(id, newPL);
                    }
                }
            } catch (IOException e) {
                logger.error("Unable to decode image: {}", e);
            }
        });

        if (this.zoneMapper != null) {
            zoneMapper.processPersonLocations(new ArrayList<>(personLocations.values()));
        }
    }

    public synchronized void refresh(long minTimestamp) {
        logger.debug("Refreshing global map with minimum timestamp {}", minTimestamp);

        Iterator<Map.Entry<Integer, PersonLocation>> iterator = personLocations.entrySet().iterator();
        while (iterator.hasNext()) {
            PersonLocation personLocation = iterator.next().getValue();
            Map<Integer, PersonCoordinate> coordinates = personLocation.getContributingCoordinates();
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

    @Override
    public void finalize() {
        reIdentifier.close();
    }

    public AccuracyTester getAccuracyTester() {
        return accuracyTester;
    }

    public void setAccuracyTester(AccuracyTester accuracyTester) {
        this.accuracyTester = accuracyTester;
    }

    public void setZoneMapper(ZoneMapper zoneMapper) {
        this.zoneMapper = zoneMapper;
    }

    public ZoneMapper getZoneMapper() {
        return zoneMapper;
    }

    public synchronized List<List<PersonSnapshot>> getSnapshot() {
        return personLocations.values().stream()
                .map(PersonLocation::getSnapshots)
                .collect(Collectors.toList());
    }
}
