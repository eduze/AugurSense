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

package org.eduze.fyp.rest.services;

import org.eduze.fyp.api.ConfigurationManager;
import org.eduze.fyp.api.listeners.ProcessedMapListener;
import org.eduze.fyp.api.resources.PersonCoordinate;
import org.eduze.fyp.api.resources.PersonSnapshot;
import org.eduze.fyp.impl.PhotoMapper;
import org.eduze.fyp.impl.db.dao.CaptureStampDAO;
import org.eduze.fyp.impl.db.dao.PersonDAO;
import org.eduze.fyp.impl.db.dao.ZoneDAO;
import org.eduze.fyp.impl.db.model.Person;
import org.eduze.fyp.impl.db.model.Zone;
import org.eduze.fyp.rest.resources.ZoneStatistics;
import org.eduze.fyp.rest.util.ImageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsService implements ProcessedMapListener {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsService.class);

    private List<List<PersonSnapshot>> snapshots = new ArrayList<>();
    private PersonDAO personDAO;

    private ZoneDAO zoneDAO;

    private int timeInterval = 10; //Every 10ms

    private CaptureStampDAO captureStampDAO;

    private ConfigurationManager configurationManager;
    private int mapWidth = -1;
    private int mapHeight = -1;

    private PhotoMapper photoMapper = null;

    public PhotoMapper getPhotoMapper() {
        return photoMapper;
    }

    public void setPhotoMapper(PhotoMapper photoMapper) {
        this.photoMapper = photoMapper;
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

    public AnalyticsService() {
    }

    public List<List<Person>> getTimeBoundMovements(Date start,Date end){
        List<Person> candidates = personDAO.list(start,end);
        Map<Integer,List<Person>> trackedCandidates = new HashMap<>();

        for(Person p : candidates){
            for(int id: p.getIds()){
                if(!trackedCandidates.containsKey(id)) {
                    trackedCandidates.put(id, new ArrayList<>());
                }
                trackedCandidates.get(id).add(p);
            }
        }
        return new ArrayList<List<Person>>(trackedCandidates.values());
    }

    public List<PersonCoordinate> getPhotos(int trackingId){
        return photoMapper.getSnapshots(trackingId);
    }

    public Map<String, byte[]> getMap() throws IOException {
        BufferedImage map = configurationManager.getMap();
        byte[] bytes = ImageUtils.bufferedImageToByteArray(map);
        Map<String, byte[]> response = new HashMap<>();
        response.put("image", bytes);
        return response;
    }

    public List<List<PersonSnapshot>> getRealTimeMap() {
        return snapshots;
    }

    public int[][] getHeatMap(long fromTimestamp, long toTimestamp) {
        if (mapHeight == -1 || mapWidth == -1) {
            mapWidth = configurationManager.getMap().getWidth();
            mapHeight = configurationManager.getMap().getHeight();
        }

        logger.debug("Generating heat map for {}-{} (Map - {}x{})", fromTimestamp, toTimestamp, mapWidth, mapHeight);
        int[][] heatmap = new int[mapHeight][mapWidth];

        Date from = new Date(fromTimestamp);
        Date to = new Date(toTimestamp);
        List<Person> people = personDAO.list(from, to);
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

        return captureStampDAO.getCaptureStampCount(from,to);
    }

    public int getCount(long fromTimestamp, long toTimestamp) {
        List<Integer> ids = getPeopleIds(fromTimestamp, toTimestamp);
        return ids.size();
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
                if (person.getIds().iterator().next() == id) {            // Select relevant rows to relevant ids
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
                canvas[xxx][yyy] = canvas[xxx][yyy]+ dd;
//            }
        }
        return canvas;
    }


    @Override
    public void mapProcessed(List<List<PersonSnapshot>> snapshots) {
        this.snapshots = snapshots;
    }

    @Override
    public void onFrame(List<List<PersonSnapshot>> snapshots, Date timestamp) {

    }

    /**
     * Provides unique ids of people in the given time period
     *
     * @param fromTimestamp
     * @param toTimestamp
     * @return Unique ids of persons
     */
    private ArrayList<Integer> getPeopleIds(long fromTimestamp, long toTimestamp) {
        Date from = new Date(fromTimestamp);
        Date to = new Date(toTimestamp);
        Set<Integer> idSet = new HashSet<>();
        List<Person> people = personDAO.list(from, to);
        for (Person person : people) {
            Set<Integer> set = person.getIds();
            Iterator itr = set.iterator();
            while (itr.hasNext()) {
                idSet.add((Integer) itr.next());
            }
        }
        return new ArrayList<Integer>(idSet);
    }

    public void setPersonDAO(PersonDAO personDAO) {
        this.personDAO = personDAO;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    public List<ZoneStatistics> getZoneStatistics(long fromTimestamp, long toTimestamp) {
        Date from = new Date(fromTimestamp);
        Date to = new Date(toTimestamp);

        long timeStampCount = captureStampDAO.getCaptureStampCount(from,to);

        List<Zone> zones = configurationManager.getZones();

        List<Object[]> zoneCounts = personDAO.getZoneCounts(from, to);

        List<Object[]> zoneStandCounts = personDAO.getZoneStandCounts(from, to, 1);
        List<Object[]> zoneSitCounts = personDAO.getZoneSitCounts(from, to, 1);

        List<Object[]> zoneUnclassifiedPoseCounts = personDAO.getZoneUnclassifiedCounts(from, to, 1, 1);

        Map<Integer, Long> zoneCountMap = new LinkedHashMap<>();
        zoneCounts.forEach(items -> {
            zoneCountMap.put((Integer) items[0], (Long) items[1]);
        });

        Map<Integer, Long> zoneStandCountMap = new LinkedHashMap<>();
        zoneStandCounts.forEach(items -> {
            zoneStandCountMap.put((Integer) items[0], (Long) items[1]);
        });

        Map<Integer, Long> zoneSitCountMap = new LinkedHashMap<>();
        zoneSitCounts.forEach(items -> {
            zoneSitCountMap.put((Integer) items[0], (Long) items[1]);
        });

        Map<Integer, Long> zoneUnclassifiedCountMap = new LinkedHashMap<>();
        zoneUnclassifiedPoseCounts.forEach(items -> {
            zoneUnclassifiedCountMap.put((Integer) items[0], (Long) items[1]);
        });

        List<Object[]> crossCounts = personDAO.getCrossCounts(from, to);


        List<ZoneStatistics> results = new ArrayList<>();
        for (Zone zone : zones) {
            ZoneStatistics statistic = new ZoneStatistics(zone.getId(), zone.getZoneName(), fromTimestamp, toTimestamp);
            if (zoneCountMap.containsKey(zone.getId()))
                statistic.setAveragePersonCount((double) zoneCountMap.get(zone.getId()) / (double) timeStampCount);

            if (zoneSitCountMap.containsKey(zone.getId()))
                statistic.setAverageSittingCount((double) zoneSitCountMap.get(zone.getId()) / (double) timeStampCount);

            if (zoneStandCountMap.containsKey(zone.getId()))
                statistic.setAverageStandingCount((double) zoneStandCountMap.get(zone.getId()) / (double) timeStampCount);

            if (zoneUnclassifiedCountMap.containsKey(zone.getId()))
                statistic.setAverageUnclassifiedPoseCount((double) zoneUnclassifiedCountMap.get(zone.getId()) / (double) timeStampCount);

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
            crossCounts.stream().filter(crossing -> crossing[1] != null && (int) crossing[1] == zone.getId() && crossing[0] != crossing[1]).forEach(crossing -> {
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
}
