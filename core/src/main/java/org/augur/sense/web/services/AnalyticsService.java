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

package org.augur.sense.web.services;

import org.augur.sense.api.ConfigurationManager;
import org.augur.sense.api.Constants;
import org.augur.sense.api.model.CameraGroup;
import org.augur.sense.api.model.Person;
import org.augur.sense.api.model.Zone;
import org.augur.sense.api.util.ImageUtils;
import org.augur.sense.core.db.dao.CameraConfigDAO;
import org.augur.sense.core.db.dao.CaptureStampDAO;
import org.augur.sense.core.db.dao.PersonDAO;
import org.augur.sense.core.db.dao.ZoneDAO;
import org.augur.sense.api.ConfigurationManager;
import org.augur.sense.api.Constants;
import org.augur.sense.api.listeners.ProcessedMapListener;
import org.augur.sense.api.model.CameraGroup;
import org.augur.sense.api.model.Person;
import org.augur.sense.api.model.Zone;
import org.augur.sense.api.resources.PersonCoordinate;
import org.augur.sense.api.resources.PersonSnapshot;
import org.augur.sense.api.util.ImageUtils;
import org.augur.sense.core.db.dao.CameraConfigDAO;
import org.augur.sense.core.db.dao.CaptureStampDAO;
import org.augur.sense.core.db.dao.PersonDAO;
import org.augur.sense.core.db.dao.ZoneDAO;
import org.augur.sense.web.resources.ZoneStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

public class AnalyticsService implements ProcessedMapListener {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsService.class);

    private Map<Integer, List<List<PersonSnapshot>>> snapshots = new HashMap<>();
    private PersonDAO personDAO;

    private ZoneDAO zoneDAO;
    private CameraConfigDAO cameraConfigDAO;

    private int timeInterval = 10; //Every 10ms

    private CaptureStampDAO captureStampDAO;

    private ConfigurationManager configurationManager;
    private int mapWidth = Constants.MAP_IMAGE_WIDTH;
    private int mapHeight = Constants.MAP_IMAGE_HEIGHT;

    public AnalyticsService() { }

    public List<List<Person>> getTimeBoundMovements(int cameraGroupId, Date start, Date end, boolean useSegmentIndex) {
        CameraGroup cameraGroup = cameraConfigDAO.findCameraGroupById(cameraGroupId);
        List<Zone> zones = cameraGroup.getZones();
        List<Person> people = personDAO.listByInstantZone(zones, start, end);

        Map<String, List<Person>> trackedCandidates = new HashMap<>();

        people.forEach(person -> {
            String id = String.valueOf(person.getPersonId());
            List<Person> tracks = trackedCandidates.computeIfAbsent(id, key -> new ArrayList<>());
            tracks.add(person);
        });

        return new ArrayList<>(trackedCandidates.values());
    }

    public List<PersonCoordinate> getRealtimePhotosAll() {
        //TODO: Need to identify dead trackers here

        //        final ArrayList<PersonCoordinate> results = new ArrayList<>();
        //        photoMapper.getLatestSnapshots().stream().filter((pin)->
        //            snapshots.stream().filter((hay)->
        //                hay.get(0).getIds().stream().findFirst().equals(pin.getIds().stream().findFirst())
        //            ).count()>0
        //        ).forEach(results::add);
        return Collections.emptyList();
    }

    public Map<String, byte[]> getMap() throws IOException {
        BufferedImage map = configurationManager.getMap();
        byte[] bytes = ImageUtils.bufferedImageToByteArray(map);
        Map<String, byte[]> response = new HashMap<>();
        response.put("image", bytes);
        return response;
    }

    public List<List<PersonSnapshot>> getRealTimeMap(int cameraGroupId) {
        return snapshots.computeIfAbsent(cameraGroupId, key -> new ArrayList<>());
    }

    public int[][] getHeatMap(int cameraGroupId, long fromTimestamp, long toTimestamp) {
        logger.debug("Generating heat map for {}-{} (Map - {}x{})", fromTimestamp, toTimestamp, mapWidth, mapHeight);
        int[][] heatmap = new int[mapHeight][mapWidth];

        CameraGroup cameraGroup = cameraConfigDAO.findCameraGroupById(cameraGroupId);
        List<Zone> zones = new ArrayList<>(cameraGroup.getZones());
        Date from = new Date(fromTimestamp);
        Date to = new Date(toTimestamp);
        List<Person> people = personDAO.listByInstantZone(zones, from, to);
        if (people == null || people.size() == 0) {
            return heatmap;
        }

        people.forEach(person -> {
            int x = (int) person.getX();
            int y = (int) person.getY();
            if (x >= 0 && y >= 0 && x < mapWidth && y < mapHeight) {
                heatmap[y][x] += 1;
            }
        });
        return heatmap;
    }

    public List<Object[]> getCrossCount(long fromTimestamp, long toTimestamp) {

        Date from = new Date(fromTimestamp);
        Date to = new Date(toTimestamp);

        return personDAO.getCrossCounts(from, to);
    }

    public long getTimestampCount(long fromTimestamp, long toTimestamp) {

        Date from = new Date(fromTimestamp);
        Date to = new Date(toTimestamp);

        return captureStampDAO.getCaptureStampCount(from, to);
    }

    public int getCount(long fromTimestamp, long toTimestamp) {
        List<Integer> ids = getPeopleIds(fromTimestamp, toTimestamp);
        return ids.size();
    }

    public HashMap<Double, Integer> getOverallVelocityFrequency(Date startDate, Date endDate, long timeInterval, boolean segmented, int basketCount) {
        List<Person> persons = personDAO.listTrackOrderedOverall(startDate, endDate, segmented);
        HashMap<Long, List<Double>> timeVariation = getTimeVelocityDistribution(persons, 0, persons.size(), timeInterval, false);
        List<Double> flatVariation = new ArrayList<>();
        timeVariation.values().forEach(flatVariation::addAll);
        Collections.sort(flatVariation);


        if (flatVariation.size() == 0)
            return new HashMap<>();

        double min = flatVariation.get(0);
        double max = flatVariation.get(flatVariation.size() - 1);
        double basketWidth = (max - min) / basketCount;

        HashMap<Double, Integer> resultBaskets = new LinkedHashMap<>();
        flatVariation.forEach((v) -> {
            double offset = v % basketWidth;
            double start = v - offset;
            double end = v + basketWidth;
            double mid = (start + end) / 2;
            mid = Math.round(mid);
            resultBaskets.put(mid, resultBaskets.getOrDefault(mid, 0) + 1);
        });
        return resultBaskets;
    }

    public HashMap<Long, List<Double>> getOverallTimeVelocityDistribution(Date startDate, Date endDate, long timeInterval, boolean segmented, int basketCount) {
        List<Person> persons = personDAO.listTrackOrderedOverall(startDate, endDate, segmented);
        HashMap<Long, List<Double>> timeVariation = getTimeVelocityDistribution(persons, 0, persons.size(), timeInterval, false);

        return timeVariation;
    }

    public HashMap<Long, List<Double>> getZonedTimeVelocityDistribution(Date startDate, Date endDate, int zoneId, long timeInterval, boolean segmented, int basketCount) {
        List<Person> persons = personDAO.listTrackOrderedInZone(startDate, endDate, zoneId, segmented);
        HashMap<Long, List<Double>> timeVariation = getTimeVelocityDistribution(persons, 0, persons.size(), timeInterval, false);

        return timeVariation;
    }

    public HashMap<Double, Integer> getZonedVelocityFrequency(Date startDate, Date endDate, int zoneId, long timeInterval, boolean segmented, int basketCount) {
        List<Person> persons = personDAO.listTrackOrderedInZone(startDate, endDate, zoneId, segmented);
        HashMap<Long, List<Double>> timeVariation = getTimeVelocityDistribution(persons, 0, persons.size(), timeInterval, false);
        List<Double> flatVariation = new ArrayList<>();
        timeVariation.values().forEach(flatVariation::addAll);
        Collections.sort(flatVariation);

        if (flatVariation.size() == 0)
            return new HashMap<>();

        double min = flatVariation.get(0);
        double max = flatVariation.get(flatVariation.size() - 1);
        double basketWidth = (max - min) / basketCount;

        HashMap<Double, Integer> resultBaskets = new LinkedHashMap<>();
        flatVariation.forEach((v) -> {
            double offset = v % basketWidth;
            double start = v - offset;
            double end = v + basketWidth;
            double mid = (start + end) / 2;
            mid = Math.round(mid);
            resultBaskets.put(mid, resultBaskets.getOrDefault(mid, 0) + 1);
        });
        return resultBaskets;
    }

    private HashMap<Long, List<Double>> getTimeVelocityDistribution(List<Person> persons, int startIndex, int stopIndex, long timeInterval, boolean segmented) {
        int i = startIndex;
        HashMap<Long, List<Double>> results = new LinkedHashMap<>();

        while (i < stopIndex) {
            //find the bounds for time interval in each person
            int i_p = i;
            int personId = persons.get(i_p).getId();
            while ((i_p < stopIndex) && (persons.get(i_p).getId() == personId) && (!segmented)) {
                long personStartTime = persons.get(i_p).getTimestamp().getTime();

                int i_t_start = i_p;
                //now identify the time intervals
                while (i_t_start < stopIndex && persons.get(i_t_start).getId() == personId) {
                    long frameStartTime = persons.get(i_t_start).getTimestamp().getTime();

                    // identify time intervals bounds
                    int i_t = i_t_start;
                    while ((i_t < stopIndex) && (persons.get(i_t).getId() == personId)) {
                        if (persons.get(i_t).getTimestamp().getTime() - frameStartTime < timeInterval) {
                            i_t++;
                        } else {
                            break;
                        }
                    }

                    boolean addBack = false;
                    if (i_t >= stopIndex) {
                        i_t = stopIndex - 1;
                        addBack = true;
                    }
                    int i_t_end = i_t;
                    long frameEndTime = persons.get(i_t_end).getTimestamp().getTime();
                    //continue;
                    if (i_t_start < i_t_end) {
                        double averageVelocity = getAverageVelocity(persons, i_t_start, i_t_end);
                        List<Double> samples = results.getOrDefault(frameStartTime, new ArrayList<>());
                        samples.add(averageVelocity);
                        results.put(frameStartTime, samples);
                    }

                    if (addBack) {
                        i_t += 1;
                        i_t_end = i_t;
                    }

                    i_t_start = i_t_end;
                }

                i_p = i_t_start;

            }
            i = i_p;
        }
        return results;
    }

    public double getAverageVelocity(List<Person> persons, int startIndex, int stopIndex) {
        double averageX = 0;
        double averageY = 0;
        int count = 0;
        for (int i = startIndex; i < stopIndex; i++) {
            Person person = persons.get(i);
            averageX += person.getX();
            averageY += person.getY();
            count += 1;
        }

        averageX = averageX / count;
        averageY = averageY / count;

        double totalVelocity = 0;

        for (int i = startIndex; i < stopIndex; i++) {
            Person person = persons.get(i);
            double xDif = averageX - person.getX();
            double yDif = averageY - person.getY();
            double vel = Math.sqrt(xDif * xDif + yDif * yDif);
            totalVelocity += vel;
        }

        double result = totalVelocity / count;
        if (Double.isNaN(result)) {
            assert false;
            //System.out.println("Nan");
        }
        return result;

    }

    public int[][] getStopPoints(long fromTimestamp, long toTimestamp, int r, int t, int height, int width) {

        Date from = new Date(fromTimestamp);
        Date to = new Date(toTimestamp);
        List<int[]> stopPoints = new ArrayList<>();                      //Final stop points [x][y][density]
        List<Integer> ids = getPeopleIds(fromTimestamp, toTimestamp);    // List of ids in relevant time period
        List<Person> idSet = personDAO.list(from, to);                   // Get all data base row within time period

        for (int id : ids) {                                              // Get one id
            List<Person> temp = new ArrayList<>();
            Iterator<Person> iter = idSet.iterator();
            while (iter.hasNext()) {                                      // Go through all the rows
                Person person = iter.next();
                if (person.getId() == id) {            // Select relevant rows to relevant ids
                    temp.add(person);                                     // Add rows to relevant id to temp array
                }
            }
            //Add stop points to a common list
            List<Person> local = new ArrayList<>();
            if (temp.size() > 1) {
                double x = temp.get(0).getX();
                double y = temp.get(0).getY();

                for (int i = 1; i < temp.size(); i++) {
                    if ((Math.abs(temp.get(i).getX() - temp.get(i - 1).getX()) < r) &&
                            (Math.abs(temp.get(i).getY() - temp.get(i - 1).getY()) < r)) {
                        //
                        //                        if (local.size() > 0 && (Math.abs(temp.get(i).getX() - local.get(0).getX()) < 10) &&
                        //                                (Math.abs(temp.get(i).getY() - local.get(0).getY()) < 10)) {
                        local.add(temp.get(i));
                        x = x + temp.get(i).getX();
                        y = y + temp.get(i).getY();
                        //                        }

                    } else {
                        if (local.size() > t) {
                            int k = local.size();
                            int[] add = {((int) x) / k, ((int) y) / k, k};
                            stopPoints.add(add);
                        }
                        local.clear();
                        x = temp.get(i).getX();
                        y = temp.get(i).getY();
                    }
                }
            }
        }
        int[][] canvas = new int[height][width];
        for (int k = 0; k < height; k++) {
            Arrays.fill(canvas[k], 0);
        }

        int threshold = 5;
        for (int j = 0; j < stopPoints.size(); j++) {
            double xx = stopPoints.get(j)[0];
            double yy = stopPoints.get(j)[1];
            int dd = stopPoints.get(j)[2];
            int xxx = (int) (xx / threshold);
            int yyy = (int) (yy / threshold);
            if (xx % threshold > threshold / 2)
                xxx++;
            if (yy % threshold > threshold / 2)
                yyy++;

            //            if (canvas[xxx][yyy] < dd) {
            canvas[xxx][yyy] = canvas[xxx][yyy] + dd;
            //            }
        }
        return canvas;
    }


    @Override
    public void mapProcessed(CameraGroup cameraGroup, List<List<PersonSnapshot>> snapshots) {
        this.snapshots.put(cameraGroup.getId(), snapshots);
    }

    /**
     * Provides unique ids of people in the given time period
     *
     * @param fromTimestamp from
     * @param toTimestamp   to
     * @return Unique ids of persons
     */
    private ArrayList<Integer> getPeopleIds(long fromTimestamp, long toTimestamp) {
        Date from = new Date(fromTimestamp);
        Date to = new Date(toTimestamp);
        Set<Integer> idSet = new HashSet<>();
        List<Person> people = personDAO.list(from, to);
        people.forEach(person -> {
            idSet.add(person.getId());
        });
        return new ArrayList<>(idSet);
    }

    public void setPersonDAO(PersonDAO personDAO) {
        this.personDAO = personDAO;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    private HashMap<Long, Double> getTotalCountVariation(long fromTimestamp, long toTimestamp, int divisionCount, String additionalCondition) {
        List<Object[]> totalCountVariation = personDAO.getTotalPersonCountVariation(new Date(fromTimestamp), new Date(toTimestamp), additionalCondition);
        final HashMap<Long, Long> totalPeople = new LinkedHashMap<>();
        final HashMap<Long, Long> totalTime = new LinkedHashMap<>();

        long divisionLength = (toTimestamp - fromTimestamp) / divisionCount;

        totalCountVariation.forEach((v) -> {
            Date timestamp = (Date) v[0];
            long p_count = (long) v[1];
            long t_count = (long) v[2];

            long t_offset = timestamp.getTime() % divisionLength;
            long divisionStart = timestamp.getTime() - t_offset;
            long divisionEnd = divisionStart + t_offset;
            long divisionMid = (divisionStart + divisionEnd) / 2;
            totalPeople.put(divisionMid, totalPeople.getOrDefault(divisionMid, 0L) + p_count);
            totalTime.put(divisionMid, totalTime.getOrDefault(divisionMid, 0L) + t_count);
        });

        final HashMap<Long, Double> result = new LinkedHashMap<>();

        totalPeople.forEach((t, p_count) -> {
            result.put(t, Double.valueOf(p_count) / Double.valueOf(totalTime.get(t)));
        });


        return result;

    }

    private HashMap<Integer, HashMap<Long, Double>> getTotalZoneCountVariation(long fromTimestamp, long toTimestamp, int divisionCount, String additionalCondition) {
        List<Object[]> totalCountVariation = personDAO.getZonePersonCountVariation(new Date(fromTimestamp), new Date(toTimestamp), additionalCondition);
        final HashMap<Integer, HashMap<Long, Long>> totalPeople = new LinkedHashMap<>();
        final HashMap<Integer, HashMap<Long, Long>> totalTime = new LinkedHashMap<>();

        long divisionLength = (toTimestamp - fromTimestamp) / divisionCount;

        totalCountVariation.forEach((v) -> {
            Date timestamp = (Date) v[0];
            Zone zone = (Zone) v[1];
            long p_count = (long) v[2];
            long t_count = (long) v[3];

            HashMap<Long, Long> zonePeople = totalPeople.getOrDefault(zone, new LinkedHashMap<>());
            HashMap<Long, Long> zoneTime = totalTime.getOrDefault(zone, new LinkedHashMap<>());

            long t_offset = timestamp.getTime() % divisionLength;
            long divisionStart = timestamp.getTime() - t_offset;
            long divisionEnd = divisionStart + t_offset;
            long divisionMid = (divisionStart + divisionEnd) / 2;
            zonePeople.put(divisionMid, zonePeople.getOrDefault(divisionMid, 0L) + p_count);
            zoneTime.put(divisionMid, zoneTime.getOrDefault(divisionMid, 0L) + t_count);

            totalPeople.put(zone.getId(), zonePeople);
            totalTime.put(zone.getId(), zoneTime);
        });

        final HashMap<Integer, HashMap<Long, Double>> result = new LinkedHashMap<>();
        totalPeople.forEach((zoneIndex, zonePeopleVariation) -> {
            HashMap<Long, Double> zoneResult = new LinkedHashMap<>();
            HashMap<Long, Long> zoneTimeVariation = totalTime.get(zoneIndex);
            zonePeopleVariation.forEach((t, p_count) -> {
                zoneResult.put(t, Double.valueOf(p_count) / Double.valueOf(zoneTimeVariation.get(t)));
            });
            result.put(zoneIndex, zoneResult);
        });

        return result;

    }

    public List<ZoneStatistics> getZoneStatistics(int cameraGroupId, long fromTimestamp, long toTimestamp) {
        Date from = new Date(fromTimestamp);
        Date to = new Date(toTimestamp);

        CameraGroup cameraGroup = cameraConfigDAO.findCameraGroupById(cameraGroupId);
        List<Zone> zones = cameraGroup.getZones();

        Map<Integer, Long> zonePeopleCountMap = new HashMap<>();
        Map<Integer, Long> zoneStandCountMap = new HashMap<>();
        Map<Integer, Long> zoneSitCountMap = new HashMap<>();
        Map<Integer, Long> zoneUnclassifiedCountMap = new HashMap<>();

        zones.forEach(zone -> {
            zonePeopleCountMap.put(zone.getId(), personDAO.peopleCountByZone(zone, from, to));
            zoneSitCountMap.put(zone.getId(), personDAO.getSitCountsByZone(zone, from, to, 1));
            zoneStandCountMap.put(zone.getId(), personDAO.getStandCountsByZone(zone, from, to, 1));
            zoneUnclassifiedCountMap.put(zone.getId(), personDAO.getUnclassifiedCountsByZone(zone, from, to, 1, 1));
        });

        long totalPeopleCount = zonePeopleCountMap.values().stream().mapToLong(value -> value).sum();
        long totalStandingCount = zoneStandCountMap.values().stream().mapToLong(value -> value).sum();
        long totalSittingCount = zoneSitCountMap.values().stream().mapToLong(value -> value).sum();
        long totalUnclassifiedCount = zoneUnclassifiedCountMap.values().stream().mapToLong(value -> value).sum();

        logger.debug("People: {}", zonePeopleCountMap);
        logger.debug("Stand: {}", zoneStandCountMap);
        logger.debug("Sit: {}", zoneSitCountMap);
        logger.debug("Unclassified: {}", zoneUnclassifiedCountMap);

        logger.debug("Counts: {} {} {} {}", totalPeopleCount, totalStandingCount, totalSittingCount, totalUnclassifiedCount);

        List<Object[]> crossCounts = personDAO.getCrossCounts(from, to);
        logger.debug("Cross: {}", crossCounts);

        //Total count variation
        HashMap<Integer, HashMap<Long, Double>> totalCountVariation =
                getTotalZoneCountVariation(fromTimestamp, toTimestamp, 20, "");

        //Total sitting count variation
        HashMap<Integer, HashMap<Long, Double>> totalSittingCountVariation =
                getTotalZoneCountVariation(fromTimestamp, toTimestamp, 20, "and P.sitProbability >= 1 ");

        HashMap<Integer, HashMap<Long, Double>> totalStandingCountVariation =
                getTotalZoneCountVariation(fromTimestamp, toTimestamp, 20, "and P.standProbability >= 1 ");

        List<ZoneStatistics> results = new ArrayList<>();
        for (Zone zone : zones) {
            ZoneStatistics statistic = new ZoneStatistics(zone.getId(), zone.getZoneName(), fromTimestamp, toTimestamp);

            if (zone.getId() != 0) {
                if (zonePeopleCountMap.containsKey(zone.getId()))
                    statistic.setAveragePersonCount((double) zonePeopleCountMap.get(zone.getId()) / (double) totalPeopleCount);

                if (zoneSitCountMap.containsKey(zone.getId()))
                    statistic.setAverageSittingCount((double) zoneSitCountMap.get(zone.getId()) / (double) totalPeopleCount);

                if (zoneStandCountMap.containsKey(zone.getId()))
                    statistic.setAverageStandingCount((double) zoneStandCountMap.get(zone.getId()) / (double) totalPeopleCount);

                if (zoneUnclassifiedCountMap.containsKey(zone.getId()))
                    statistic.setAverageUnclassifiedPoseCount((double) zoneUnclassifiedCountMap.get(zone.getId()) / (double) totalPeopleCount);

                if (totalCountVariation.containsKey(zone.getId()))
                    statistic.setTotalCountVariation(totalCountVariation.get(zone.getId()));

                if (totalStandingCountVariation.containsKey(zone.getId()))
                    statistic.setTotalStandingCountVariation(totalStandingCountVariation.get(zone.getId()));

                if (totalSittingCountVariation.containsKey(zone.getId()))
                    statistic.setTotalSittingCountVariation(totalSittingCountVariation.get(zone.getId()));
            } else {
                //For the world, statistics need to be separately calculated
                statistic.setAveragePersonCount((double) totalPeopleCount / (double) totalPeopleCount);
                statistic.setAverageStandingCount((double) totalStandingCount / (double) totalPeopleCount);
                statistic.setAverageSittingCount((double) totalSittingCount / (double) totalPeopleCount);
                statistic.setAverageUnclassifiedPoseCount((double) totalUnclassifiedCount / (double) totalPeopleCount);

                statistic.setTotalCountVariation(getTotalCountVariation(fromTimestamp, toTimestamp, 20, ""));
                statistic.setTotalSittingCountVariation(getTotalCountVariation(fromTimestamp, toTimestamp, 20, "and P.sitProbability >= 1 "));
                statistic.setTotalStandingCountVariation(getTotalCountVariation(fromTimestamp, toTimestamp, 20, "and P.standProbability >= 1 "));
            }

            final long[] totalOutgoing = {0};
            Map<Integer, Long> outgoingCounts = new HashMap<>();
            crossCounts.stream().filter(crossing -> crossing[0] != null && (int) crossing[0] == zone.getId() && crossing[0] != crossing[1]).forEach(crossing -> {
                totalOutgoing[0] += (long) crossing[2];
                if (crossing[1] != null)
                    outgoingCounts.put((int) crossing[1], (long) crossing[2]);
                else
                    outgoingCounts.put(-1, (long) crossing[2]);
            });

            statistic.setOutgoingMap(outgoingCounts);

            final long[] totalIncomming = {0};
            Map<Integer, Long> incommingCounts = new HashMap<>();
            crossCounts.stream().filter(crossing -> crossing[1] != null && (int) crossing[1] == zone.getId() && crossing[0] != crossing[1])
                    .forEach(crossing -> {
                        totalIncomming[0] += (long) crossing[2];
                        if (crossing[0] != null)
                            incommingCounts.put((int) crossing[0], (long) crossing[2]);
                        else
                            incommingCounts.put(-1, (long) crossing[2]);
                    });

            statistic.setIncomingMap(incommingCounts);

            statistic.setTotalIncoming(totalIncomming[0]);
            statistic.setTotalOutgoing(totalOutgoing[0]);

            results.add(statistic);
        }
        return results;
    }

    public void setCaptureStampDAO(CaptureStampDAO captureStampDAO) {
        this.captureStampDAO = captureStampDAO;
    }

    public CaptureStampDAO getCaptureStampDAO() {
        return captureStampDAO;
    }

    public void setZoneDAO(ZoneDAO zoneDAO) {
        this.zoneDAO = zoneDAO;
    }

    public ZoneDAO getZoneDAO() {
        return zoneDAO;
    }

    public CameraConfigDAO getCameraConfigDAO() {
        return cameraConfigDAO;
    }

    public void setCameraConfigDAO(CameraConfigDAO cameraConfigDAO) {
        this.cameraConfigDAO = cameraConfigDAO;
    }
}
