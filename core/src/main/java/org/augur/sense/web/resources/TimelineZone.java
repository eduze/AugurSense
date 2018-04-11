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

package org.augur.sense.web.resources;

import org.augur.sense.api.model.Person;
import org.augur.sense.api.model.Zone;
import org.augur.sense.api.model.Person;
import org.augur.sense.api.model.Zone;

public class TimelineZone {
    private long startTime;
    private long endTime;
    private Zone zone;
    private Person person;

    public Zone getZone() {
        return zone;
    }

    public void setZone(Zone zone) {
        this.zone = zone;
    }

    public long getEndTime() {
        return endTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public Person getPerson() {
        return person;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public TimelineZone(Person person, Zone zone, long startTime, long endTime){
        this.person = person;
        this.zone = zone;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
