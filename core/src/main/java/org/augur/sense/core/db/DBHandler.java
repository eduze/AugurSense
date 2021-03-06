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

package org.augur.sense.core.db;

import org.augur.sense.api.model.CameraGroup;
import org.augur.sense.api.model.Person;
import org.augur.sense.api.model.Zone;
import org.augur.sense.core.db.dao.PersonDAO;
import org.augur.sense.core.db.dao.PersonDAO;
import org.augur.sense.core.db.dao.ZoneDAO;
import org.augur.sense.api.annotations.AutoStart;
import org.augur.sense.api.listeners.ProcessedMapListener;
import org.augur.sense.api.model.CameraGroup;
import org.augur.sense.api.model.Person;
import org.augur.sense.api.model.Zone;
import org.augur.sense.api.resources.PersonSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

@AutoStart(startOrder = 2)
public class DBHandler implements ProcessedMapListener {

    private static final Logger logger = LoggerFactory.getLogger(DBHandler.class);

    private PersonDAO personDAO;
    private ZoneDAO zoneDAO;

    @Override
    public void mapProcessed(CameraGroup cameraGroup, List<List<PersonSnapshot>> snapshots) {
        snapshots.stream()
                .filter(snapshotList -> snapshotList.size() > 0)
                .filter(snapshotList -> !snapshotList.get(0).isStored())
                .map(snapshotList -> {
                    PersonSnapshot snapshot = snapshotList.get(0);

                    Zone instantZone = null;
                    Zone persistentZone = null;
                    Zone pastPersistentZone = null;

                    if (snapshot.getInstanceZone() != null)
                        instantZone = snapshot.getInstanceZone();

                    if (snapshot.getPersistantZone() != null)
                        persistentZone = snapshot.getPersistantZone();

                    if (snapshot.getPastPersistantZone() != null)
                        pastPersistentZone = snapshot.getPastPersistantZone();

                    for (PersonSnapshot personSnapshot : snapshotList) {
                        if (personSnapshot.isStored()) {
                            break;
                        }

                        if (persistentZone == pastPersistentZone) {
                            if (pastPersistentZone != null) {
                                if (pastPersistentZone.getId() != personSnapshot.getPastPersistantZone().getId()) {
                                    pastPersistentZone = personSnapshot.getPastPersistantZone();
                                }
                            }
                        }
                    }

                    snapshot.markStored();

                    return new Person(new Date(snapshot.getTimestamp()), snapshot.getX(), snapshot.getY(),
                            instantZone, persistentZone, pastPersistentZone, snapshot.getImage(),
                            snapshot.getPersonId(), snapshot.getSitProbability(), snapshot.getStandProbability(),
                            snapshot.getHeadDirectionX(), snapshot.getHeadDirectionY());
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
