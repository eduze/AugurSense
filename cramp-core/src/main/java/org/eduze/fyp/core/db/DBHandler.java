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

package org.eduze.fyp.core.db;

import org.eduze.fyp.api.annotations.AutoStart;
import org.eduze.fyp.api.listeners.ProcessedMapListener;
import org.eduze.fyp.api.model.CameraGroup;
import org.eduze.fyp.api.model.CaptureStamp;
import org.eduze.fyp.api.model.Person;
import org.eduze.fyp.api.resources.PersonSnapshot;
import org.eduze.fyp.core.PhotoMapper;
import org.eduze.fyp.core.db.dao.CaptureStampDAO;
import org.eduze.fyp.core.db.dao.PersonDAO;
import org.eduze.fyp.core.db.dao.ZoneDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

@AutoStart(startOrder = 2)
public class DBHandler implements ProcessedMapListener {

    private static final Logger logger = LoggerFactory.getLogger(DBHandler.class);

    private PersonDAO personDAO;

    private ZoneDAO zoneDAO;

    private PhotoMapper photoMapper = null;

    public PhotoMapper getPhotoMapper() {
        return photoMapper;
    }

    public void setPhotoMapper(PhotoMapper photoMapper) {
        this.photoMapper = photoMapper;
    }

    private CaptureStampDAO captureStampDAO;

    public CaptureStampDAO getCaptureStampDAO() {
        return captureStampDAO;
    }

    public void setCaptureStampDAO(CaptureStampDAO captureStampDAO) {
        this.captureStampDAO = captureStampDAO;
    }

    @Override
    public void mapProcessed(CameraGroup cameraGroup, List<List<PersonSnapshot>> snapshots) {
        // Nothing here
    }

    @Override
    public void onFrame(List<List<PersonSnapshot>> snapshots, Date timestamp) {
        captureStampDAO.save(new CaptureStamp(timestamp));

        snapshots.stream()
                .filter(snapshotList -> snapshotList.size() > 0)
                .filter(snapshotList -> !snapshotList.get(0).isStored())
                .map(snapshotList -> {
                    PersonSnapshot snapshot = snapshotList.get(0);

                    int instantZone = -1;
                    int persistantZone = -1;
                    int pastPersistantZone = -1;

                    if (snapshot.getInstanceZone() != null)
                        instantZone = snapshot.getInstanceZone().getId();

                    if (snapshot.getPersistantZone() != null)
                        persistantZone = snapshot.getPersistantZone().getId();

                    if (snapshot.getPastPersistantZone() != null)
                        pastPersistantZone = snapshot.getPastPersistantZone().getId();

                    String previousUuid = "";
                    for (PersonSnapshot personSnapshot : snapshotList) {
                        if (personSnapshot.isStored()) {
                            previousUuid = personSnapshot.getUuid();
                            break;
                        }

                        if (persistantZone == pastPersistantZone) {
                            if (personSnapshot.getPastPersistantZone() != null) {
                                if (pastPersistantZone != personSnapshot.getPastPersistantZone().getId()) {
                                    pastPersistantZone = personSnapshot.getPastPersistantZone().getId();
                                }
                            }
                        }
                    }

                    snapshot.markStored();

                    if (this.photoMapper != null)
                        photoMapper.onDBStore(snapshot);

                    return new Person(snapshot.getIds(), snapshot.getTimestamp(), snapshot.getX(), snapshot.getY(),
                            snapshot.getSitProbability(), snapshot.getStandProbability(), snapshot.getHeadDirectionX(),
                            snapshot.getHeadDirectionY(), instantZone, persistantZone, pastPersistantZone,
                            snapshot.getUuid(), previousUuid, snapshot.getTrackSegmentIndex());
                })
                .forEach(person -> {
                    try {
                        personDAO.save(person);
                    } catch (Exception e) {
                        logger.error("Error saving person", e);
                    }
                });
    }

    public PersonDAO getPersonDAO() {
        return personDAO;
    }

    public void setZoneDAO(ZoneDAO zoneDAO) {
        this.zoneDAO = zoneDAO;
    }

    public ZoneDAO getZoneDAO() {
        return zoneDAO;
    }

    public void setPersonDAO(PersonDAO personDAO) {
        this.personDAO = personDAO;
    }
}
