package org.eduze.fyp.rest.resources;

import org.eduze.fyp.api.resources.PersonSnapshot;
import org.eduze.fyp.impl.db.model.Person;
import org.eduze.fyp.impl.db.model.Zone;

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
