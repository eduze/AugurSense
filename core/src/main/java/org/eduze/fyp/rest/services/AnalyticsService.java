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
import org.eduze.fyp.api.State;
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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FilenameFilter;
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

    public List<Person> getTrackingRouteFromUUID(Date start, Date end, String uuid, boolean segmented)
    {
        Person target = personDAO.getPerson(uuid);
        List<Person> candidates = personDAO.list(start,end);
        final ArrayList<Person> result = new ArrayList<>();

        for(int trackId : target.getIds())
        {
            if(!segmented)
            {
                candidates.stream()
                        .filter((person -> person.getIds().contains(trackId)))
                        .forEach(result::add);
            }
            else{
                candidates.stream()
                        .filter((person -> person.getIds().contains(trackId) && person.getTrackSegmentIndex() == target.getTrackSegmentIndex()))
                        .forEach(result::add);
                System.out.println("Segmented");
            }

        }
        return result;

    }



    public List<List<Person>> getTimeBoundMovements(Date start,Date end, boolean useSegmentIndex){
        List<Person> candidates = personDAO.list(start,end);
        Map<String,List<Person>> trackedCandidates = new HashMap<>();

        for(Person p : candidates){
//            List<Person> target = null;
//            if(!trackedCandidates.containsKey(p.getPreviousUuid())){
//                target = new ArrayList<>();
//            }
//            else{
//                target = trackedCandidates.remove(p.getPreviousUuid());
//            }
//            target.add(p);
//
//            trackedCandidates.put(p.getUuid(),target);
            for(int _id: p.getIds()){
                String id = String.valueOf(_id);
                if(useSegmentIndex)
                    id += "_" + p.getTrackSegmentIndex();
                if(!trackedCandidates.containsKey(id)) {
                    trackedCandidates.put(id, new ArrayList<>());
                }
                trackedCandidates.get(id).add(p);
            }
        }
        return new ArrayList<List<Person>>(trackedCandidates.values());
    }

    public PersonCoordinate getProfile(String uuid) throws IOException {
        Person p = personDAO.getPerson(uuid);
        File f  = new File(photoMapper.getPhotoSavePath() + "/" + uuid + ".jpg");
        byte[] bytes = null;
        if(f.exists()){
            bytes = ImageUtils.bufferedImageToByteArray(ImageIO.read(f));
        }

        PersonCoordinate result = new PersonCoordinate(p,bytes);
        return result;
    }

    public List<PersonCoordinate> getPastPhotos(int trackingId, int segmentIndex){
        File dir = new File(photoMapper.getPhotoSavePath());
        dir.mkdirs();

        Map<String,File> fileMap = new HashMap<>();

        File[] snaps = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jpg");
            }
        });

        for(File snap: snaps){
            fileMap.put(snap.getName().replaceFirst("[.][^.]+$", ""),snap);
        }

        List<PersonCoordinate> results = new LinkedList<>();

        List<Person> candidates = personDAO.getPersonFromTrackingId(trackingId);
        for(Person p : candidates){
            if(segmentIndex >= 0 && p.getTrackSegmentIndex() != segmentIndex)
                continue;
            if(fileMap.containsKey(p.getUuid())){
                try {
                    byte[] bytes = ImageUtils.bufferedImageToByteArray(ImageIO.read(fileMap.get(p.getUuid())));
                    PersonCoordinate result = new PersonCoordinate(p,bytes);
                    results.add(result);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

        return results;

    }

    public List<PersonCoordinate> getAllPastPhotosOfZoneFlow(Date start, Date end, int zoneId, boolean isInflow, boolean isOutflow, boolean useSegments){
        File dir = new File(photoMapper.getPhotoSavePath());
        dir.mkdirs();

        List<Zone> availableZones = zoneDAO.list();
        List<Object> availableZoneIds = new ArrayList<>();
        Collections.addAll(availableZoneIds, availableZones.stream().map(Zone::getId).toArray());

        Map<String,File> fileMap = new HashMap<>();

        File[] snaps = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jpg");
            }
        });

        for(File snap: snaps){
            fileMap.put(snap.getName().replaceFirst("[.][^.]+$", ""),snap);
        }

        HashMap<String,Person> resultContentMap = new HashMap<>();
        HashMap<Integer,Person> resultImageMap = new HashMap<>();
        List<Person[]> candidates = new ArrayList<>();
        if(isInflow)
            candidates.addAll(personDAO.getZoneInflow(start,end,zoneId, useSegments));
        if(isOutflow)
            candidates.addAll(personDAO.getZoneOutflow(start,end,zoneId, useSegments));
        for(Object[] ps : candidates){
            Person p2 = (Person) ps[1];
            if(!availableZoneIds.contains(p2.getPastPersistantZoneId()))
                continue;
            if(!availableZoneIds.contains(p2.getPersistantZoneId()))
                continue;
            Person p = (Person) ps[0];
            p.getIds().forEach((id)->{
                if(fileMap.containsKey(p.getUuid())) {
                    resultImageMap.put(id, p);
                    resultContentMap.put(p.getUuid(),p2);
                }
            });
        }

        final List<PersonCoordinate> results = new LinkedList<>();

        resultImageMap.values().forEach((p)->{
            if(fileMap.containsKey(p.getUuid())){
                try {
                    byte[] bytes = ImageUtils.bufferedImageToByteArray(ImageIO.read(fileMap.get(p.getUuid())));
                    Person content = resultContentMap.get(p.getUuid());
                    content.setUuid(p.getUuid()); //This is a hack undertaken to make re-id link work
                    PersonCoordinate result = new PersonCoordinate(content,bytes);
                    results.add(result);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        return results;

    }

    public List<PersonCoordinate> getAllPastPhotos(Date start, Date end){
        File dir = new File(photoMapper.getPhotoSavePath());
        dir.mkdirs();

        Map<String,File> fileMap = new HashMap<>();

        File[] snaps = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jpg");
            }
        });

        for(File snap: snaps){
            fileMap.put(snap.getName().replaceFirst("[.][^.]+$", ""),snap);
        }


        HashMap<Integer,Person> resultMap = new HashMap<>();
        List<Person> candidates = personDAO.list(start,end);
        for(Person p : candidates){
            p.getIds().forEach((id)->{
                if(fileMap.containsKey(p.getUuid())) {
                    resultMap.put(id, p);
                }
            });
        }

        final List<PersonCoordinate> results = new LinkedList<>();

        resultMap.values().forEach((p)->{
            if(fileMap.containsKey(p.getUuid())){
                try {
                    byte[] bytes = ImageUtils.bufferedImageToByteArray(ImageIO.read(fileMap.get(p.getUuid())));
                    PersonCoordinate result = new PersonCoordinate(p,bytes);
                    results.add(result);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        return results;

    }


    public List<PersonCoordinate> getRealtimePhotosAll(){

        //TODO: Need to identify dead trackers here

//        final ArrayList<PersonCoordinate> results = new ArrayList<>();
//        photoMapper.getLatestSnapshots().stream().filter((pin)->
//            snapshots.stream().filter((hay)->
//                hay.get(0).getIds().stream().findFirst().equals(pin.getIds().stream().findFirst())
//            ).count()>0
//        ).forEach(results::add);
        return photoMapper.getLatestSnapshots();
    }

    public List<PersonCoordinate> getRealtimePhotos(int trackingId){
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

    private HashMap<Integer,HashMap<Long,Double>> getTotalCountVariation(long fromTimestamp, long toTimestamp, int divisionCount, String additionalCondition){
        List<Object[]> totalCountVariation = personDAO.getPersonCountVariation(new Date(fromTimestamp), new Date(toTimestamp), additionalCondition);
        final HashMap<Integer,HashMap<Long,Long>> totalPeople = new LinkedHashMap<>();
        final HashMap<Integer,HashMap<Long,Long>> totalTime = new LinkedHashMap<>();

        long divisionLength = (toTimestamp - fromTimestamp) / divisionCount;

        totalCountVariation.forEach((v)->{

            Date timestamp = (Date) v[0];
            int zoneIndex = (int) v[1];
            long p_count = (long)v[2];
            long t_count = (long)v[3];

            HashMap<Long,Long> zonePeople = totalPeople.getOrDefault(zoneIndex, new LinkedHashMap<>());
            HashMap<Long,Long> zoneTime = totalTime.getOrDefault(zoneIndex, new LinkedHashMap<>());

            long t_offset = timestamp.getTime() % divisionLength;
            long divisionStart = timestamp.getTime() - t_offset;
            long divisionEnd = divisionStart + t_offset;
            long divisionMid = (divisionStart + divisionEnd)/2;
            zonePeople.put(divisionMid, zonePeople.getOrDefault(divisionMid, 0L)+p_count);
            zoneTime.put(divisionMid, zoneTime.getOrDefault(divisionMid, 0L)+t_count);

            totalPeople.put(zoneIndex,zonePeople);
            totalTime.put(zoneIndex,zoneTime);
        });

        final HashMap<Integer, HashMap<Long,Double>> result = new LinkedHashMap<>();
        totalPeople.forEach((zoneIndex,zonePeopleVariation)->{
            HashMap<Long,Double> zoneResult = new LinkedHashMap<>();
            HashMap<Long,Long> zoneTimeVariation = totalTime.get(zoneIndex);
            zonePeopleVariation.forEach((t,p_count)->{
                zoneResult.put(t,Double.valueOf(p_count)/Double.valueOf(zoneTimeVariation.get(t)));
            });
            result.put(zoneIndex,zoneResult);
        });

        return result;

//        for(int i = 0; i < divisionCount; i++)
//        {
//            long divisionStart = divisionLength * i;
//            long divisionEnd = divisionLength * (i + 1);
//            long divisionMid = (divisionStart+divisionEnd)/2;
//
//            long divisionTotalPeople = 0;
//            long divisionTotalTimestamps = 0;
//
//        }
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

        //Total count variation
        HashMap<Integer,HashMap<Long,Double>> totalCountVariation = getTotalCountVariation(fromTimestamp,toTimestamp,20 , "");

        //Total sitting count variation
        HashMap<Integer,HashMap<Long,Double>> totalSittingCountVariation = getTotalCountVariation(fromTimestamp,toTimestamp,20 , "and P.sitProbability >= 1 ");

        HashMap<Integer,HashMap<Long,Double>> totalStandingCountVariation = getTotalCountVariation(fromTimestamp,toTimestamp,20 , "and P.standProbability >= 1 ");

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

            if(totalCountVariation.containsKey(zone.getId()))
                statistic.setTotalCountVariation(totalCountVariation.get(zone.getId()));

            if(totalStandingCountVariation.containsKey(zone.getId()))
                statistic.setTotalStandingCountVariation(totalStandingCountVariation.get(zone.getId()));

            if(totalSittingCountVariation.containsKey(zone.getId()))
                statistic.setTotalSittingCountVariation(totalSittingCountVariation.get(zone.getId()));


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
