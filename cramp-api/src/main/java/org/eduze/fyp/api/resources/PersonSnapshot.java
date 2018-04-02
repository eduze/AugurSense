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

import org.eduze.fyp.api.model.Zone;

public class PersonSnapshot extends PersonCoordinate {

    private int personId;
    private Zone instanceZone;
    private Zone persistantZone;
    private Zone pastPersistantZone;
    private boolean stored = false;

    public PersonSnapshot() { }

    public PersonSnapshot(PersonCoordinate coordinate, int personId, Zone instanceZone, Zone persistantZone, Zone pastPersistantZone) {
        this(coordinate.getX(), coordinate.getY(), coordinate.getTimestamp(), coordinate.getSitProbability(),
                coordinate.getStandProbability(), coordinate.getHeadDirectionX(), coordinate.getHeadDirectionY(),
                coordinate.getImage(), personId, instanceZone, persistantZone, pastPersistantZone);
    }

    public PersonSnapshot(double x, double y, long timestamp, double standProbability, double sitProbability,
            double headDirectionY, double headDirectionX, byte[] image, int personId, Zone instanceZone,
            Zone persistantZone, Zone pastPersistantZone) {
        super(x, y, timestamp, standProbability, sitProbability, headDirectionY, headDirectionX, image);
        this.instanceZone = instanceZone;
        this.persistantZone = persistantZone;
        this.pastPersistantZone = pastPersistantZone;
        this.personId = personId;
    }

    public boolean isStored() {
        return stored;
    }

    public void setPastPersistantZone(Zone pastPersistantZone) {
        this.pastPersistantZone = pastPersistantZone;
    }

    public Zone getPastPersistantZone() {
        return pastPersistantZone;
    }

    public Zone getPersistantZone() {
        return persistantZone;
    }

    public void setPersistantZone(Zone zone) {
        this.persistantZone = zone;
    }

    public Zone getInstanceZone() {
        return instanceZone;
    }

    public void setInstanceZone(Zone instanceZone) {
        this.instanceZone = instanceZone;
    }

    public void markStored() {
        this.stored = true;
    }

    public int getPersonId() {
        return personId;
    }

    public void setPersonId(int personId) {
        this.personId = personId;
    }
}
